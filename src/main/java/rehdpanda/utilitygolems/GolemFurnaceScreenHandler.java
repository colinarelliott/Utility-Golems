package rehdpanda.utilitygolems;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.ContainerData;

public class GolemFurnaceScreenHandler extends FurnaceMenu {

    private final UtilityGolem golem;

    public GolemFurnaceScreenHandler(int syncId, Inventory playerInventory) {
        super(syncId, playerInventory);
        this.golem = null;
    }

    public GolemFurnaceScreenHandler(
            int syncId,
            Inventory playerInventory,
            Container inventory,
            ContainerData properties,
            UtilityGolem golem
    ) {
        super(syncId, playerInventory, inventory, properties);
        this.golem = golem;
        checkContainerSize(inventory, 3);
    }

    @Override
    public boolean stillValid(Player player) {
        return golem == null || (golem.isAlive() && golem.distanceTo(player) < 8.0F);
    }
}