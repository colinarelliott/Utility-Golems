package rehdpanda.utilitygolems.client;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CopperGolemRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.rendertype.RenderType;
import rehdpanda.utilitygolems.GolemType;
import rehdpanda.utilitygolems.client.model.BambooGolemModel;
import rehdpanda.utilitygolems.client.model.UGModelLayers;

public class BambooGolemEntityRenderer extends UtilityGolemRenderer {

    public BambooGolemEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, GolemType.BAMBOO);
        this.model = new BambooGolemModel(ctx.bakeLayer(UGModelLayers.BAMBOO_GOLEM));
        this.layers.clear();
        this.addLayer(new ItemInHandLayer<>(this));
    }

    @Override
    public void submit(CopperGolemRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        matrices.pushPose();
        
        // 135.0F rotation correction to align the custom model with vanilla feature layers (eyes)
        // This must be applied before calling super.submit to ensure all layers follow it.
        matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(135.0F));
        
        // super.submit will render the body and any layers (like eyes) using our custom model
        super.submit(state, matrices, queue, cameraState);
        
        matrices.popPose();
    }
}
