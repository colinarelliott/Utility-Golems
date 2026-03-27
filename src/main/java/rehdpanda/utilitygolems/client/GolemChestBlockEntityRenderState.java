package rehdpanda.utilitygolems.client;

import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import rehdpanda.utilitygolems.GolemType;

public class GolemChestBlockEntityRenderState extends BlockEntityRenderState {
    public float yaw;
    public float animationProgress;
    public GolemType golemType;
    public ChestType chestType;
    public boolean isStripped;
}
