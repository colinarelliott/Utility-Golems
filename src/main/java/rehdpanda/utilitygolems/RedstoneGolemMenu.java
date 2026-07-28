package rehdpanda.utilitygolems;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RedstoneGolemMenu extends AbstractContainerMenu {
    // Layout shared with RedstoneGolemScreen so the slots and the background agree.
    public static final int PANEL_WIDTH = 300;
    /** Left edge of both slot grids, centred in the wide panel. */
    public static final int SLOT_X = (PANEL_WIDTH - 9 * 18) / 2;
    public static final int GOLEM_SLOT_Y = 18;
    public static final int PLAYER_INVENTORY_Y = 182;
    public static final int HOTBAR_Y = 242;
    public static final int PANEL_HEIGHT = HOTBAR_Y + 24;

    private final Container inventory;
    private final UtilityGolem golem;

    public RedstoneGolemMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, findGolem(playerInventory));
    }

    private static UtilityGolem findGolem(Inventory inventory) {
        if (UGInit.golemFinder != null) {
            return UGInit.golemFinder.apply(inventory.player.level(), 0);
        }
        return null;
    }

    public RedstoneGolemMenu(int syncId, Inventory playerInventory, UtilityGolem golem) {
        this(syncId, playerInventory, golem.getInventory(), golem);
    }

    public RedstoneGolemMenu(int syncId, Inventory playerInventory, Container inventory, UtilityGolem golem) {
        super(UGInit.REDSTONE_GOLEM_HANDLER, syncId);
        this.inventory = inventory;
        this.golem = golem;
        checkContainerSize(inventory, 9);
        inventory.startOpen(playerInventory.player);

        int i;
        int j;
        for (j = 0; j < 9; ++j) {
            this.addSlot(new Slot(inventory, j, SLOT_X + j * 18, GOLEM_SLOT_Y));
        }

        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, SLOT_X + j * 18, PLAYER_INVENTORY_Y + i * 18));
            }
        }

        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, SLOT_X + i * 18, HOTBAR_Y));
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
            if (index < 9) {
                if (!this.moveItemStackTo(itemStack2, 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack2, 0, 9, false)) {
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
