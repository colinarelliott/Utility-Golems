package rehdpanda.utilitygolems.client;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.CopperGolemRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.entity.state.CopperGolemRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import rehdpanda.utilitygolems.GolemAnimation;
import rehdpanda.utilitygolems.GolemType;
import rehdpanda.utilitygolems.UtilityGolem;

public class UtilityGolemRenderer extends CopperGolemRenderer {
    private final GolemType type;

    public UtilityGolemRenderer(EntityRendererProvider.Context ctx, GolemType type) {
        super(ctx);
        this.type = type;
    }

    public Identifier getTextureLocation(CopperGolemRenderState state) {
        if (state instanceof UtilityGolemRenderState renderState) {
            if (renderState.isLampOn && type == GolemType.LAMP) {
                return Identifier.fromNamespaceAndPath("utility-golems", "textures/entity/lamp_golem_illuminated.png");
            }
            if (renderState.isSmelting && (type == GolemType.FURNACE || type == GolemType.SMOKER || type == GolemType.BLAST_FURNACE)) {
                return Identifier.fromNamespaceAndPath("utility-golems", "textures/entity/" + type.getName() + "_illuminated.png");
            }
            if (renderState.isStripped && type == GolemType.BAMBOO) {
                return Identifier.fromNamespaceAndPath("utility-golems", "textures/entity/stripped_bamboo_golem.png");
            }
        }
        return type.getTexture();
    }

    public UtilityGolemRenderState createRenderState() {
        return new UtilityGolemRenderState();
    }

    public void extractRenderState(UtilityGolem entity, UtilityGolemRenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        if (entity instanceof UtilityGolem utilityGolem && state instanceof UtilityGolemRenderState renderState) {
            renderState.chestPos = utilityGolem.getChestPos();
            renderState.aiTarget = utilityGolem.getDebugTarget();
            renderState.isDebug = utilityGolem.hasCustomName() && "debug".equalsIgnoreCase(utilityGolem.getCustomName().getString());
            renderState.isLampOn = utilityGolem.isLampOn();
            renderState.isStripped = utilityGolem.isStripped();
            renderState.isSmelting = utilityGolem.isSmelting();
            renderState.isTinted = utilityGolem.getGolemType() == GolemType.TINTED_GLASS;
            renderState.yawDegrees = utilityGolem.getYRot();
            renderState.headYaw = utilityGolem.getYHeadRot();
            renderState.headPitch = utilityGolem.getXRot();
            renderState.animationId = utilityGolem.getAnimation().ordinal();
            renderState.animationProgress = utilityGolem.getAnimationProgress(tickDelta);
            renderState.mainHandItem = utilityGolem.getMainHandItem().copy();
        }
    }

    protected RenderType getRenderTypeForModel(CopperGolemRenderState state, boolean translucent, boolean showOutline) {
        Identifier texture = getTextureLocation(state);
        if (showOutline) {
            if (state instanceof UtilityGolemRenderState renderState && renderState.isDebug) {
                return translucent ? RenderTypes.entityTranslucent(texture) : RenderTypes.entityCutout(texture);
            }
            return null;
        }
        if (state instanceof UtilityGolemRenderState renderState && renderState.isTinted) {
            return RenderTypes.entityTranslucent(texture);
        }
        return translucent ? RenderTypes.entityTranslucent(texture) : RenderTypes.entityCutout(texture);
    }

    private boolean isOutlinePass = false;

    @Override
    protected RenderType getRenderType(CopperGolemRenderState state, boolean showBody, boolean translucent, boolean showOutline) {
        this.isOutlinePass = showOutline;
        if (showOutline) {
            boolean isDebug = state instanceof UtilityGolemRenderState rs && rs.isDebug;
            if (!isDebug) return null;
        }
        return getRenderTypeForModel(state, translucent, showOutline);
    }

    @Override
    public void submit(CopperGolemRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        boolean isDebug = state instanceof UtilityGolemRenderState rs && rs.isDebug;
        
        matrices.pushPose();
        
        // Apply animations to the PoseStack so both body and features follow them
        applyAnimations(state, matrices);

        // Let vanilla handle the rest (rotations, standard flip, model rendering, layers)
        super.submit(state, matrices, queue, cameraState);

        matrices.popPose();

        if (this.isOutlinePass && isDebug) {
            if (state instanceof UtilityGolemRenderState renderState) {
                if (renderState.chestPos != null) {
                    renderDebugLine(renderState, renderState.chestPos, 0, 255, 0, matrices, queue); // Green for chest
                }
                if (renderState.aiTarget != null) {
                    renderDebugLine(renderState, renderState.aiTarget, 255, 0, 0, matrices, queue); // Red for AI target
                }
            }
        }
    }

    protected void applyAnimations(CopperGolemRenderState state, PoseStack matrices) {
        if (state instanceof UtilityGolemRenderState renderState) {
            float p = renderState.animationProgress;
            GolemAnimation anim = renderState.getAnimation();

            switch (anim) {
                case DIGGING, CHOPPING, FARMING -> {
                    float angle = -15.0f * (float) Math.sin(p * Math.PI * 2.0);
                    matrices.mulPose(com.mojang.math.Axis.XP.rotationDegrees(angle));
                }
                case ATTACKING -> {
                    float z = (float) Math.sin(p * Math.PI) * 0.4f;
                    float angle = (float) Math.sin(p * Math.PI * 10.0) * 5.0f;
                    matrices.translate(0.0, 0.0, z);
                    matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle));
                }
                case CONNECTING -> {
                    float dx = (float) Math.sin(p * Math.PI * 20.0) * 0.05f;
                    float dz = (float) Math.cos(p * Math.PI * 20.0) * 0.05f;
                    matrices.translate(dx, 0.0, dz);
                }
                case NODDING, TRADING -> {
                    float angle = -15.0f * (float) Math.sin(p * Math.PI * 2.0);
                    matrices.mulPose(com.mojang.math.Axis.XP.rotationDegrees(angle));
                }
                case LIGHTING, PLACING -> {
                    float z = (float) Math.sin(p * Math.PI) * 0.3f;
                    float angle = -15.0f * (float) Math.sin(p * Math.PI);
                    matrices.translate(0.0, 0.0, z);
                    matrices.mulPose(com.mojang.math.Axis.XP.rotationDegrees(angle));
                }
                case SMELTING -> {
                    float y = (float) Math.sin(p * Math.PI * 2.0) * 0.05f;
                    matrices.translate(0.0, y, 0.0);
                }
                case REDSTONE, BREEDING -> {
                    float angle = -15.0f * (float) Math.sin(p * Math.PI);
                    matrices.mulPose(com.mojang.math.Axis.XP.rotationDegrees(angle));
                }
                case PLAYING_MUSIC -> {
                    float angle = (float) Math.sin(p * Math.PI * 4.0) * 15.0f;
                    matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle));
                }
                case SEARCHING -> {
                    float tiltAngle = -10.0f * (float) Math.sin(p * Math.PI);
                    float swayAngle = 15.0f * (float) Math.sin(p * Math.PI * 2.0);
                    matrices.mulPose(com.mojang.math.Axis.XP.rotationDegrees(tiltAngle));
                    matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(swayAngle));
                }
                case PRESSING_BUTTON -> {
                    float angle = -25.0f * (float) Math.sin(p * Math.PI);
                    matrices.mulPose(com.mojang.math.Axis.XP.rotationDegrees(angle));
                }
                default -> {
                }
            }
        }
    }

    protected void renderDebugLine(UtilityGolemRenderState state, BlockPos targetPos, int r, int g, int b, PoseStack matrices, SubmitNodeCollector queue) {
    }
}
