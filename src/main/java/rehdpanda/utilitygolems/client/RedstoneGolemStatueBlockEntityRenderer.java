package rehdpanda.utilitygolems.client;

import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Unit;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.model.object.statue.CopperGolemStatueModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import rehdpanda.utilitygolems.GolemType;
import rehdpanda.utilitygolems.RedstoneGolemStatueBlock;
import rehdpanda.utilitygolems.RedstoneGolemStatueBlockEntity;

public class RedstoneGolemStatueBlockEntityRenderer implements BlockEntityRenderer<RedstoneGolemStatueBlockEntity, RedstoneGolemStatueBlockEntityRenderState> {
    private final CopperGolemStatueModel model;

    public RedstoneGolemStatueBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.model = new CopperGolemStatueModel(ctx.bakeLayer(ModelLayers.COPPER_GOLEM));
    }

    @Override
    public RedstoneGolemStatueBlockEntityRenderState createRenderState() {
        return new RedstoneGolemStatueBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(RedstoneGolemStatueBlockEntity entity, RedstoneGolemStatueBlockEntityRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(entity, state, tickProgress, cameraPos, crumblingOverlay);
        state.facing = entity.getBlockState().getValue(RedstoneGolemStatueBlock.FACING);
    }

    @Override
    public void submit(RedstoneGolemStatueBlockEntityRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        matrices.pushPose();
        matrices.translate(0.5, 0.0, 0.5);
        matrices.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));
        RenderType renderLayer = RenderTypes.entityCutout(GolemType.REDSTONE.getTexture());
        
        // Use 0 for color if that's what vanilla used.
        queue.submitModel(
                this.model,
                Unit.INSTANCE,
                matrices,
                renderLayer,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0xFFFFFFFF, // color (white)
                state.breakProgress
        );
        matrices.popPose();
    }
}
