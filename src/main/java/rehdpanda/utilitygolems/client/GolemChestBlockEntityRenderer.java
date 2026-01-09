package rehdpanda.utilitygolems.client;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.model.ChestBlockModel;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import rehdpanda.utilitygolems.GolemChestBlockEntity;
import rehdpanda.utilitygolems.GolemType;
import rehdpanda.utilitygolems.UGInit;

import java.util.HashMap;
import java.util.Map;

public class GolemChestBlockEntityRenderer implements BlockEntityRenderer<GolemChestBlockEntity, GolemChestBlockEntityRenderState> {
    private static final Map<GolemType, SpriteIdentifier> TEXTURES = new HashMap<>();
    private final ChestBlockModel model;
    private final BlockEntityRendererFactory.Context ctx;

    static {
        for (GolemType type : GolemType.values()) {
            TEXTURES.put(type, new SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, Identifier.of(UGInit.MOD_ID, "entity/chest/" + type.getName() + "_chest")));
        }
    }

    public GolemChestBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.ctx = ctx;
        this.model = new ChestBlockModel(ctx.getLayerModelPart(EntityModelLayers.CHEST));
    }

    @Override
    public GolemChestBlockEntityRenderState createRenderState() {
        return new GolemChestBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(GolemChestBlockEntity entity, GolemChestBlockEntityRenderState state, float tickProgress, Vec3d cameraPos, ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderState.updateBlockEntityRenderState(entity, state, crumblingOverlay);
        state.yaw = entity.getCachedState().get(rehdpanda.utilitygolems.GolemChestBlock.FACING).getPositiveHorizontalDegrees();
        state.animationProgress = entity.getAnimationProgress(tickProgress);
        state.golemType = entity.getGolemType();
    }

    @Override
    public void render(GolemChestBlockEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        matrices.push();
        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-state.yaw));
        matrices.translate(-0.5, -0.5, -0.5);

        float progress = state.animationProgress;
        progress = 1.0F - progress;
        progress = 1.0F - progress * progress * progress;

        SpriteIdentifier spriteIdentifier = TEXTURES.getOrDefault(state.golemType, TEXTURES.values().iterator().next());
        RenderLayer renderLayer = spriteIdentifier.getRenderLayer(RenderLayers::entityCutout);
        Sprite sprite = ctx.spriteHolder().getSprite(spriteIdentifier);
        
        queue.submitModel(
                this.model,
                progress,
                matrices,
                renderLayer,
                state.lightmapCoordinates,
                OverlayTexture.DEFAULT_UV,
                -1, // tintedColor
                sprite, // sprite
                0, // outlineColor
                state.crumblingOverlay // crumblingOverlay
        );
        matrices.pop();
    }
}
