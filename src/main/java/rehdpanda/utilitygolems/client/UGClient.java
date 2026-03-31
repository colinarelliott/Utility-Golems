package rehdpanda.utilitygolems.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.inventory.MenuType;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.BiConsumer;
import rehdpanda.utilitygolems.GolemType;
import rehdpanda.utilitygolems.UGBlocks;
import rehdpanda.utilitygolems.UGInit;
import rehdpanda.utilitygolems.UtilityGolem;

public class UGClient implements ClientModInitializer {

    public static class ClientFabricBridge {
        public static void registerScreen(MenuType<?> type, Object factory) {
            try {
                Class<?> screensClass = Class.forName("net.minecraft.client.gui.screens.MenuScreens");
                findMethod(screensClass, "register", 2).invoke(null, type, factory);
            } catch (Exception e) { e.printStackTrace(); }
        }

        public static void registerEntityRenderer(EntityType<?> type, EntityRendererProvider<?> provider) {
            try {
                Class<?> registryClass = Class.forName("net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry");
                findMethod(registryClass, "register", 2).invoke(null, type, provider);
            } catch (Exception e) { e.printStackTrace(); }
        }

        public static void registerBlockEntityRenderer(BlockEntityType<?> type, Object provider) {
            try {
                Class<?> registryClass = Class.forName("net.minecraft.client.renderer.blockentity.BlockEntityRenderers");
                findMethod(registryClass, "register", 2).invoke(null, type, provider);
            } catch (Exception e) { e.printStackTrace(); }
        }

        public static void registerReceiver(Object id, BiConsumer<Object, Object> handler) {
            try {
                Class<?> networkingClass = Class.forName("net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking");
                Class<?> handlerClass = Class.forName("net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking$PlayPayloadHandler");
                Object proxy = Proxy.newProxyInstance(handlerClass.getClassLoader(), new Class[]{handlerClass}, (p, method, args) -> {
                    if (method.getName().equals("receive")) {
                        handler.accept(args[0], args[1]);
                    }
                    return null;
                });
                try {
                    findMethod(networkingClass, "registerGlobalReceiver", 2).invoke(null, id, proxy);
                } catch (Exception e) {
                    // Try to find if it's named differently in some versions
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        private static Method findMethod(Class<?> clazz, String name, int paramCount) {
            for (Method m : clazz.getMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == paramCount) {
                    return m;
                }
            }
            throw new RuntimeException("Method " + name + " with " + paramCount + " params not found in " + clazz.getName());
        }
    }

    @Override
    public void onInitializeClient() {
        ClientFabricBridge.registerScreen(UGInit.GOLEM_SCREEN_HANDLER_TYPE, (Object)(MenuScreens.ScreenConstructor<rehdpanda.utilitygolems.GolemInventoryMenu, GolemInventoryScreen>)((handler, inventory, title) -> new GolemInventoryScreen(handler, inventory, title)));
        ClientFabricBridge.registerScreen(UGInit.GOLEM_FURNACE_HANDLER, (Object)(MenuScreens.ScreenConstructor<net.minecraft.world.inventory.FurnaceMenu, FurnaceScreen>)((handler, inventory, title) -> new FurnaceScreen(handler, inventory, title)));
        ClientFabricBridge.registerScreen(UGInit.GOLEM_JUKEBOX_HANDLER, (Object)(MenuScreens.ScreenConstructor<rehdpanda.utilitygolems.GolemJukeboxMenu, GolemJukeboxScreen>)((handler, inventory, title) -> new GolemJukeboxScreen(handler, inventory, title)));
        ClientFabricBridge.registerScreen(UGInit.REDSTONE_GOLEM_HANDLER, (Object)(MenuScreens.ScreenConstructor<rehdpanda.utilitygolems.RedstoneGolemMenu, RedstoneGolemScreen>)((handler, inventory, title) -> new RedstoneGolemScreen(handler, inventory, title)));
        ClientFabricBridge.registerScreen(UGInit.GOLEM_CHEST_HANDLER, (Object)(MenuScreens.ScreenConstructor<rehdpanda.utilitygolems.GolemChestMenu, GolemChestScreen>)((handler, inventory, title) -> new GolemChestScreen(handler, inventory, title)));

        ClientFabricBridge.registerBlockEntityRenderer(UGBlocks.GOLEM_CHEST_BLOCK_ENTITY, (Object)(net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider<rehdpanda.utilitygolems.GolemChestBlockEntity, GolemChestBlockEntityRenderState>)((ctx) -> new GolemChestBlockEntityRenderer(ctx)));
        ClientFabricBridge.registerBlockEntityRenderer(UGBlocks.REDSTONE_GOLEM_STATUE_BLOCK_ENTITY, (Object)(net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider<rehdpanda.utilitygolems.RedstoneGolemStatueBlockEntity, RedstoneGolemStatueBlockEntityRenderState>)((ctx) -> new RedstoneGolemStatueBlockEntityRenderer(ctx)));

        for (GolemType type : GolemType.values()) {
            if (type == GolemType.SPONGE) {
                ClientFabricBridge.registerEntityRenderer(UGInit.GOLEM_TYPES.get(type), (EntityRendererProvider)(ctx -> (net.minecraft.client.renderer.entity.EntityRenderer)(Object)new SpongeGolemEntityRenderer(ctx)));
                continue;
            }
            ClientFabricBridge.registerEntityRenderer(UGInit.GOLEM_TYPES.get(type), (EntityRendererProvider)(ctx -> (net.minecraft.client.renderer.entity.EntityRenderer)(Object)new UtilityGolemRenderer(ctx, type)));
        }

        ClientFabricBridge.registerReceiver(UGInit.SyncDiscoveredTradesPayload.ID, (payload, context) -> {
            Minecraft.getInstance().execute(() -> {
                Entity entity = Minecraft.getInstance().level.getEntity(((UGInit.SyncDiscoveredTradesPayload)payload).entityId());
                if (entity instanceof UtilityGolem golem) {
                    golem.getDiscoveredTrades().clear();
                    golem.getDiscoveredTrades().addAll(((UGInit.SyncDiscoveredTradesPayload)payload).trades());
                }
            });
        });
    }
}
