package rehdpanda.utilitygolems.client;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import rehdpanda.utilitygolems.client.UtilityGolemRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.CopperGolemRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import rehdpanda.utilitygolems.UtilityGolem;
import rehdpanda.utilitygolems.GolemType;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;

public class SpongeGolemEntityRenderer extends UtilityGolemRenderer {
    private static final Identifier BOBBER_TEXTURE = Identifier.withDefaultNamespace("textures/entity/fishing_hook.png");
    private static final RenderType BOBBER_LAYER = RenderTypes.entityCutout(BOBBER_TEXTURE);

    public SpongeGolemEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, GolemType.SPONGE);
    }

    @Override
    public SpongeGolemEntityRenderState createRenderState() {
        return new SpongeGolemEntityRenderState();
    }

    @Override
    public void extractRenderState(rehdpanda.utilitygolems.UtilityGolem entity, UtilityGolemRenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        if (entity instanceof UtilityGolem utilityGolem && state instanceof SpongeGolemEntityRenderState spongeState) {
            spongeState.fishingTarget = utilityGolem.getFishingTarget();
        }
    }

    @Override
    public void submit(CopperGolemRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);

        if (state instanceof SpongeGolemEntityRenderState spongeState) {
            BlockPos target = spongeState.fishingTarget;
            if (target != null) {
                // Render Fishing Bobber
                matrices.pushPose();
                matrices.translate(target.getX() + 0.5 - spongeState.x, target.getY() + 0.8 - spongeState.y, target.getZ() + 0.5 - spongeState.z);
                matrices.scale(0.5F, 0.5F, 0.5F);
                matrices.mulPose(cameraState.orientation);
                
                queue.submitCustomGeometry(matrices, BOBBER_LAYER, (entry, vertices) -> {
                    Matrix4f matrix = entry.pose();
                    vertices.addVertex(matrix, -0.5F, -0.5F, 0.0F).setColor(255, 255, 255, 255).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(spongeState.lightCoords).setNormal(entry, 0.0F, 0.0F, 1.0F);
                    vertices.addVertex(matrix, 0.5F, -0.5F, 0.0F).setColor(255, 255, 255, 255).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(spongeState.lightCoords).setNormal(entry, 0.0F, 0.0F, 1.0F);
                    vertices.addVertex(matrix, 0.5F, 0.5F, 0.0F).setColor(255, 255, 255, 255).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(spongeState.lightCoords).setNormal(entry, 0.0F, 0.0F, 1.0F);
                    vertices.addVertex(matrix, -0.5F, 0.5F, 0.0F).setColor(255, 255, 255, 255).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(spongeState.lightCoords).setNormal(entry, 0.0F, 0.0F, 1.0F);
                });
                matrices.popPose();
            }
        }
    }
}
