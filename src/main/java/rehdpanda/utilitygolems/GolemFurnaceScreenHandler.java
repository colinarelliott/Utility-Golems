package rehdpanda.utilitygolems;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.PropertyDelegate;

/*
public class GolemFurnaceScreenHandler extends AbstractFurnaceScreenHandler {

    private final UtilityGolem golem;


    public GolemFurnaceScreenHandler(
            int syncId,
            PlayerInventory playerInventory,
            Inventory inventory,
            PropertyDelegate properties,
            UtilityGolem golem
    ) {
        super(syncId, playerInventory, inventory, properties);
        this.golem = golem;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return golem.isAlive() && golem.distanceTo(player) < 8.0F;
    }
}
*/