package rehdpanda.utilitygolems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public class RedstoneGolemScreenHandler extends AbstractContainerMenu {
    private final UtilityGolem golem;

    public RedstoneGolemScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, null);
    }

    public RedstoneGolemScreenHandler(int syncId, Inventory playerInventory, UtilityGolem golem) {
        super(UGInit.REDSTONE_GOLEM_HANDLER, syncId);
        this.golem = golem;

        // Player Inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }

        // Hotbar
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 198));
        }
    }

    public UtilityGolem getGolem() {
        return golem;
    }

    @Override
    public boolean stillValid(Player player) {
        return (golem == null || (golem.isAlive() && golem.distanceTo(player) < 8.0F));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
