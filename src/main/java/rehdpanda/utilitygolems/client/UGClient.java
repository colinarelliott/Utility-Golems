// java
package rehdpanda.utilitygolems.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.FurnaceScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.entity.CopperGolemEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.CopperGolemEntityRenderState;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.util.Identifier;
import rehdpanda.utilitygolems.GolemType;
import rehdpanda.utilitygolems.UGInit;
import rehdpanda.utilitygolems.UGBlocks;

/// HANDLES CLIENT SIDE RENDERING OF GOLEMS
public class UGClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(UGInit.GOLEM_SCREEN_HANDLER_TYPE, GolemInventoryScreen::new);
        HandledScreens.register(UGInit.GOLEM_FURNACE_HANDLER, FurnaceScreen::new);

        // Register block entity renderer using the non-deprecated API
        BlockEntityRendererFactories.register(UGBlocks.GOLEM_CHEST_BLOCK_ENTITY, GolemChestBlockEntityRenderer::new);

        // Register the renderer for all golem types
        for (GolemType type : GolemType.values()) {
            if (type == GolemType.SPONGE) {
                EntityRendererRegistry.register(UGInit.GOLEM_TYPES.get(type), SpongeGolemEntityRenderer::new);
                continue;
            }
            EntityRendererRegistry.register(
                    UGInit.GOLEM_TYPES.get(type),
                    (EntityRendererFactory.Context ctx) -> new UtilityGolemRenderer(ctx, type)
            );
        }
    }
}
