package rehdpanda.utilitygolems.client;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.CopperGolemEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.CopperGolemEntityRenderState;
import net.minecraft.util.Identifier;
import rehdpanda.utilitygolems.GolemType;
import rehdpanda.utilitygolems.UGInit;
import rehdpanda.utilitygolems.UtilityGolem;

/// HANDLES CLIENT SIDE RENDERING OF GOLEMS

public class UGClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        // Register the renderer for all golem types
        for (GolemType type : GolemType.values()) {
            EntityRendererRegistry.register(
                    UGInit.GOLEM_TYPES.get(type),
                    (EntityRendererFactory.Context ctx) -> new MobEntityRenderer<
                            UtilityGolem,
                            CopperGolemEntityRenderState,
                            CopperGolemEntityModel
                            >(
                            ctx,
                            new CopperGolemEntityModel(ctx.getPart(EntityModelLayers.COPPER_GOLEM)),
                            0.7f
            ) {
                        @Override
                        public CopperGolemEntityRenderState createRenderState() {
                            return new CopperGolemEntityRenderState();
                        }

                        @Override
                        public Identifier getTexture(CopperGolemEntityRenderState state) {
                            // We don't have the entity here, so we get the golem type from the state if you stored it,
                            // or just default to the type texture:
                            return type.getTexture();
                        }

                        {
                            this.addFeature(new HeldItemFeatureRenderer<>(this));
                        }
                    }
            );

        }
    }
}
