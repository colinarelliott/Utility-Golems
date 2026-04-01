package rehdpanda.utilitygolems.client;

import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import rehdpanda.utilitygolems.GolemType;
import rehdpanda.utilitygolems.client.model.NetheriteGolemModel;
import rehdpanda.utilitygolems.client.model.UGModelLayers;

public class NetheriteGolemEntityRenderer extends UtilityGolemRenderer {

    public NetheriteGolemEntityRenderer(EntityRendererProvider.Context ctx, GolemType type) {
        super(ctx, type);
        this.model = new NetheriteGolemModel(ctx.bakeLayer(UGModelLayers.NETHERITE_GOLEM));
        this.layers.clear();
        this.addLayer(new ItemInHandLayer<>(this));
    }

}
