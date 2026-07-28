package rehdpanda.utilitygolems;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ContainerData;

public class GolemFurnaceMenu extends AbstractContainerMenu {
    private final ContainerData propertyDelegate;
    private final Container inventory;
    private final UtilityGolem golem;

    public GolemFurnaceMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(3), new net.minecraft.world.inventory.SimpleContainerData(4), findGolem(playerInventory));
    }

    private static UtilityGolem findGolem(Inventory inventory) {
        if (UGInit.golemFinder != null) {
            return UGInit.golemFinder.apply(inventory.player.level(), 0);
        }
        return null;
    }

    public GolemFurnaceMenu(int syncId, Inventory playerInventory, Container inventory, ContainerData propertyDelegate, UtilityGolem golem) {
        super(UGInit.GOLEM_FURNACE_HANDLER, syncId);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        this.golem = golem;
        checkContainerSize(inventory, 3);
        inventory.startOpen(playerInventory.player);
        this.addDataSlots(propertyDelegate);

        this.addSlot(new Slot(inventory, 0, 56, 17)); // Input
        this.addSlot(new Slot(inventory, 1, 56, 53)); // Fuel
        this.addSlot(new Slot(inventory, 2, 116, 35)); // Output

        int i;
        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return inventory.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (index < 3) {
                if (!this.moveItemStackTo(itemStack2, 3, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack2, 0, 3, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.inventory.stopOpen(player);
    }

    public UtilityGolem getGolem() {
        return golem;
    }

    public int getCookProgress() {
        int i = this.propertyDelegate.get(2);
        int j = this.propertyDelegate.get(3);
        return j != 0 && i != 0 ? i * 24 / j : 0;
    }

    public int getBurnProgress() {
        int i = this.propertyDelegate.get(1);
        if (i == 0) {
            i = 200;
        }
        return this.propertyDelegate.get(0) * 13 / i;
    }

    public boolean isLit() {
        return this.propertyDelegate.get(0) > 0;
    }
}
