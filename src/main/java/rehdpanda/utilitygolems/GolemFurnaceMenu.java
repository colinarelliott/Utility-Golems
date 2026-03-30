package rehdpanda.utilitygolems;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GolemFurnaceMenu extends AbstractContainerMenu {
    private final net.minecraft.world.inventory.ContainerData propertyDelegate;
    private final Container inventory;
    private final UtilityGolem golem;

    public GolemFurnaceMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(3), new net.minecraft.world.inventory.SimpleContainerData(4), null);
    }

    public GolemFurnaceMenu(int syncId, Inventory playerInventory, Container inventory, net.minecraft.world.inventory.ContainerData propertyDelegate, UtilityGolem golem) {
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
}
