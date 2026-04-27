package rehdpanda.utilitygolems.client;

import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import rehdpanda.utilitygolems.GolemType;
import rehdpanda.utilitygolems.client.model.NetheriteGolemModel;
import rehdpanda.utilitygolems.client.model.UGModelLayers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.CopperGolemRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class NetheriteGolemEntityRenderer extends UtilityGolemRenderer {

    public NetheriteGolemEntityRenderer(EntityRendererProvider.Context ctx, GolemType type) {
        super(ctx, type);
        this.model = new NetheriteGolemModel(ctx.bakeLayer(UGModelLayers.NETHERITE_GOLEM));
        this.layers.clear();
        this.addLayer(new ItemInHandLayer<>(this));
    }

    @Override
    public void submit(CopperGolemRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        matrices.pushPose();
        
        // 180.0F rotation correction to align the custom model with vanilla rendering orientation.
        // This ensures the golem faces forward when walking.
        matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F));
        
        super.submit(state, matrices, queue, cameraState);
        
        matrices.popPose();
    }
}
