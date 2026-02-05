package rehdpanda.utilitygolems.client;

import net.minecraft.block.enums.ChestType;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.util.math.Direction;
import rehdpanda.utilitygolems.GolemType;

public class GolemChestBlockEntityRenderState extends BlockEntityRenderState {
    public float yaw;
    public float animationProgress;
    public GolemType golemType;
    public ChestType chestType;
    public boolean isStripped;
}
