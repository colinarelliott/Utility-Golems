package rehdpanda.utilitygolems.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import rehdpanda.utilitygolems.GolemType;
import rehdpanda.utilitygolems.UGBlocks;
import rehdpanda.utilitygolems.UGInit;
import rehdpanda.utilitygolems.UtilityGolem;
public class UGClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MenuScreens.register(UGInit.GOLEM_SCREEN_HANDLER_TYPE, GolemInventoryScreen::new);
        MenuScreens.register(UGInit.GOLEM_FURNACE_HANDLER, FurnaceScreen::new);
        MenuScreens.register(UGInit.GOLEM_JUKEBOX_HANDLER, GolemJukeboxScreen::new);
        MenuScreens.register(UGInit.REDSTONE_GOLEM_HANDLER, RedstoneGolemScreen::new);
        MenuScreens.register(UGInit.GOLEM_CHEST_SCREEN_HANDLER, GolemChestScreen::new);

        // Register block entity renderer using the non-deprecated API
        BlockEntityRenderers.register(UGBlocks.GOLEM_CHEST_BLOCK_ENTITY, GolemChestBlockEntityRenderer::new);
        BlockEntityRenderers.register(UGBlocks.REDSTONE_GOLEM_STATUE_BLOCK_ENTITY, RedstoneGolemStatueBlockEntityRenderer::new);

        // Register the renderer for all golem types
        for (GolemType type : GolemType.values()) {
            if (type == GolemType.SPONGE) {
                EntityRendererRegistry.register(UGInit.GOLEM_TYPES.get(type), SpongeGolemEntityRenderer::new);
                continue;
            }
            EntityRendererRegistry.register(
                    UGInit.GOLEM_TYPES.get(type),
                    (EntityRendererProvider.Context ctx) -> new UtilityGolemRenderer(ctx, type)
            );
        }

        ClientPlayNetworking.registerGlobalReceiver(UGInit.SyncDiscoveredTradesPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                Entity entity = context.client().level.getEntity(payload.entityId());
                if (entity instanceof UtilityGolem golem) {
                    golem.getDiscoveredTrades().clear();
                    golem.getDiscoveredTrades().addAll(payload.trades());
                }
            });
        });
    }
}
