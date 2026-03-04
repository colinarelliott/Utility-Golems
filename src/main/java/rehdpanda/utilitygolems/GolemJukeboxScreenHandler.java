package rehdpanda.utilitygolems;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.component.DataComponentTypes;

public class GolemJukeboxScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final UtilityGolem golem;

    public GolemJukeboxScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(9), null);
    }

    public GolemJukeboxScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, UtilityGolem golem) {
        super(UGInit.GOLEM_JUKEBOX_HANDLER, syncId);
        checkSize(inventory, 9);
        this.inventory = inventory;
        this.golem = golem;

        inventory.onOpen(playerInventory.player);

        // Jukebox Playlist (1x9)
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 18) {
                @Override
                public boolean canInsert(ItemStack stack) {
                    return stack.contains(DataComponentTypes.JUKEBOX_PLAYABLE);
                }
            });
        }

        // Player Inventory (shifted down by 18 pixels for buttons)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Hotbar (shifted down)
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public UtilityGolem getGolem() {
        return golem;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player) && (golem == null || (golem.isAlive() && golem.distanceTo(player) < 8.0F));
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack stack = slot.getStack();
            result = stack.copy();
            if (index < 9) {
                if (!this.insertItem(stack, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!stack.contains(DataComponentTypes.JUKEBOX_PLAYABLE)) {
                    return ItemStack.EMPTY;
                }
                if (!this.insertItem(stack, 0, 9, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return result;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.inventory.onClose(player);
    }
}
