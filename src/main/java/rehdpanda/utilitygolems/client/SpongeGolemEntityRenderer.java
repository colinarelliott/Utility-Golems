package rehdpanda.utilitygolems.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
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
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.util.math.Vec3d;

public class SpongeGolemEntityRenderer extends UtilityGolemRenderer {
    private static final Identifier BOBBER_TEXTURE = Identifier.ofVanilla("textures/entity/fishing_hook.png");
    private static final RenderLayer BOBBER_LAYER = RenderLayers.entityCutout(BOBBER_TEXTURE);

    public SpongeGolemEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, GolemType.SPONGE);
    }

    @Override
    public SpongeGolemEntityRenderState createRenderState() {
        return new SpongeGolemEntityRenderState();
    }

    @Override
    public void updateRenderState(net.minecraft.entity.passive.CopperGolemEntity entity, CopperGolemEntityRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        if (entity instanceof UtilityGolem utilityGolem && state instanceof SpongeGolemEntityRenderState spongeState) {
            spongeState.fishingTarget = utilityGolem.getFishingTarget();
        }
    }

    @Override
    public void render(CopperGolemEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        super.render(state, matrices, queue, cameraState);

        if (state instanceof SpongeGolemEntityRenderState spongeState) {
            BlockPos target = spongeState.fishingTarget;
            if (target != null) {
                // Render Fishing Bobber
                matrices.push();
                matrices.translate(target.getX() + 0.5 - state.x, target.getY() + 0.8 - state.y, target.getZ() + 0.5 - state.z);
                matrices.scale(0.5F, 0.5F, 0.5F);
                matrices.multiply(cameraState.orientation);
                
                queue.submitCustom(matrices, BOBBER_LAYER, (entry, vertices) -> {
                    Matrix4f matrix = entry.getPositionMatrix();
                    vertices.vertex(matrix, -0.5F, -0.5F, 0.0F).color(255, 255, 255, 255).texture(0.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(state.light).normal(entry, 0.0F, 1.0F, 0.0F);
                    vertices.vertex(matrix, 0.5F, -0.5F, 0.0F).color(255, 255, 255, 255).texture(1.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(state.light).normal(entry, 0.0F, 1.0F, 0.0F);
                    vertices.vertex(matrix, 0.5F, 0.5F, 0.0F).color(255, 255, 255, 255).texture(1.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(state.light).normal(entry, 0.0F, 1.0F, 0.0F);
                    vertices.vertex(matrix, -0.5F, 0.5F, 0.0F).color(255, 255, 255, 255).texture(0.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(state.light).normal(entry, 0.0F, 1.0F, 0.0F);
                });
                matrices.pop();
            }
        }
    }
}
