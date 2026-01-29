package rehdpanda.utilitygolems.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.CopperGolemEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.CopperGolemEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import rehdpanda.utilitygolems.UtilityGolem;
import rehdpanda.utilitygolems.GolemType;
import org.joml.Matrix4f;

public class UtilityGolemRenderer extends CopperGolemEntityRenderer {
    private final GolemType type;

    public UtilityGolemRenderer(EntityRendererFactory.Context ctx, GolemType type) {
        super(ctx);
        this.type = type;
    }

    @Override
    public Identifier getTexture(CopperGolemEntityRenderState state) {
        return type.getTexture();
    }

    @Override
    public UtilityGolemRenderState createRenderState() {
        return new UtilityGolemRenderState();
    }

    @Override
    public void updateRenderState(net.minecraft.entity.passive.CopperGolemEntity entity, CopperGolemEntityRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        if (entity instanceof UtilityGolem utilityGolem && state instanceof UtilityGolemRenderState renderState) {
            renderState.chestPos = utilityGolem.getChestPos();
            renderState.aiTarget = utilityGolem.getDebugTarget();
            renderState.isDebug = utilityGolem.hasCustomName() && "debug".equalsIgnoreCase(utilityGolem.getCustomName().getString());
        }
    }

    @Override
    public void render(CopperGolemEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        super.render(state, matrices, queue, cameraState);

        if (state instanceof UtilityGolemRenderState renderState && renderState.isDebug) {
            if (renderState.chestPos != null) {
                renderDebugLine(renderState, renderState.chestPos, 0, 255, 0, matrices, queue); // Green for chest
            }
            if (renderState.aiTarget != null) {
                renderDebugLine(renderState, renderState.aiTarget, 255, 0, 0, matrices, queue); // Red for AI target
            }
        }
    }

    protected void renderDebugLine(UtilityGolemRenderState state, BlockPos targetPos, int r, int g, int b, MatrixStack matrices, OrderedRenderCommandQueue queue) {
    }
}
