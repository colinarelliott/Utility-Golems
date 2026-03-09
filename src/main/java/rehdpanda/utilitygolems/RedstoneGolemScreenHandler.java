package rehdpanda.utilitygolems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class RedstoneGolemScreenHandler extends ScreenHandler {
    private final UtilityGolem golem;

    public RedstoneGolemScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, null);
    }

    public RedstoneGolemScreenHandler(int syncId, PlayerInventory playerInventory, UtilityGolem golem) {
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
    public boolean canUse(PlayerEntity player) {
        return (golem == null || (golem.isAlive() && golem.distanceTo(player) < 8.0F));
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        return ItemStack.EMPTY;
    }
}
