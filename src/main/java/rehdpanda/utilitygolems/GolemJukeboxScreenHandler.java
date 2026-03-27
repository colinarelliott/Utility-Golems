package rehdpanda.utilitygolems;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.core.component.DataComponents;

public class GolemJukeboxScreenHandler extends AbstractContainerMenu {
    private final Container inventory;
    private final UtilityGolem golem;

    public GolemJukeboxScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(9), null);
    }

    public GolemJukeboxScreenHandler(int syncId, Inventory playerInventory, Container inventory, UtilityGolem golem) {
        super(UGInit.GOLEM_JUKEBOX_HANDLER, syncId);
        checkContainerSize(inventory, 9);
        this.inventory = inventory;
        this.golem = golem;

        inventory.startOpen(playerInventory.player);

        // Jukebox Playlist (1x9)
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.has(DataComponents.JUKEBOX_PLAYABLE);
                }
            });
        }

        // Player Inventory (shifted down by 48 pixels for buttons)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + 48 + row * 18));
            }
        }

        // Hotbar (shifted down)
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142 + 48));
        }
    }

    public UtilityGolem getGolem() {
        return golem;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player) && (golem == null || (golem.isAlive() && golem.distanceTo(player) < 8.0F));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < 9) {
                if (!this.moveItemStackTo(stack, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!stack.has(DataComponents.JUKEBOX_PLAYABLE)) {
                    return ItemStack.EMPTY;
                }
                if (!this.moveItemStackTo(stack, 0, 9, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.inventory.stopOpen(player);
    }
}
