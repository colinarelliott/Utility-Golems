package rehdpanda.utilitygolems;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;

public class GolemJukeboxMenu extends AbstractContainerMenu {
    /** Discs the golem's playlist holds. Must match UtilityGolem#jukeboxInventory. */
    public static final int PLAYLIST_SIZE = 9;

    // Layout shared with GolemJukeboxScreen so the slots and the background agree.
    /** Height of the top panel: 17px border/title strip + one 18px row of playlist slots. */
    public static final int TOP_HEIGHT = 35;
    /** Blank strip between the playlist and the player inventory, where the transport buttons live. */
    public static final int CONTROLS_HEIGHT = 48;
    /** Y of the playlist slot row. */
    public static final int PLAYLIST_Y = 18;
    /** Y at which the player inventory panel starts. */
    public static final int PLAYER_INVENTORY_Y = TOP_HEIGHT + CONTROLS_HEIGHT;

    private final Container inventory;
    private final Inventory playerInventory;
    private UtilityGolem golem;

    public GolemJukeboxMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(PLAYLIST_SIZE), findGolem(playerInventory));
    }

    private static UtilityGolem findGolem(Inventory inventory) {
        if (UGInit.golemFinder != null) {
            return UGInit.golemFinder.apply(inventory.player.level(), 0);
        }
        return null;
    }

    public GolemJukeboxMenu(int syncId, Inventory playerInventory, Container inventory, UtilityGolem golem) {
        super(UGInit.GOLEM_JUKEBOX_HANDLER, syncId);
        this.inventory = inventory;
        this.playerInventory = playerInventory;
        this.golem = golem;
        checkContainerSize(inventory, PLAYLIST_SIZE);
        inventory.startOpen(playerInventory.player);

        for (int i = 0; i < PLAYLIST_SIZE; ++i) {
            this.addSlot(new DiscSlot(inventory, i, 8 + i * 18, PLAYLIST_Y));
        }

        int i;
        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, PLAYER_INVENTORY_Y + 14 + i * 18));
            }
        }

        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, PLAYER_INVENTORY_Y + 72));
        }
    }

    /** Playlist slots take music discs only, one per slot. */
    private static class DiscSlot extends Slot {
        DiscSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return JukeboxSong.fromStack(stack).isPresent();
        }

        @Override
        public int getMaxStackSize() {
            return 1;
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
            if (index < PLAYLIST_SIZE) {
                if (!this.moveItemStackTo(itemStack2, PLAYLIST_SIZE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack2, 0, PLAYLIST_SIZE, false)) {
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
        if (this.golem == null) {
            // On the client the entity may not have been resolvable when the menu was built.
            this.golem = findGolem(this.playerInventory);
        }
        return this.golem;
    }
}
