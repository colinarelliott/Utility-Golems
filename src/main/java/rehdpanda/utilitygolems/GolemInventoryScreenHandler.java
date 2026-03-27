package rehdpanda.utilitygolems;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

/**
 * Handles the 3x3 inventory UI for utility golems
 */
public class GolemInventoryScreenHandler extends AbstractContainerMenu {

    private static final int GOLEM_INV_SIZE = 9;
    private static final int HELD_ITEM_SLOT_INDEX = 9; // Slot index for held item in the UI
    private static final int PLAYER_INV_START = 10;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_END = PLAYER_INV_END + 9;

    private final Container inventory;
    private final Container heldItemInventory = new SimpleContainer(1);
    private final UtilityGolem golem;

    public UtilityGolem getGolem() {
        return golem;
    }

    // Client constructor
    public GolemInventoryScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(GOLEM_INV_SIZE), null);
    }

    // Server constructor
    public GolemInventoryScreenHandler(
            int syncId,
            Inventory playerInventory,
            Container inventory,
            UtilityGolem golem
    ) {
        super(UGInit.GOLEM_SCREEN_HANDLER_TYPE, syncId);
        checkContainerSize(inventory, GOLEM_INV_SIZE);

        this.inventory = inventory;
        this.golem = golem;

        if (golem != null) {
            this.heldItemInventory.setItem(0, golem.getHeldItem());
        }

        inventory.startOpen(playerInventory.player);

        addGolemInventory(inventory);
        addHeldItemSlot();
        addPlayerInventory(playerInventory);
        addHotbar(playerInventory);
    }

    private void addHeldItemSlot() {
        this.addSlot(new ReadOnlySlot(this.heldItemInventory, 0, 134, 35));
    }

    private class ReadOnlySlot extends Slot {
        public ReadOnlySlot(Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player playerEntity) {
            return false;
        }

        @Override
        public ItemStack getItem() {
            if (golem != null) {
                return golem.getHeldItem();
            }
            return super.getItem();
        }
    }

    private void addGolemInventory(Container inventory) {
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
        public GolemSlot(Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (golem != null && golem.getGolemType() == GolemType.HOPPER) {
                for (int i = 0; i < GOLEM_INV_SIZE; i++) {
                    if (i == this.getContainerSlot()) continue;
                    ItemStack otherStack = container.getItem(i);
                    // Use areItemsEqual to ignore NBT/components for Hopper Golem filtering slots
                    if (!otherStack.isEmpty() && ItemStack.isSameItem(stack, otherStack)) {
                        return false;
                    }
                }
            }
            return super.mayPlace(stack);
        }
    }

    private void addPlayerInventory(Inventory playerInventory) {
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

    private void addHotbar(Inventory playerInventory) {
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
    public void broadcastChanges() {
        if (this.golem != null) {
            ItemStack currentHeldItem = golem.getHeldItem();
            if (!ItemStack.matches(this.heldItemInventory.getItem(0), currentHeldItem)) {
                this.heldItemInventory.setItem(0, currentHeldItem.copy());
            }
        }
        super.broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        return inventory.stillValid(player)
                && (golem == null || (golem.isAlive() && golem.distanceTo(player) < 8.0F));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if (index < PLAYER_INV_START) {
                if (!moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, GOLEM_INV_SIZE, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            slot.onTake(player, stack);
        }

        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.inventory.stopOpen(player);
        if (player.level() != null && this.inventory instanceof GolemChestBlockEntity golemChestBlockEntity) {
            player.level().blockEvent(golemChestBlockEntity.getBlockPos(), golemChestBlockEntity.getBlockState().getBlock(), 1, 0);
        }
    }
}
