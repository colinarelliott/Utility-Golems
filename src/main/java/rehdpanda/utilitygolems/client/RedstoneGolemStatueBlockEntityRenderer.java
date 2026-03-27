package rehdpanda.utilitygolems.client;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.model.CopperGolemStatueModel;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import rehdpanda.utilitygolems.GolemType;
import rehdpanda.utilitygolems.RedstoneGolemStatueBlock;
import rehdpanda.utilitygolems.RedstoneGolemStatueBlockEntity;

public class RedstoneGolemStatueBlockEntityRenderer implements BlockEntityRenderer<RedstoneGolemStatueBlockEntity, RedstoneGolemStatueBlockEntityRenderState> {
    private final CopperGolemStatueModel model;

    public RedstoneGolemStatueBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.model = new CopperGolemStatueModel(ctx.getLayerModelPart(EntityModelLayers.COPPER_GOLEM));
    }

    @Override
    public RedstoneGolemStatueBlockEntityRenderState createRenderState() {
        return new RedstoneGolemStatueBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(RedstoneGolemStatueBlockEntity entity, RedstoneGolemStatueBlockEntityRenderState state, float tickProgress, Vec3d cameraPos, ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(entity, state, tickProgress, cameraPos, crumblingOverlay);
        state.facing = entity.getCachedState().get(RedstoneGolemStatueBlock.FACING);
    }

    @Override
    public void render(RedstoneGolemStatueBlockEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        matrices.push();
        matrices.translate(0.5, 0.0, 0.5);
        RenderLayer renderLayer = RenderLayers.entityCutoutNoCull(GolemType.REDSTONE.getTexture());
        
        // Use 0 for color if that's what vanilla used.
        queue.submitModel(
                this.model,
                state.facing,
                matrices,
                renderLayer,
                state.lightmapCoordinates,
                OverlayTexture.DEFAULT_UV,
                0, // color/tintedColor
                state.crumblingOverlay
        );
        matrices.pop();
    }
}
