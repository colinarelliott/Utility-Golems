package rehdpanda.utilitygolems.client;

import net.minecraft.client.render.entity.state.CopperGolemEntityRenderState;
import net.minecraft.util.math.BlockPos;

public class UtilityGolemRenderState extends CopperGolemEntityRenderState {
    public BlockPos chestPos;
    public BlockPos aiTarget;
    public boolean isDebug;
    public boolean isLampOn;
}
