package rehdpanda.utilitygolems.client;

import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;
import com.mojang.math.Axis;
import net.minecraft.world.phys.Vec3;
import rehdpanda.utilitygolems.GolemChestBlockEntity;
import rehdpanda.utilitygolems.GolemType;
import rehdpanda.utilitygolems.UGInit;

import java.util.HashMap;
import java.util.Map;

public class GolemChestBlockEntityRenderer implements BlockEntityRenderer<GolemChestBlockEntity, GolemChestBlockEntityRenderState> {
    private static final Map<GolemType, Identifier> SINGLE_TEXTURES = new HashMap<>();
    private static final Map<GolemType, Identifier> LEFT_TEXTURES = new HashMap<>();
    private static final Map<GolemType, Identifier> RIGHT_TEXTURES = new HashMap<>();
    private final ChestModel singleModel;
    private final ChestModel leftModel;
    private final ChestModel rightModel;
    private final BlockEntityRendererProvider.Context ctx;

    static {
        for (GolemType type : GolemType.values()) {
            SINGLE_TEXTURES.put(type, Identifier.fromNamespaceAndPath("utility-golems", "textures/entity/chest/" + type.getName() + "_chest.png"));
            LEFT_TEXTURES.put(type, Identifier.fromNamespaceAndPath("utility-golems", "textures/entity/chest/" + type.getName() + "_chest_left.png"));
            RIGHT_TEXTURES.put(type, Identifier.fromNamespaceAndPath("utility-golems", "textures/entity/chest/" + type.getName() + "_chest_right.png"));
        }
    }

    public GolemChestBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.ctx = ctx;
        this.singleModel = new ChestModel(ctx.bakeLayer(ModelLayers.CHEST));
        this.leftModel = new ChestModel(ctx.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT));
        this.rightModel = new ChestModel(ctx.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT));
    }

    @Override
    public GolemChestBlockEntityRenderState createRenderState() {
        return new GolemChestBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(GolemChestBlockEntity entity, GolemChestBlockEntityRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(entity, state, crumblingOverlay);
        state.yaw = entity.getBlockState().getValue(rehdpanda.utilitygolems.GolemChestBlock.FACING).toYRot();
        state.animationProgress = entity.getOpenNess(tickProgress);
        state.golemType = entity.getGolemType();
        state.chestType = entity.getBlockState().getValue(rehdpanda.utilitygolems.GolemChestBlock.CHEST_TYPE);
        state.isStripped = entity.getBlockState().hasProperty(rehdpanda.utilitygolems.GolemChestBlock.STRIPPED) && entity.getBlockState().getValue(rehdpanda.utilitygolems.GolemChestBlock.STRIPPED);
    }

    @Override
    public void submit(GolemChestBlockEntityRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        matrices.pushPose();
        matrices.translate(0.5, 0.5, 0.5);
        matrices.mulPose(Axis.YP.rotationDegrees(-state.yaw));
        matrices.translate(-0.5, -0.5, -0.5);

        float progress = state.animationProgress;
        progress = 1.0F - progress;
        progress = 1.0F - progress * progress * progress;

        Identifier identifier = switch (state.chestType) {
            case SINGLE -> state.isStripped && state.golemType == GolemType.BAMBOO 
                    ? Identifier.fromNamespaceAndPath("utility-golems", "textures/entity/chest/stripped_bamboo_golem_chest.png")
                    : SINGLE_TEXTURES.get(state.golemType);
            case LEFT -> state.isStripped && state.golemType == GolemType.BAMBOO 
                    ? Identifier.fromNamespaceAndPath("utility-golems", "textures/entity/chest/stripped_bamboo_golem_chest_left.png")
                    : LEFT_TEXTURES.get(state.golemType);
            case RIGHT -> state.isStripped && state.golemType == GolemType.BAMBOO 
                    ? Identifier.fromNamespaceAndPath("utility-golems", "textures/entity/chest/stripped_bamboo_golem_chest_right.png")
                    : RIGHT_TEXTURES.get(state.golemType);
        };
        if (identifier == null) {
            identifier = SINGLE_TEXTURES.values().iterator().next();
        }
        RenderType renderLayer = state.golemType == GolemType.TINTED_GLASS
                ? RenderTypes.entityTranslucent(identifier)
                : RenderTypes.entityCutout(identifier);

        ChestModel model = switch (state.chestType) {
            case SINGLE -> this.singleModel;
            case LEFT -> this.leftModel;
            case RIGHT -> this.rightModel;
        };

        queue.submitModel(
                model,
                progress,
                matrices,
                renderLayer,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                -1, // tintedColor
                null, // sprite
                0, // outlineColor
                state.breakProgress // crumblingOverlay
        );
        matrices.popPose();
    }
}
