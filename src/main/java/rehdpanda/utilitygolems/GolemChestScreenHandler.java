package rehdpanda.utilitygolems;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public class GolemChestScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final BlockPos pos;
    private boolean golemDead;

    public GolemChestScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(54), BlockPos.ORIGIN, false);
    }

    public GolemChestScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, BlockPos pos, boolean golemDead) {
        super(UGInit.GOLEM_CHEST_SCREEN_HANDLER, syncId);
        this.inventory = inventory;
        this.pos = pos;
        this.golemDead = golemDead;

        int rows = inventory.size() / 9;
        inventory.onOpen(playerInventory.player);

        // Chest inventory
        for (int j = 0; j < rows; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(inventory, k + j * 9, 8 + k * 18, 18 + j * 18));
            }
        }

        int playerInvY = 31 + rows * 18;

        // Player inventory
        for (int j = 0; j < 3; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, playerInvY + j * 18));
            }
        }

        // Hotbar
        for (int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18, playerInvY + 58));
        }
    }

    public int getRows() {
        return this.inventory.size() / 9;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        int rows = getRows();
        int inventorySize = rows * 9;
        if (slot != null && slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (index < inventorySize) {
                if (!this.insertItem(itemStack2, inventorySize, inventorySize + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(itemStack2, 0, inventorySize, false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTakeItem(player, itemStack2);
        }
        return itemStack;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.inventory.onClose(player);
    }

    public boolean isGolemDead() {
        return golemDead;
    }
}
