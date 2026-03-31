package rehdpanda.utilitygolems.client;

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
            renderState.animationId = utilityGolem.getAnimation().ordinal();
            renderState.animationProgress = utilityGolem.getAnimationProgress(tickDelta);
        }
    }

    @Override
    protected RenderType getRenderType(CopperGolemRenderState state, boolean showBody, boolean translucent, boolean showOutline) {
        Identifier texture = getTextureLocation(state);
        if (state instanceof UtilityGolemRenderState renderState && renderState.isTinted) {
            return RenderTypes.entityTranslucent(texture);
        }
        return translucent ? RenderTypes.entityTranslucent(texture) : RenderTypes.entityCutout(texture);
    }

    @Override
    public void submit(CopperGolemRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        // Apply simple whole-body pose offsets based on current animation
        if (state instanceof UtilityGolemRenderState renderState) {
            float p = renderState.animationProgress;
            GolemAnimation anim = renderState.getAnimation();
            float yawDegrees = renderState.yawDegrees;
            
            // Tinted Glass Golem's held items should be rendered
            // UtilityGolemEntityRenderer usually doesn't render held items as the base model doesn't have an arm that supports it in vanilla?
            // Wait, UtilityGolem is from a mod or specific version. 
            // If the model doesn't support it, we'd need a feature renderer.
            
            // If debug mode is on, we might see it chat or console
            if (renderState.isDebug && anim != GolemAnimation.IDLE) {
                // System.out.println("[DEBUG] Rendering Golem " + type + " with animation: " + anim + " at progress " + p);
            }

            switch (anim) {
                // Lean in and out (forward/backward) while digging/chopping/farming
                case DIGGING, CHOPPING, FARMING -> {
                    float angle = -15.0f * (float) Math.sin(p * Math.PI * 2.0); // Leaning in and out
                    matrices.pushPose();
                    matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-yawDegrees));
                    matrices.mulPose(com.mojang.math.Axis.XP.rotationDegrees(angle));
                    matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yawDegrees));
                    super.submit(state, matrices, queue, cameraState);
                    matrices.popPose();
                    return;
                }
                case FISHING -> {
                    // No more bobbing for fishing
                }
                // Forward thrust/shaking for attacking
                case ATTACKING -> {
                    float z = (float) Math.sin(p * Math.PI) * 0.4f;
                    float angle = (float) Math.sin(p * Math.PI * 10.0) * 5.0f; // Rapid vibration
                    matrices.pushPose();
                    matrices.translate(0.0, 0.0, z);
                    matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle));
                    super.submit(state, matrices, queue, cameraState);
                    matrices.popPose();
                    return;
                }
                // High-frequency vibration for redstone connecting
                case CONNECTING -> {
                    float dx = (float) Math.sin(p * Math.PI * 20.0) * 0.05f;
                    float dz = (float) Math.cos(p * Math.PI * 20.0) * 0.05f;
                    matrices.pushPose();
                    matrices.translate(dx, 0.0, dz);
                    super.submit(state, matrices, queue, cameraState);
                    matrices.popPose();
                    return;
                }
                // Energetic head-nodding for trading/giving items
                case NODDING, TRADING -> {
                    float angle = -15.0f * (float) Math.sin(p * Math.PI * 2.0); // 2 nods, reduced angle
                    matrices.pushPose();
                    matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-yawDegrees));
                    matrices.mulPose(com.mojang.math.Axis.XP.rotationDegrees(angle));
                    matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yawDegrees));
                    super.submit(state, matrices, queue, cameraState);
                    matrices.popPose();
                    return;
                }
                // Quick forward reach for lighting/placing
                case LIGHTING, PLACING -> {
                    float z = (float) Math.sin(p * Math.PI) * 0.3f;
                    float angle = -15.0f * (float) Math.sin(p * Math.PI);
                    matrices.pushPose();
                    matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-yawDegrees));
                    matrices.translate(0.0, 0.0, z);
                    matrices.mulPose(com.mojang.math.Axis.XP.rotationDegrees(angle));
                    matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yawDegrees));
                    super.submit(state, matrices, queue, cameraState);
                    matrices.popPose();
                    return;
                }
                // Slow "breathing" bob for smelting
                case SMELTING -> {
                    float y = (float) Math.sin(p * Math.PI * 2.0) * 0.05f;
                    matrices.pushPose();
                    matrices.translate(0.0, y, 0.0);
                    super.submit(state, matrices, queue, cameraState);
                    matrices.popPose();
                    return;
                }
                // Lean forward a bit when working redstone/breeding
                case REDSTONE, BREEDING -> {
                    float angle = -15.0f * (float) Math.sin(p * Math.PI); // Sinusoidal lean for smoother motion
                    matrices.pushPose();
                    matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-yawDegrees));
                    matrices.mulPose(com.mojang.math.Axis.XP.rotationDegrees(angle));
                    matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yawDegrees));
                    super.submit(state, matrices, queue, cameraState);
                    matrices.popPose();
                    return;
                }
                // Sway side-to-side when playing music
                case PLAYING_MUSIC -> {
                    float angle = (float) Math.sin(p * Math.PI * 4.0) * 15.0f; // More energetic sway
                    matrices.pushPose();
                    matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle));
                    super.submit(state, matrices, queue, cameraState);
                    matrices.popPose();
                    return;
                }
                case SEARCHING -> {
                    // Nod side to side and tilt forward a bit
                    float tiltAngle = -10.0f * (float) Math.sin(p * Math.PI);
                    float swayAngle = 15.0f * (float) Math.sin(p * Math.PI * 2.0);
                    matrices.pushPose();
                    matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-yawDegrees));
                    matrices.mulPose(com.mojang.math.Axis.XP.rotationDegrees(tiltAngle));
                    matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(swayAngle));
                    matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yawDegrees));
                    super.submit(state, matrices, queue, cameraState);
                    matrices.popPose();
                    return;
                }
                case DEPOSITING, WITHDRAWING, SPINNING_HEAD -> {
                    // These are now handled by UtilityGolemEntity's AnimationStates
                    // which are processed by the superclass's model/renderer.
                }
                case PRESSING_BUTTON -> {
                    // Quick forward lean for button pressing
                    float angle = -25.0f * (float) Math.sin(p * Math.PI);
                    matrices.pushPose();
                    matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-yawDegrees));
                    matrices.mulPose(com.mojang.math.Axis.XP.rotationDegrees(angle));
                    matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yawDegrees));
                    super.submit(state, matrices, queue, cameraState);
                    matrices.popPose();
                    return;
                }
                default -> {
                }
            }
        }
        
        super.submit(state, matrices, queue, cameraState);

        if (state instanceof UtilityGolemRenderState renderState && renderState.isDebug) {
            if (renderState.chestPos != null) {
                renderDebugLine(renderState, renderState.chestPos, 0, 255, 0, matrices, queue); // Green for chest
            }
            if (renderState.aiTarget != null) {
                renderDebugLine(renderState, renderState.aiTarget, 255, 0, 0, matrices, queue); // Red for AI target
            }
        }
    }

    protected void renderDebugLine(UtilityGolemRenderState state, BlockPos targetPos, int r, int g, int b, PoseStack matrices, SubmitNodeCollector queue) {
    }
}
