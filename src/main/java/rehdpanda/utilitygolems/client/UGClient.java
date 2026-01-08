package rehdpanda.utilitygolems.client;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.entity.CopperGolemEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.CopperGolemEntityRenderState;
import net.minecraft.util.Identifier;
import rehdpanda.utilitygolems.GolemType;
import rehdpanda.utilitygolems.UGInit;
import rehdpanda.utilitygolems.UtilityGolem;

/// HANDLES CLIENT SIDE RENDERING OF GOLEMS

public class UGClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(UGInit.GOLEM_SCREEN_HANDLER_TYPE, GolemInventoryScreen::new);

        // Register the renderer for all golem types
        for (GolemType type : GolemType.values()) {
            EntityRendererRegistry.register(
                    UGInit.GOLEM_TYPES.get(type),
                    (EntityRendererFactory.Context ctx) -> new CopperGolemEntityRenderer(ctx) {
                        @Override
                        public Identifier getTexture(CopperGolemEntityRenderState state) {
                            return type.getTexture();
                        }
                    }
            );

        }
    }
}
