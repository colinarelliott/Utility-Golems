package rehdpanda.utilitygolems.client;

import net.minecraft.block.enums.ChestType;
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
    private static final Map<GolemType, Identifier> SINGLE_TEXTURES = new HashMap<>();
    private static final Map<GolemType, Identifier> LEFT_TEXTURES = new HashMap<>();
    private static final Map<GolemType, Identifier> RIGHT_TEXTURES = new HashMap<>();
    private final ChestBlockModel singleModel;
    private final ChestBlockModel leftModel;
    private final ChestBlockModel rightModel;
    private final BlockEntityRendererFactory.Context ctx;

    static {
        for (GolemType type : GolemType.values()) {
            SINGLE_TEXTURES.put(type, Identifier.of("utility-golems", "textures/entity/chest/" + type.getName() + "_chest.png"));
            LEFT_TEXTURES.put(type, Identifier.of("utility-golems", "textures/entity/chest/" + type.getName() + "_chest_left.png"));
            RIGHT_TEXTURES.put(type, Identifier.of("utility-golems", "textures/entity/chest/" + type.getName() + "_chest_right.png"));
        }
    }

    public GolemChestBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.ctx = ctx;
        this.singleModel = new ChestBlockModel(ctx.getLayerModelPart(EntityModelLayers.CHEST));
        this.leftModel = new ChestBlockModel(ctx.getLayerModelPart(EntityModelLayers.DOUBLE_CHEST_LEFT));
        this.rightModel = new ChestBlockModel(ctx.getLayerModelPart(EntityModelLayers.DOUBLE_CHEST_RIGHT));
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
        state.chestType = entity.getCachedState().get(rehdpanda.utilitygolems.GolemChestBlock.CHEST_TYPE);
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

        Identifier identifier = switch (state.chestType) {
            case SINGLE -> SINGLE_TEXTURES.get(state.golemType);
            case LEFT -> LEFT_TEXTURES.get(state.golemType);
            case RIGHT -> RIGHT_TEXTURES.get(state.golemType);
        };
        if (identifier == null) {
            identifier = SINGLE_TEXTURES.values().iterator().next();
        }
        RenderLayer renderLayer = RenderLayers.entityCutout(identifier);

        ChestBlockModel model = switch (state.chestType) {
            case SINGLE -> this.singleModel;
            case LEFT -> this.leftModel;
            case RIGHT -> this.rightModel;
        };

        queue.submitModel(
                model,
                progress,
                matrices,
                renderLayer,
                state.lightmapCoordinates,
                OverlayTexture.DEFAULT_UV,
                -1, // tintedColor
                null, // sprite
                0, // outlineColor
                state.crumblingOverlay // crumblingOverlay
        );
        matrices.pop();
    }
}
