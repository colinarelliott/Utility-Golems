package rehdpanda.utilitygolems;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.core.BlockPos;

public class GolemChestMenu extends AbstractContainerMenu {
    private final Container inventory;
    private final BlockPos pos;
    private boolean golemDead;

    public GolemChestMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(54), BlockPos.ZERO, false);
    }

    public GolemChestMenu(int syncId, Inventory playerInventory, Container inventory, BlockPos pos, boolean golemDead) {
        super(UGInit.GOLEM_CHEST_HANDLER, syncId);
        this.inventory = inventory;
        this.pos = pos;
        this.golemDead = golemDead;

        int rows = inventory.getContainerSize() / 9;
        inventory.startOpen(playerInventory.player);

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
        return this.inventory.getContainerSize() / 9;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        int rows = getRows();
        int inventorySize = rows * 9;
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (index < inventorySize) {
                if (!this.moveItemStackTo(itemStack2, inventorySize, inventorySize + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack2, 0, inventorySize, false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemStack2);
        }
        return itemStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.inventory.stopOpen(player);
    }

    public boolean isGolemDead() {
        return golemDead;
    }
}
