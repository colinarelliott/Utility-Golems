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
import rehdpanda.utilitygolems.GolemAnimation;
import org.joml.Matrix4f;

public class UtilityGolemRenderer extends CopperGolemEntityRenderer {
    private final GolemType type;

    public UtilityGolemRenderer(EntityRendererFactory.Context ctx, GolemType type) {
        super(ctx);
        this.type = type;
    }

    @Override
    public Identifier getTexture(CopperGolemEntityRenderState state) {
        if (state instanceof UtilityGolemRenderState renderState) {
            if (renderState.isLampOn && type == GolemType.LAMP) {
                return Identifier.of("utility-golems", "textures/entity/lamp_golem_illuminated.png");
            }
            if (renderState.isStripped && type == GolemType.BAMBOO) {
                return Identifier.of("utility-golems", "textures/entity/stripped_bamboo_golem.png");
            }
        }
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
            renderState.isLampOn = utilityGolem.isLampOn();
            renderState.isStripped = utilityGolem.isStripped();
            renderState.animationId = utilityGolem.getAnimation().ordinal();
            renderState.animationProgress = utilityGolem.getAnimationProgress(tickDelta);
        }
    }

    @Override
    public void render(CopperGolemEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        // Apply simple whole-body pose offsets based on current animation
        if (state instanceof UtilityGolemRenderState renderState) {
            float p = renderState.animationProgress;
            GolemAnimation anim = renderState.getAnimation();
            
            // If debug mode is on, we might see it in chat or console
            if (renderState.isDebug && anim != GolemAnimation.IDLE) {
                // System.out.println("[DEBUG] Rendering Golem " + type + " with animation: " + anim + " at progress " + p);
            }

            switch (anim) {
                // Lean in and out (forward/backward) while digging/chopping/farming
                case DIGGING, CHOPPING, FARMING -> {
                    float angle = -15.0f * (float) Math.sin(p * Math.PI * 2.0); // Leaning in and out
                    matrices.push();
                    matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(angle));
                    super.render(state, matrices, queue, cameraState);
                    matrices.pop();
                    return;
                }
                case FISHING -> {
                    // No more bobbing for fishing
                }
                // Forward thrust/shaking for attacking
                case ATTACKING -> {
                    float z = (float) Math.sin(p * Math.PI) * 0.4f;
                    float angle = (float) Math.sin(p * Math.PI * 10.0) * 5.0f; // Rapid vibration
                    matrices.push();
                    matrices.translate(0.0, 0.0, z);
                    matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(angle));
                    super.render(state, matrices, queue, cameraState);
                    matrices.pop();
                    return;
                }
                // High-frequency vibration for redstone connecting
                case CONNECTING -> {
                    float dx = (float) Math.sin(p * Math.PI * 20.0) * 0.05f;
                    float dz = (float) Math.cos(p * Math.PI * 20.0) * 0.05f;
                    matrices.push();
                    matrices.translate(dx, 0.0, dz);
                    super.render(state, matrices, queue, cameraState);
                    matrices.pop();
                    return;
                }
                // Energetic head-nodding for trading/giving items
                case NODDING, TRADING -> {
                    float angle = -15.0f * (float) Math.sin(p * Math.PI * 2.0); // 2 nods, reduced angle
                    matrices.push();
                    matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(angle));
                    super.render(state, matrices, queue, cameraState);
                    matrices.pop();
                    return;
                }
                // Quick forward reach for lighting/placing
                case LIGHTING, PLACING -> {
                    float z = (float) Math.sin(p * Math.PI) * 0.3f;
                    float angle = -15.0f * (float) Math.sin(p * Math.PI);
                    matrices.push();
                    matrices.translate(0.0, 0.0, z);
                    matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(angle));
                    super.render(state, matrices, queue, cameraState);
                    matrices.pop();
                    return;
                }
                // Slow "breathing" bob for smelting
                case SMELTING -> {
                    float y = (float) Math.sin(p * Math.PI * 2.0) * 0.05f;
                    matrices.push();
                    matrices.translate(0.0, y, 0.0);
                    super.render(state, matrices, queue, cameraState);
                    matrices.pop();
                    return;
                }
                // Lean forward a bit when working redstone/breeding
                case REDSTONE, BREEDING -> {
                    float angle = -15.0f * (float) Math.sin(p * Math.PI); // Sinusoidal lean for smoother motion
                    matrices.push();
                    matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(angle));
                    super.render(state, matrices, queue, cameraState);
                    matrices.pop();
                    return;
                }
                // Sway side-to-side when playing music
                case PLAYING_MUSIC -> {
                    float angle = (float) Math.sin(p * Math.PI * 4.0) * 15.0f; // More energetic sway
                    matrices.push();
                    matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(angle));
                    super.render(state, matrices, queue, cameraState);
                    matrices.pop();
                    return;
                }
                case SEARCHING -> {
                    // Nod side to side and tilt forward a bit
                    float tiltAngle = -10.0f * (float) Math.sin(p * Math.PI);
                    float swayAngle = 15.0f * (float) Math.sin(p * Math.PI * 2.0);
                    matrices.push();
                    matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(tiltAngle));
                    matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(swayAngle));
                    super.render(state, matrices, queue, cameraState);
                    matrices.pop();
                    return;
                }
                case DEPOSITING -> {
                    // Reach forward and down
                    float z = (float) Math.sin(p * Math.PI) * 0.4f;
                    float xAngle = -20.0f * (float) Math.sin(p * Math.PI);
                    matrices.push();
                    matrices.translate(0.0, 0.0, z);
                    matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(xAngle));
                    super.render(state, matrices, queue, cameraState);
                    matrices.pop();
                    return;
                }
                case WITHDRAWING -> {
                    // Reach forward then pull back
                    float z = (float) Math.sin(p * Math.PI) * 0.4f;
                    float xAngle = -15.0f * (float) Math.sin(p * Math.PI);
                    matrices.push();
                    matrices.translate(0.0, 0.0, z);
                    matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(xAngle));
                    super.render(state, matrices, queue, cameraState);
                    matrices.pop();
                    return;
                }
                case CATCHING_FISH -> {
                    float y = (float) Math.abs(Math.sin(p * Math.PI)) * 0.4f;
                    matrices.push();
                    matrices.translate(0.0, y, 0.0);
                    super.render(state, matrices, queue, cameraState);
                    matrices.pop();
                    return;
                }
                case CATCHING_RARE_FISH -> {
                    float y = (float) Math.abs(Math.sin(p * Math.PI)) * 0.8f;
                    matrices.push();
                    matrices.translate(0.0, y, 0.0);
                    super.render(state, matrices, queue, cameraState);
                    matrices.pop();
                    return;
                }
                default -> {
                }
            }
        }
        
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
