package rehdpanda.utilitygolems.client;

import net.minecraft.client.render.entity.state.CopperGolemEntityRenderState;
import net.minecraft.util.math.BlockPos;

public class UtilityGolemRenderState extends CopperGolemEntityRenderState {
    public BlockPos chestPos;
    public BlockPos aiTarget;
    public boolean isDebug;
    public boolean isLampOn;
    public boolean isStripped;
    public boolean isSmelting;
    public float yawDegrees;

    // Animation state copied from entity each frame
    public int animationId;
    public float animationProgress;

    public rehdpanda.utilitygolems.GolemAnimation getAnimation() {
        if (animationId < 0 || animationId >= rehdpanda.utilitygolems.GolemAnimation.values().length) {
            return rehdpanda.utilitygolems.GolemAnimation.IDLE;
        }
        return rehdpanda.utilitygolems.GolemAnimation.values()[animationId];
    }
}
