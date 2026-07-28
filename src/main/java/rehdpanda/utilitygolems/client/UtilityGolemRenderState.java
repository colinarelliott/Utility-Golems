package rehdpanda.utilitygolems.client;

import net.minecraft.client.renderer.entity.state.CopperGolemRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public class UtilityGolemRenderState extends CopperGolemRenderState {
    public BlockPos chestPos;
    public BlockPos aiTarget;
    public boolean isDebug;
    public boolean isLampOn;
    public boolean isStripped;
    public boolean isSmelting;
    public boolean isTinted;
    public float yawDegrees;
    public float headYaw;
    public float headPitch;
    public ItemStack mainHandItem = ItemStack.EMPTY;

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
