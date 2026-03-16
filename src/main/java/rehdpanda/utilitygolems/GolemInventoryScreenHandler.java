package rehdpanda.utilitygolems;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

/// Handles the 3x3 inventory UI for utility golems
public class GolemInventoryScreenHandler extends ScreenHandler {

    private static final int GOLEM_INV_SIZE = 9;
    private static final int PLAYER_INV_START = GOLEM_INV_SIZE;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_END = PLAYER_INV_END + 9;

    private final Inventory inventory;
    private final UtilityGolem golem;

    public UtilityGolem getGolem() {
        return golem;
    }

    // Client constructor
    public GolemInventoryScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(GOLEM_INV_SIZE), null);
    }

    // Server constructor
    public GolemInventoryScreenHandler(
            int syncId,
            PlayerInventory playerInventory,
            Inventory inventory,
            UtilityGolem golem
    ) {
        super(UGInit.GOLEM_SCREEN_HANDLER_TYPE, syncId);
        checkSize(inventory, GOLEM_INV_SIZE);

        this.inventory = inventory;
        this.golem = golem;

        inventory.onOpen(playerInventory.player);

        addGolemInventory(inventory);
        addPlayerInventory(playerInventory);
        addHotbar(playerInventory);
    }

    private void addGolemInventory(Inventory inventory) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 3; ++col) {
                int index = col + row * 3;
                this.addSlot(new GolemSlot(
                        inventory,
                        index,
                        62 + col * 18,
                        17 + row * 18
                ));
            }
        }
    }

    private class GolemSlot extends Slot {
        public GolemSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            if (golem != null && golem.getGolemType() == GolemType.HOPPER) {
                for (int i = 0; i < GOLEM_INV_SIZE; i++) {
                    if (i == this.getIndex()) continue;
                    ItemStack otherStack = inventory.getStack(i);
                    if (!otherStack.isEmpty() && ItemStack.areItemsEqual(stack, otherStack)) {
                        return false;
                    }
                }
            }
            return super.canInsert(stack);
        }
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        UtilityGolem golem = getGolem();
        int offset = 0;
        if (golem != null && (golem.getGolemType() == GolemType.DIAMOND || golem.getGolemType() == GolemType.EMERALD)) {
            offset = 40; // Always use max offset
        }
        
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        8 + col * 18,
                        84 + offset + row * 18
                ));
            }
        }
    }

    private void addHotbar(PlayerInventory playerInventory) {
        UtilityGolem golem = getGolem();
        int offset = 0;
        if (golem != null && (golem.getGolemType() == GolemType.DIAMOND || golem.getGolemType() == GolemType.EMERALD)) {
            offset = 40;
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(
                    playerInventory,
                    i,
                    8 + i * 18,
                    142 + offset
            ));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player)
                && (golem == null || (golem.isAlive() && golem.distanceTo(player) < 8.0F));
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasStack()) {
            ItemStack stack = slot.getStack();
            result = stack.copy();

            if (index < GOLEM_INV_SIZE) {
                if (!insertItem(stack, PLAYER_INV_START, HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!insertItem(stack, 0, GOLEM_INV_SIZE, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            slot.onTakeItem(player, stack);
        }

        return result;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.inventory.onClose(player);
        if (player.getEntityWorld() != null && this.inventory instanceof GolemChestBlockEntity golemChestBlockEntity) {
            player.getEntityWorld().addSyncedBlockEvent(golemChestBlockEntity.getPos(), golemChestBlockEntity.getCachedState().getBlock(), 1, 0);
        }
    }
}
