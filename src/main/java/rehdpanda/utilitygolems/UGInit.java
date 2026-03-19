package rehdpanda.utilitygolems;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UGInit implements ModInitializer {

    public static final String MOD_ID = "utility-golems";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public record SyncPatternPayload(int entityId, int patternOrdinal, int width, int length, ItemStack filter, boolean started, String schematicName) implements CustomPayload {
        public static final Id<SyncPatternPayload> ID = new Id<>(Identifier.of(MOD_ID, "sync_pattern"));
        public static final PacketCodec<RegistryByteBuf, SyncPatternPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, SyncPatternPayload::entityId,
                PacketCodecs.VAR_INT, SyncPatternPayload::patternOrdinal,
                PacketCodecs.VAR_INT, SyncPatternPayload::width,
                PacketCodecs.VAR_INT, SyncPatternPayload::length,
                ItemStack.OPTIONAL_PACKET_CODEC, SyncPatternPayload::filter,
                PacketCodecs.BOOLEAN, SyncPatternPayload::started,
                PacketCodecs.STRING, SyncPatternPayload::schematicName,
                SyncPatternPayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record SyncDiscoveredTradesPayload(int entityId, List<ItemStack> trades) implements CustomPayload {
        public static final Id<SyncDiscoveredTradesPayload> ID = new Id<>(Identifier.of(MOD_ID, "sync_discovered_trades"));
        public static final PacketCodec<RegistryByteBuf, SyncDiscoveredTradesPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, SyncDiscoveredTradesPayload::entityId,
                ItemStack.PACKET_CODEC.collect(PacketCodecs.toList()), SyncDiscoveredTradesPayload::trades,
                SyncDiscoveredTradesPayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record SelectBuyItemPayload(int entityId, ItemStack selectedItem) implements CustomPayload {
        public static final Id<SelectBuyItemPayload> ID = new Id<>(Identifier.of(MOD_ID, "select_buy_item"));
        public static final PacketCodec<RegistryByteBuf, SelectBuyItemPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, SelectBuyItemPayload::entityId,
                ItemStack.OPTIONAL_PACKET_CODEC, SelectBuyItemPayload::selectedItem,
                SelectBuyItemPayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record JukeboxActionPayload(int entityId, int actionId) implements CustomPayload {
        public static final Id<JukeboxActionPayload> ID = new Id<>(Identifier.of(MOD_ID, "jukebox_action"));
        public static final PacketCodec<RegistryByteBuf, JukeboxActionPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, JukeboxActionPayload::entityId,
                PacketCodecs.VAR_INT, JukeboxActionPayload::actionId,
                JukeboxActionPayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record RedstoneActionPayload(int entityId, int actionId) implements CustomPayload {
        public static final Id<RedstoneActionPayload> ID = new Id<>(Identifier.of(MOD_ID, "redstone_action"));
        public static final PacketCodec<RegistryByteBuf, RedstoneActionPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, RedstoneActionPayload::entityId,
                PacketCodecs.VAR_INT, RedstoneActionPayload::actionId,
                RedstoneActionPayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record SyncRedstoneProgramPayload(int entityId, List<UtilityGolem.RedstoneInteraction> program) implements CustomPayload {
        public static final Id<SyncRedstoneProgramPayload> ID = new Id<>(Identifier.of(MOD_ID, "sync_redstone_program"));
        public static final PacketCodec<RegistryByteBuf, SyncRedstoneProgramPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, SyncRedstoneProgramPayload::entityId,
                PacketCodec.tuple(
                        BlockPos.PACKET_CODEC, UtilityGolem.RedstoneInteraction::pos,
                        PacketCodecs.VAR_INT, UtilityGolem.RedstoneInteraction::interval,
                        UtilityGolem.RedstoneInteraction::new
                ).collect(PacketCodecs.toList()), SyncRedstoneProgramPayload::program,
                SyncRedstoneProgramPayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record ClearCactusSlotPayload(int entityId, int slotIndex) implements CustomPayload {
        public static final Id<ClearCactusSlotPayload> ID = new Id<>(Identifier.of(MOD_ID, "clear_cactus_slot"));
        public static final PacketCodec<RegistryByteBuf, ClearCactusSlotPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, ClearCactusSlotPayload::entityId,
                PacketCodecs.VAR_INT, ClearCactusSlotPayload::slotIndex,
                ClearCactusSlotPayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static final Map<GolemType, EntityType<UtilityGolem>> GOLEM_TYPES = new HashMap<>();

    public static final ScreenHandlerType<GolemInventoryScreenHandler> GOLEM_SCREEN_HANDLER_TYPE =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "golem_inventory"), new ExtendedScreenHandlerType<>(
                    (syncId, playerInventory, entityId) -> {
                        Entity entity = playerInventory.player.getEntityWorld().getEntityById(entityId);
                        if (entity instanceof UtilityGolem golem) {
                            return new GolemInventoryScreenHandler(syncId, playerInventory, golem.getInventory(), golem);
                        }
                        return new GolemInventoryScreenHandler(syncId, playerInventory);
                    }, PacketCodecs.INTEGER
            ));
    public static final ScreenHandlerType<GolemFurnaceScreenHandler> GOLEM_FURNACE_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "golem_furnace"), new ScreenHandlerType<>(GolemFurnaceScreenHandler::new, FeatureSet.empty()));

    public static final ScreenHandlerType<GolemJukeboxScreenHandler> GOLEM_JUKEBOX_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "golem_jukebox"), new ExtendedScreenHandlerType<>(
                    (syncId, playerInventory, entityId) -> {
                        Entity entity = playerInventory.player.getEntityWorld().getEntityById(entityId);
                        if (entity instanceof UtilityGolem golem) {
                            return new GolemJukeboxScreenHandler(syncId, playerInventory, golem.getJukeboxInventory(), golem);
                        }
                        return new GolemJukeboxScreenHandler(syncId, playerInventory);
                    }, PacketCodecs.INTEGER
            ));

    public static record GolemChestPayload(BlockPos pos, boolean golemDead, int inventorySize) implements CustomPayload {
        public static final Id<GolemChestPayload> ID = new Id<>(Identifier.of(MOD_ID, "golem_chest_payload"));
        public static final PacketCodec<RegistryByteBuf, GolemChestPayload> CODEC = PacketCodec.tuple(
                BlockPos.PACKET_CODEC, GolemChestPayload::pos,
                PacketCodecs.BOOLEAN, GolemChestPayload::golemDead,
                PacketCodecs.VAR_INT, GolemChestPayload::inventorySize,
                GolemChestPayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static final ScreenHandlerType<GolemChestScreenHandler> GOLEM_CHEST_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "golem_chest"), new ExtendedScreenHandlerType<>(
                    (syncId, playerInventory, payload) -> {
                        return new GolemChestScreenHandler(syncId, playerInventory, new SimpleInventory(payload.inventorySize()), payload.pos(), payload.golemDead());
                    }, GolemChestPayload.CODEC
            ));

    public static final ScreenHandlerType<RedstoneGolemScreenHandler> REDSTONE_GOLEM_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "redstone_golem"), new ExtendedScreenHandlerType<>(
                    (syncId, playerInventory, entityId) -> {
                        Entity entity = playerInventory.player.getEntityWorld().getEntityById(entityId);
                        if (entity instanceof UtilityGolem golem) {
                            return new RedstoneGolemScreenHandler(syncId, playerInventory, golem);
                        }
                        return new RedstoneGolemScreenHandler(syncId, playerInventory);
                    }, PacketCodecs.INTEGER
            ));



    @Override
    public void onInitialize() {
        LOGGER.info("Utility Golems initializing...");
        ConfigManager.load();
        UGBlocks.register();
        UGItems.register();
        UGItems.registerSpawnEggs();

        PayloadTypeRegistry.playC2S().register(SyncPatternPayload.ID, SyncPatternPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SelectBuyItemPayload.ID, SelectBuyItemPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(JukeboxActionPayload.ID, JukeboxActionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RedstoneActionPayload.ID, RedstoneActionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SyncRedstoneProgramPayload.ID, SyncRedstoneProgramPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ClearCactusSlotPayload.ID, ClearCactusSlotPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncDiscoveredTradesPayload.ID, SyncDiscoveredTradesPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GolemChestPayload.ID, GolemChestPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SelectBuyItemPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                Entity entity = context.player().getEntityWorld().getEntityById(payload.entityId());
                if (entity instanceof UtilityGolem golem) {
                    golem.setSelectedBuyItem(payload.selectedItem());
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(JukeboxActionPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                Entity entity = context.player().getEntityWorld().getEntityById(payload.entityId());
                if (entity instanceof UtilityGolem golem) {
                    switch (payload.actionId()) {
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
        });

        ServerPlayNetworking.registerGlobalReceiver(RedstoneActionPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                Entity entity = context.player().getEntityWorld().getEntityById(payload.entityId());
                if (entity instanceof UtilityGolem golem) {
                    switch (payload.actionId()) {
                        case 0 -> golem.setRedstoneProgramStarted(!golem.isRedstoneProgramStarted());
                        case 1 -> {
                            golem.setRedstoneProgramStarted(false);
                            golem.setCurrentInteractionIndex(0);
                            golem.setRedstoneTickCounter(0);
                        }
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(SyncRedstoneProgramPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                Entity entity = context.player().getEntityWorld().getEntityById(payload.entityId());
                if (entity instanceof UtilityGolem golem) {
                    golem.setRedstoneProgram(payload.program());
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(ClearCactusSlotPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                Entity entity = context.player().getEntityWorld().getEntityById(payload.entityId());
                if (entity instanceof UtilityGolem golem && golem.getGolemType() == GolemType.CACTUS) {
                    if (payload.slotIndex() >= 0 && payload.slotIndex() < golem.getInventory().size()) {
                        golem.getInventory().setStack(payload.slotIndex(), ItemStack.EMPTY);
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(SyncPatternPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                Entity entity = context.player().getEntityWorld().getEntityById(payload.entityId());
                if (entity instanceof UtilityGolem golem) {
                    BuildPattern pattern = BuildPattern.values()[payload.patternOrdinal()];
                    golem.setBuildPattern(pattern);
                    golem.setWallWidth(payload.width());
                    golem.setWallLength(payload.length());
                    golem.setBuildingStarted(payload.started());
                    if (!payload.filter().isEmpty()) {
                        golem.setHeldItem(payload.filter());
                    }
                    if (payload.schematicName() != null) {
                        golem.setSchematicName(payload.schematicName());
                    }
                    context.player().sendMessage(Text.literal("Golem mode set to: " + pattern.getDisplayName() + (payload.started() ? " (Started)" : " (Stopped)") + (golem.getSchematicName().isEmpty() ? "" : (" | Schematic: " + golem.getSchematicName()))), true);
                }
            });
        });


        /// REGISTER DEBUG COMMANDS
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("golem")
                            .then(CommandManager.argument("type", com.mojang.brigadier.arguments.StringArgumentType.word())
                                    .suggests((context, builder) -> {
                                        for (GolemType gt : GolemType.values()) {
                                            builder.suggest(gt.getName());
                                        }
                                        return builder.buildFuture();
                                    })
                                    .then(CommandManager.argument("equipped", BoolArgumentType.bool())
                                            .executes(context -> {
                                                ServerCommandSource source = (ServerCommandSource) context.getSource();
                                                ServerPlayerEntity player = source.getPlayer();
                                                if (player == null) {
                                                    source.sendError(Text.literal("Command can only be used by players."));
                                                    return 0;
                                                }
                                                
                                                String typeName = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "type");
                                                GolemType foundType = null;
                                                for (GolemType gt : GolemType.values()) {
                                                    if (gt.getName().equalsIgnoreCase(typeName)) {
                                                        foundType = gt;
                                                        break;
                                                    }
                                                }
                                                if (foundType == null) {
                                                    source.sendError(Text.literal("Unknown golem type: " + typeName));
                                                    return 0;
                                                }
                                                final GolemType type = foundType;

                                                boolean equipped = BoolArgumentType.getBool(context, "equipped");

                                                ServerWorld world = source.getWorld();
                                                EntityType<UtilityGolem> entityType = GOLEM_TYPES.get(type);
                                                if (entityType == null) {
                                                    source.sendError(Text.literal("Golem type not registered: " + type.getName()));
                                                    return 0;
                                                }

                                                UtilityGolem golem = new UtilityGolem(entityType, world, type);
                                                golem.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), 0.0F);
                                                
                                                if (equipped) {
                                                    final ItemStack equipment = getStandardEquipment(type);
                                                    if (!equipment.isEmpty()) {
                                                        golem.setHeldItem(equipment);
                                                    }
                                                    // Give them some extra in inventory just in case
                                                    golem.getInventory().addStack(equipment.copy());
                                                }
                                                world.spawnEntity(golem);
                                                source.sendFeedback(() -> Text.literal("Summoned " + type.getFriendlyName() + (equipped ? " (Equipped)" : "")), true);
                                                return 1;
                                            })
                                    )
                            )
            );
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
            for (GolemType type : GolemType.values()) {
                entries.add(UGItems.GOLEM_SPAWN_EGGS.get(type));
            }
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(UGItems.WRENCH_ITEM);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            for (GolemType type : GolemType.values()) {
                if (type == GolemType.LAMP || type == GolemType.FURNACE || type == GolemType.JUKEBOX || type == GolemType.SMOKER || type == GolemType.BLAST_FURNACE || type == GolemType.MEDIC || type == GolemType.CACTUS) continue;
                net.minecraft.block.Block chest = UGBlocks.GOLEM_CHESTS.get(type);
                if (chest != null) {
                    entries.add(chest);
                }
            }
        });

        // Register all golem types
        for (GolemType type : GolemType.values()) {
            // Create the entity type
            EntityType<UtilityGolem> entityType = FabricEntityTypeBuilder.create(
                            SpawnGroup.CREATURE,
                            (EntityType<UtilityGolem> et, net.minecraft.world.World world) -> new UtilityGolem(et, world, type)
                    )
                    .dimensions(EntityDimensions.fixed(0.6F, 1.8F))
                    .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, type.getName())));


            GOLEM_TYPES.put(type, entityType);

            // Register attributes
            FabricDefaultAttributeRegistry.register(entityType, type.getAttributes());

            // Register in the global registry
            Registry.register(
                    Registries.ENTITY_TYPE,
                    Identifier.of(MOD_ID, type.getName()),
                    entityType
            );
        }
        LOGGER.info("Utility Golems registered: " + GOLEM_TYPES.keySet());
    }


    public static void syncDiscoveredTrades(UtilityGolem golem) {
        if (!golem.getEntityWorld().isClient() && golem.getEntityWorld() instanceof ServerWorld) {
            SyncDiscoveredTradesPayload payload = new SyncDiscoveredTradesPayload(golem.getId(), golem.getDiscoveredTrades());
            for (ServerPlayerEntity player : net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(golem)) {
                ServerPlayNetworking.send(player, payload);
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
