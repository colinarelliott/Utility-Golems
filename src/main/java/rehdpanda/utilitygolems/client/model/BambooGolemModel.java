package rehdpanda.utilitygolems.client.model;

import net.minecraft.client.model.ArmedModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.entity.state.CopperGolemRenderState;
import rehdpanda.utilitygolems.client.UtilityGolemRenderState;
import rehdpanda.utilitygolems.GolemAnimation;
import rehdpanda.utilitygolems.UtilityGolem;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;

public class BambooGolemModel extends net.minecraft.client.model.animal.golem.CopperGolemModel implements ArmedModel<CopperGolemRenderState> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public BambooGolemModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.leftArm = this.body.getChild("left_arm");
        this.rightArm = this.body.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition root = meshdefinition.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(2, 17).addBox(-4.0F, -6.0F, -2.0F, 8.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(40, 43).addBox(-3.0F, -6.0F, 2.0F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 19.0F, 0.0F));

        body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(4, 4).addBox(-4.0F, -5.0F, -3.0F, 8.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(56, 0).addBox(-1.0F, -2.0F, -5.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(38, 9).addBox(0.0F, -9.0F, 1.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(-0.01F))
                .texOffs(38, 8).addBox(-3.0F, -9.0F, -2.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.01F))
                .texOffs(38, 8).addBox(-1.0F, -8.0F, -3.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.01F))
                .texOffs(38, 8).addBox(-2.0F, -7.0F, 1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.01F))
                .texOffs(39, 9).addBox(1.0F, -7.0F, -2.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, -6.0F, 0.0F));

        body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(52, 18).addBox(-2.0F, -1.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -6.0F, 0.0F));

        body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(39, 18).addBox(0.0F, -1.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -6.0F, 0.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(19, 29).addBox(-2.9F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 19.0F, 0.0F));

        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(3, 29).addBox(0.9F, 0.0F, -0.99F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 19.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(CopperGolemRenderState state) {
        super.setupAnim(state);
        // Reset parts to default rotation
        this.head.xRot = 0;
        this.head.yRot = 0;
        this.head.zRot = 0;
        this.body.xRot = 0;
        this.body.yRot = 0;
        this.body.zRot = 0;
        this.leftArm.xRot = 0;
        this.leftArm.yRot = 0;
        this.leftArm.zRot = 0;
        this.rightArm.xRot = 0;
        this.rightArm.yRot = 0;
        this.rightArm.zRot = 0;
        this.leftLeg.xRot = 0;
        this.leftLeg.yRot = 0;
        this.leftLeg.zRot = 0;
        this.rightLeg.xRot = 0;
        this.rightLeg.yRot = 0;
        this.rightLeg.zRot = 0;

        // Basic head rotation
        if (state instanceof UtilityGolemRenderState rs) {
            float relativeHeadYaw = rs.headYaw - state.yRot;
            this.head.yRot = relativeHeadYaw * ((float)Math.PI / 180F);
            this.head.xRot = rs.headPitch * ((float)Math.PI / 180F);
        } else {
            this.head.yRot = 0;
            this.head.xRot = 0;
        }

        // Basic walking animation (if moving)
        float limbSwing = state.walkAnimationPos;
        float limbSwingAmount = state.walkAnimationSpeed;
        
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;

        // Custom farming animations
        if (state instanceof UtilityGolemRenderState renderState) {
            if (renderState.getAnimation() == GolemAnimation.FARMING) {
                float p = renderState.animationProgress;
                boolean isTilling = UtilityGolem.isHoe(renderState.mainHandItem);
                
                if (isTilling) {
                    // tillGround animation
                    // Body tilt
                    float bodyTilt = 0.0f;
                    if (p < 0.25f) bodyTilt = Mth.lerp(p / 0.25f, 0.0f, -10.0f);
                    else if (p < 1.08f) bodyTilt = -10.0f;
                    else if (p < 1.29f) bodyTilt = Mth.lerp((p - 1.08f) / 0.21f, -10.0f, 0.0f);
                    this.body.xRot += bodyTilt * ((float)Math.PI / 180F);

                    // Right arm swing
                    float armSwing = 0.0f;
                    if (p > 0.54f && p < 1.29f) {
                        armSwing = 50.0f * Mth.sin((p - 0.54f) / (1.29f - 0.54f) * (float)Math.PI);
                    }
                    this.rightArm.xRot += armSwing * ((float)Math.PI / 180F);
                    
                    // Head tilt
                    float headTilt = 0.0f;
                    if (p < 0.25f) headTilt = Mth.lerp(p / 0.25f, 0.0f, -20.0f);
                    else if (p < 0.66f) headTilt = Mth.lerp((p - 0.25f) / 0.41f, -20.0f, -30.0f);
                    else if (p < 0.91f) headTilt = Mth.lerp((p - 0.66f) / 0.25f, -30.0f, -20.0f);
                    else if (p < 1.25f) headTilt = Mth.lerp((p - 0.91f) / 0.34f, -20.0f, 0.0f);
                    this.head.xRot += headTilt * ((float)Math.PI / 180F);
                } else {
                    // plantSeeds animation (simplified)
                    // Body tilt deeper
                    float bodyTilt = 0.0f;
                    if (p < 0.25f) bodyTilt = Mth.lerp(p / 0.25f, 0.0f, -30.0f);
                    else if (p < 1.29f) bodyTilt = Mth.lerp((p - 0.25f) / 1.04f, -30.0f, 0.0f);
                    this.body.xRot += bodyTilt * ((float)Math.PI / 180F);

                    // Arms movement
                    float armMove = Mth.sin(p * (float)Math.PI) * 45.0f;
                    this.leftArm.xRot += armMove * ((float)Math.PI / 180F);
                    this.rightArm.xRot += armMove * ((float)Math.PI / 180F);
                }
            }
        }
    }

    @Override
    public void translateToHand(CopperGolemRenderState state, HumanoidArm arm, PoseStack matrices) {
        this.body.translateAndRotate(matrices);
        this.getArm(arm).translateAndRotate(matrices);
        matrices.translate(0.0, 0.0, 0.0);
    }

    private ModelPart getArm(HumanoidArm arm) {
        return arm == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
    }
}
