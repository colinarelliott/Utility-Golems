package rehdpanda.utilitygolems;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.PropertyDelegate;

public class GolemFurnaceScreenHandler extends FurnaceScreenHandler {

    private final UtilityGolem golem;

    public GolemFurnaceScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(syncId, playerInventory);
        this.golem = null;
    }

    public GolemFurnaceScreenHandler(
            int syncId,
            PlayerInventory playerInventory,
            Inventory inventory,
            PropertyDelegate properties,
            UtilityGolem golem
    ) {
        super(syncId, playerInventory, inventory, properties);
        this.golem = golem;
        checkSize(inventory, 3);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return golem == null || (golem.isAlive() && golem.distanceTo(player) < 8.0F);
    }
}