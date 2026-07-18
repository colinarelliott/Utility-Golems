package rehdpanda.utilitygolems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.world.level.block.entity.BlockEntityType;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.BiFunction;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;

public class UGInit implements ModInitializer {

    // Helper: find entity by numeric id across all server levels (Mojang types only)
    private static net.minecraft.world.entity.Entity findEntityById(net.minecraft.server.MinecraftServer server, int id) {
        for (ServerLevel level : server.getAllLevels()) {
            net.minecraft.world.entity.Entity e = level.getEntity(id);
            if (e != null) return e;
        }
        return null;
    }


    public static final String MOD_ID = "utility-golems";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Interaction state for client-side menu initialization
    public static int lastInteractedGolemId = -1;
    public static net.minecraft.core.BlockPos lastInteractedChestPos = net.minecraft.core.BlockPos.ZERO;
    public static boolean lastInteractedChestDead = false;
    public static int lastInteractedChestSize = 27;

    public record SyncPatternPayload(int entityId, int patternOrdinal, int width, int length, ItemStack filter, boolean started, String schematicName) implements CustomPacketPayload {
        public static final Type<SyncPatternPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "sync_pattern"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncPatternPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, SyncPatternPayload::entityId,
                ByteBufCodecs.VAR_INT, SyncPatternPayload::patternOrdinal,
                ByteBufCodecs.VAR_INT, SyncPatternPayload::width,
                ByteBufCodecs.VAR_INT, SyncPatternPayload::length,
                ItemStack.OPTIONAL_STREAM_CODEC, SyncPatternPayload::filter,
                ByteBufCodecs.BOOL, SyncPatternPayload::started,
                ByteBufCodecs.STRING_UTF8, SyncPatternPayload::schematicName,
                SyncPatternPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record SyncDiscoveredTradesPayload(int entityId, List<ItemStack> trades) implements CustomPacketPayload {
        public static final Type<SyncDiscoveredTradesPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "sync_discovered_trades"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncDiscoveredTradesPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, SyncDiscoveredTradesPayload::entityId,
                ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), SyncDiscoveredTradesPayload::trades,
                SyncDiscoveredTradesPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record OpenGolemInventoryPayload(int entityId) implements CustomPacketPayload {
        public static final Type<OpenGolemInventoryPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "open_golem_inventory"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenGolemInventoryPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, OpenGolemInventoryPayload::entityId,
                OpenGolemInventoryPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record SelectBuyItemPayload(int entityId, ItemStack selectedItem) implements CustomPacketPayload {
        public static final Type<SelectBuyItemPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "select_buy_item"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SelectBuyItemPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, SelectBuyItemPayload::entityId,
                ItemStack.OPTIONAL_STREAM_CODEC, SelectBuyItemPayload::selectedItem,
                SelectBuyItemPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record JukeboxActionPayload(int entityId, int actionId) implements CustomPacketPayload {
        public static final Type<JukeboxActionPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "jukebox_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf, JukeboxActionPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, JukeboxActionPayload::entityId,
                ByteBufCodecs.VAR_INT, JukeboxActionPayload::actionId,
                JukeboxActionPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record RedstoneActionPayload(int entityId, int actionId) implements CustomPacketPayload {
        public static final Type<RedstoneActionPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "redstone_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RedstoneActionPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, RedstoneActionPayload::entityId,
                ByteBufCodecs.VAR_INT, RedstoneActionPayload::actionId,
                RedstoneActionPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record SyncRedstoneProgramPayload(int entityId, List<UtilityGolem.RedstoneInteraction> program) implements CustomPacketPayload {
        public static final Type<SyncRedstoneProgramPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "sync_redstone_program"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncRedstoneProgramPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, SyncRedstoneProgramPayload::entityId,
                StreamCodec.composite(
                        BlockPos.STREAM_CODEC, UtilityGolem.RedstoneInteraction::pos,
                        ByteBufCodecs.VAR_INT, UtilityGolem.RedstoneInteraction::interval,
                        UtilityGolem.RedstoneInteraction::new
                ).apply(ByteBufCodecs.list()), SyncRedstoneProgramPayload::program,
                SyncRedstoneProgramPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record ClearCactusSlotPayload(int entityId, int slotIndex) implements CustomPacketPayload {
        public static final Type<ClearCactusSlotPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "clear_cactus_slot"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ClearCactusSlotPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, ClearCactusSlotPayload::entityId,
                ByteBufCodecs.VAR_INT, ClearCactusSlotPayload::slotIndex,
                ClearCactusSlotPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public static final Map<GolemType, EntityType<UtilityGolem>> GOLEM_TYPES = new HashMap<>();
    public static java.util.function.BiFunction<net.minecraft.world.level.Level, Integer, UtilityGolem> golemFinder;

    public static class FabricBridge {
        @FunctionalInterface
        public interface TriConsumer<T, U, V> {
            void accept(T t, U u, V v);
        }

        public static void registerC2S(Object id, Object codec) {
            try {
                Class<?> registryClass = Class.forName("net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry");
                Method playC2S;
                try {
                    playC2S = findMethod(registryClass, "playC2S", 0);
                } catch (Exception e) {
                    playC2S = findMethod(registryClass, "serverboundPlay", 0);
                }
                Object playC2SObj = playC2S.invoke(null);
                findMethod(playC2SObj.getClass(), "register", 2).invoke(playC2SObj, id, codec);
            } catch (Exception e) { e.printStackTrace(); }
        }

        public static void registerS2C(Object id, Object codec) {
            try {
                Class<?> registryClass = Class.forName("net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry");
                Method playS2C;
                try {
                    playS2C = findMethod(registryClass, "playS2C", 0);
                } catch (Exception e) {
                    playS2C = findMethod(registryClass, "clientboundPlay", 0);
                }
                Object playS2CObj = playS2C.invoke(null);
                findMethod(playS2CObj.getClass(), "register", 2).invoke(playS2CObj, id, codec);
            } catch (Exception e) { e.printStackTrace(); }
        }

        public static void registerReceiver(Object id, BiConsumer<Object, Object> handler) {
            try {
                Class<?> receiverClass = Class.forName("net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking");
                Class<?> handlerClass = Class.forName("net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking$PlayPayloadHandler");
                Object proxy = Proxy.newProxyInstance(handlerClass.getClassLoader(), new Class[]{handlerClass}, (p, method, args) -> {
                    if (method.getName().equals("receive")) {
                        handler.accept(args[0], args[1]);
                    }
                    return null;
                });
                try {
                    findMethod(receiverClass, "registerGlobalReceiver", 2).invoke(null, id, proxy);
                } catch (Exception e) {
                    // Try to find if it's named differently in some versions
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        public static void modifyEntries(Object tab, Consumer<Object> consumer) {
            try {
                Class<?> eventsClass = Class.forName("net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents");
                Method modifyEntriesEvent = null;
                try {
                    modifyEntriesEvent = findMethod(eventsClass, "modifyEntriesEvent", 1);
                } catch (Exception e) {
                    // Try to find if it's named differently or has different parameters in some versions
                }
                if (modifyEntriesEvent == null) return;
                Object event = modifyEntriesEvent.invoke(null, tab);
                Class<?> modifyEntriesClass = Class.forName("net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents$ModifyEntries");
                Object proxy = Proxy.newProxyInstance(modifyEntriesClass.getClassLoader(), new Class[]{modifyEntriesClass}, (p, method, args) -> {
                    if (method.getName().equals("modify")) {
                        consumer.accept(args[0]);
                    }
                    return null;
                });
                Method registerMethod = findMethod(event.getClass(), "register", 1);
                registerMethod.setAccessible(true);
                registerMethod.invoke(event, proxy);
            } catch (Exception e) { 
                // Silently fail for now if class is missing to allow the rest of the mod to load
            }
        }

        public static void sendToPlayer(Object player, Object payload) {
            try {
                Class<?> networkingClass = Class.forName("net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking");
                findMethod(networkingClass, "send", 2).invoke(null, player, payload);
            } catch (Exception e) { e.printStackTrace(); }
        }

        public static Collection<ServerPlayer> getTrackingPlayers(Entity entity) {
            try {
                Class<?> lookupClass = Class.forName("net.fabricmc.fabric.api.networking.v1.PlayerLookup");
                return (Collection<ServerPlayer>) findMethod(lookupClass, "tracking", 1).invoke(null, entity);
            } catch (Exception e) { e.printStackTrace(); return java.util.Collections.emptyList(); }
        }

        public static void registerAttributes(EntityType<? extends LivingEntity> type, net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder attributes) {
            net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(type, attributes);
        }

        public static <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BiFunction<BlockPos, BlockState, T> factory, net.minecraft.world.level.block.Block... blocks) {
            try {
                Class<?> builderClass = Class.forName("net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder");
                Class<?> factoryClass = Class.forName("net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder$Factory");
                Object factoryProxy = Proxy.newProxyInstance(factoryClass.getClassLoader(), new Class[]{factoryClass}, (p, method, args) -> {
                    if (method.getName().equals("create")) {
                        return factory.apply((BlockPos) args[0], (BlockState) args[1]);
                    }
                    return null;
                });
                Object builder = findMethod(builderClass, "create", 2).invoke(null, factoryProxy, blocks);
                return (BlockEntityType<T>) findMethod(builderClass, "build", 0).invoke(builder);
            } catch (Exception e) { e.printStackTrace(); return null; }
        }

        public static void registerCommand(TriConsumer<Object, Object, Object> handler) {
            try {
                CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
                    handler.accept(dispatcher, registryAccess, environment);
                });
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

    public static final MenuType<GolemInventoryMenu> GOLEM_SCREEN_HANDLER_TYPE =
            Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(MOD_ID, "golem_inventory"), new MenuType<>(GolemInventoryMenu::new, FeatureFlags.VANILLA_SET));

    public static final MenuType<GolemFurnaceMenu> GOLEM_FURNACE_HANDLER =
            Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(MOD_ID, "golem_furnace"), new MenuType<>(GolemFurnaceMenu::new, FeatureFlags.VANILLA_SET));

    public static final MenuType<GolemJukeboxMenu> GOLEM_JUKEBOX_HANDLER =
            Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(MOD_ID, "golem_jukebox"), new MenuType<>(GolemJukeboxMenu::new, FeatureFlags.VANILLA_SET));

    public static record GolemChestPayload(BlockPos pos, boolean golemDead, int inventorySize) implements CustomPacketPayload {
        public static final Type<GolemChestPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "golem_chest_payload"));
        public static final StreamCodec<RegistryFriendlyByteBuf, GolemChestPayload> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, GolemChestPayload::pos,
                ByteBufCodecs.BOOL, GolemChestPayload::golemDead,
                ByteBufCodecs.VAR_INT, GolemChestPayload::inventorySize,
                GolemChestPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public static final MenuType<GolemChestMenu> GOLEM_CHEST_HANDLER =
            Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(MOD_ID, "golem_chest"), new MenuType<>((syncId, playerInventory) -> {
                return new GolemChestMenu(syncId, playerInventory);
            }, FeatureFlags.VANILLA_SET));

    public static final MenuType<RedstoneGolemMenu> REDSTONE_GOLEM_HANDLER =
            Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(MOD_ID, "redstone_golem"), new MenuType<>((syncId, playerInventory) -> {
                return new RedstoneGolemMenu(syncId, playerInventory);
            }, FeatureFlags.VANILLA_SET));



    @Override
    public void onInitialize() {
        LOGGER.info("Utility Golems initializing...");
        ConfigManager.load();
        UGBlocks.register();
        UGItems.register();
        UGItems.registerSpawnEggs();

        FabricBridge.registerC2S(SyncPatternPayload.ID, SyncPatternPayload.CODEC);
        FabricBridge.registerC2S(SelectBuyItemPayload.ID, SelectBuyItemPayload.CODEC);
        FabricBridge.registerC2S(JukeboxActionPayload.ID, JukeboxActionPayload.CODEC);
        FabricBridge.registerC2S(RedstoneActionPayload.ID, RedstoneActionPayload.CODEC);
        FabricBridge.registerC2S(SyncRedstoneProgramPayload.ID, SyncRedstoneProgramPayload.CODEC);
        FabricBridge.registerC2S(ClearCactusSlotPayload.ID, ClearCactusSlotPayload.CODEC);
        FabricBridge.registerS2C(SyncDiscoveredTradesPayload.ID, SyncDiscoveredTradesPayload.CODEC);
        FabricBridge.registerS2C(OpenGolemInventoryPayload.ID, OpenGolemInventoryPayload.CODEC);
        FabricBridge.registerS2C(GolemChestPayload.ID, GolemChestPayload.CODEC);

        FabricBridge.registerReceiver(SelectBuyItemPayload.ID, (payload, context) -> {
            try {
                Method serverMethod = context.getClass().getMethod("server");
                net.minecraft.server.MinecraftServer server = (net.minecraft.server.MinecraftServer) serverMethod.invoke(context);
                server.execute(() -> {
                    Entity entity = findEntityById(server, ((SelectBuyItemPayload)payload).entityId());
                    if (entity instanceof UtilityGolem golem) {
                        golem.setSelectedBuyItem(((SelectBuyItemPayload)payload).selectedItem());
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        });

        FabricBridge.registerReceiver(JukeboxActionPayload.ID, (payload, context) -> {
            try {
                Method serverMethod = context.getClass().getMethod("server");
                net.minecraft.server.MinecraftServer server = (net.minecraft.server.MinecraftServer) serverMethod.invoke(context);
                server.execute(() -> {
                    Entity entity = findEntityById(server, ((JukeboxActionPayload)payload).entityId());
                    if (entity instanceof UtilityGolem golem) {
                        switch (((JukeboxActionPayload)payload).actionId()) {
                            case 0 -> {
                                boolean playing = !golem.isJukeboxPlaying();
                                golem.setJukeboxPlaying(playing);
                                if (!playing) {
                                    golem.stopJukebox();
                                }
                            }
                            case 1 -> golem.setJukeboxShuffle(!golem.isJukeboxShuffle());
                            case 2 -> golem.setJukeboxRepeat(!golem.isJukeboxRepeat());
                        }
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        });

        FabricBridge.registerReceiver(RedstoneActionPayload.ID, (payload, context) -> {
            try {
                Method serverMethod = context.getClass().getMethod("server");
                net.minecraft.server.MinecraftServer server = (net.minecraft.server.MinecraftServer) serverMethod.invoke(context);
                server.execute(() -> {
                    Entity entity = findEntityById(server, ((RedstoneActionPayload)payload).entityId());
                    if (entity instanceof UtilityGolem golem) {
                        switch (((RedstoneActionPayload)payload).actionId()) {
                            case 0 -> golem.setRedstoneProgramStarted(!golem.isRedstoneProgramStarted());
                            case 1 -> {
                                golem.setRedstoneProgramStarted(false);
                                golem.setCurrentInteractionIndex(0);
                                golem.setRedstoneTickCounter(0);
                            }
                        }
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        });

        FabricBridge.registerReceiver(SyncRedstoneProgramPayload.ID, (payload, context) -> {
            try {
                Method serverMethod = context.getClass().getMethod("server");
                net.minecraft.server.MinecraftServer server = (net.minecraft.server.MinecraftServer) serverMethod.invoke(context);
                server.execute(() -> {
                    Entity entity = findEntityById(server, ((SyncRedstoneProgramPayload)payload).entityId());
                    if (entity instanceof UtilityGolem golem) {
                        golem.setRedstoneProgram(((SyncRedstoneProgramPayload)payload).program());
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        });

        FabricBridge.registerReceiver(ClearCactusSlotPayload.ID, (payload, context) -> {
            try {
                Method serverMethod = context.getClass().getMethod("server");
                net.minecraft.server.MinecraftServer server = (net.minecraft.server.MinecraftServer) serverMethod.invoke(context);
                server.execute(() -> {
                    Entity entity = findEntityById(server, ((ClearCactusSlotPayload)payload).entityId());
                    if (entity instanceof UtilityGolem golem && golem.getGolemType() == GolemType.CACTUS) {
                        if (((ClearCactusSlotPayload)payload).slotIndex() >= 0 && ((ClearCactusSlotPayload)payload).slotIndex() < golem.getInventory().getContainerSize()) {
                            golem.getInventory().setItem(((ClearCactusSlotPayload)payload).slotIndex(), ItemStack.EMPTY);
                        }
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        });

        FabricBridge.registerReceiver(SyncPatternPayload.ID, (payload, context) -> {
            try {
                Method serverMethod = context.getClass().getMethod("server");
                net.minecraft.server.MinecraftServer server = (net.minecraft.server.MinecraftServer) serverMethod.invoke(context);
                server.execute(() -> {
                    Entity entity = findEntityById(server, ((SyncPatternPayload)payload).entityId());
                    if (entity instanceof UtilityGolem golem) {
                        SyncPatternPayload p = (SyncPatternPayload)payload;
                        BuildPattern pattern = BuildPattern.values()[p.patternOrdinal()];
                        golem.setBuildPattern(pattern);
                        golem.setWallWidth(p.width());
                        golem.setWallLength(p.length());
                        golem.setBuildingStarted(p.started());
                        if (!p.filter().isEmpty()) {
                            golem.setHeldItem(p.filter());
                        }
                        if (p.schematicName() != null) {
                            golem.setSchematicName(p.schematicName());
                        }
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        });


        /// REGISTER DEBUG COMMANDS
        FabricBridge.registerCommand((dispatcher, registryAccess, environment) -> {
            ((com.mojang.brigadier.CommandDispatcher) (Object) dispatcher).register(
                    Commands.literal("golem")
                            .then(Commands.argument("type", com.mojang.brigadier.arguments.StringArgumentType.word())
                                    .suggests((c, builder) -> {
                                        for (GolemType gt : GolemType.values()) {
                                            builder.suggest(gt.getName());
                                        }
                                        return builder.buildFuture();
                                    })
                                    .then(Commands.argument("equipped", com.mojang.brigadier.arguments.BoolArgumentType.bool())
                                            .executes(c -> {
                                                CommandSourceStack source = (CommandSourceStack) (Object) c.getSource();
                                                ServerPlayer player = source.getPlayer();
                                                if (player == null) {
                                                    source.sendFailure(Component.literal("Command can only be used by players."));
                                                    return 0;
                                                }
                                                
                                                String typeName = com.mojang.brigadier.arguments.StringArgumentType.getString(c, "type");
                                                GolemType foundType = null;
                                                for (GolemType gt : GolemType.values()) {
                                                    if (gt.getName().equalsIgnoreCase(typeName)) {
                                                        foundType = gt;
                                                        break;
                                                    }
                                                }
                                                if (foundType == null) {
                                                    source.sendFailure(Component.literal("Unknown golem type: " + typeName));
                                                    return 0;
                                                }
                                                final GolemType type = foundType;

                                                boolean equipped = com.mojang.brigadier.arguments.BoolArgumentType.getBool(c, "equipped");

                                                ServerLevel world = source.getLevel();
                                                EntityType<UtilityGolem> entityType = GOLEM_TYPES.get(type);
                                                if (entityType == null) {
                                                    source.sendFailure(Component.literal("Golem type not registered: " + type.getName()));
                                                    return 0;
                                                }

                                                UtilityGolem golem = new UtilityGolem(entityType, world, type);
                                                golem.snapTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
                                                
                                                if (equipped) {
                                                    final ItemStack equipment = getStandardEquipment(type);
                                                    if (!equipment.isEmpty()) {
                                                        golem.setHeldItem(equipment);
                                                    }
                                                    // Give them some extra in getInventory() just in case
                                                    golem.getInventory().addItem(equipment.copy());
                                                }
                                                world.addFreshEntity(golem);
                                                source.sendSuccess(() -> Component.literal("Summoned " + type.getFriendlyName() + (equipped ? " (Equipped)" : "")), true);
                                                return 1;
                                            })
                                    )
                            )
            );
        });

        FabricBridge.modifyEntries(CreativeModeTabs.SPAWN_EGGS, entries -> {
            try {
                Method acceptMethod = entries.getClass().getMethod("accept", net.minecraft.world.level.ItemLike.class);
                for (GolemType type : GolemType.values()) {
                    acceptMethod.invoke(entries, UGItems.GOLEM_SPAWN_EGGS.get(type));
                }
            } catch (Exception e) { e.printStackTrace(); }
        });

        FabricBridge.modifyEntries(CreativeModeTabs.TOOLS_AND_UTILITIES, entries -> {
            try {
                Method acceptMethod = entries.getClass().getMethod("accept", net.minecraft.world.level.ItemLike.class);
                acceptMethod.invoke(entries, UGItems.WRENCH_ITEM);
            } catch (Exception e) { e.printStackTrace(); }
        });

        FabricBridge.modifyEntries(CreativeModeTabs.FUNCTIONAL_BLOCKS, entries -> {
            try {
                Method acceptMethod = entries.getClass().getMethod("accept", net.minecraft.world.level.ItemLike.class);
                for (GolemType type : GolemType.values()) {
                    if (type == GolemType.LAMP || type == GolemType.FURNACE || type == GolemType.JUKEBOX || type == GolemType.SMOKER || type == GolemType.BLAST_FURNACE || type == GolemType.MEDIC || type == GolemType.CACTUS) continue;
                    net.minecraft.world.level.block.Block chest = UGBlocks.GOLEM_CHESTS.get(type);
                    if (chest != null) {
                        acceptMethod.invoke(entries, chest);
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        });

        for (GolemType type : GolemType.values()) {
            Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, type.getName());
            EntityType<UtilityGolem> entityType = EntityType.Builder.of(
                            (EntityType.EntityFactory<UtilityGolem>) (Object) ((EntityType.EntityFactory<UtilityGolem>) (et, world) -> new UtilityGolem(et, world, type)),
                            MobCategory.CREATURE
                    )
                    .sized(0.6F, 1.8F)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id));

            GOLEM_TYPES.put(type, entityType);
            FabricBridge.registerAttributes(entityType, type.getAttributes());
            Registry.register(BuiltInRegistries.ENTITY_TYPE, id, entityType);
        }
        LOGGER.info("Utility Golems registered: " + GOLEM_TYPES.keySet());
    }


    public static void syncDiscoveredTrades(UtilityGolem golem) {
        if (!golem.level().isClientSide() && golem.level() instanceof ServerLevel) {
            SyncDiscoveredTradesPayload payload = new SyncDiscoveredTradesPayload(golem.getId(), golem.getDiscoveredTrades());
            for (ServerPlayer player : FabricBridge.getTrackingPlayers(golem)) {
                FabricBridge.sendToPlayer(player, payload);
            }
        }
    }

    private static ItemStack getStandardEquipment(GolemType type) {
        return switch (type) {
            case LAPIS -> new ItemStack(Items.DIAMOND_PICKAXE);
            case REDSTONE -> new ItemStack(Items.REDSTONE_BLOCK);
            case EMERALD -> new ItemStack(Items.EMERALD);
            case GOLD -> new ItemStack(Items.GOLD_INGOT);
            case AMETHYST -> new ItemStack(Items.GOLDEN_APPLE);
            case NETHERITE, ANCIENT -> new ItemStack(Items.NETHERITE_SWORD);
            case FURNACE, SMOKER, BLAST_FURNACE -> new ItemStack(Items.COAL, 64);
            case BAMBOO -> new ItemStack(Items.DIAMOND_HOE);
            case DIAMOND -> new ItemStack(Items.DIAMOND);
            case SPONGE -> new ItemStack(Items.FISHING_ROD);
            case DEEPSLATE -> new ItemStack(Items.DIAMOND_AXE);
            case JUKEBOX -> new ItemStack(Items.MUSIC_DISC_CAT);
            case LAMP -> new ItemStack(Items.TORCH, 64);
            case NETHER_WART -> new ItemStack(Items.GLASS_BOTTLE, 64);
            case MEDIC, CACTUS, HOPPER -> new ItemStack(UGItems.WRENCH_ITEM);
            case HONEYCOMB -> new ItemStack(Items.SHEARS);
            case TINTED_GLASS -> new ItemStack(Items.GLASS_BOTTLE, 64);
        };
    }
}
