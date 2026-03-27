// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiClass
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
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityMobSpawnType;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UGInit implements ModInitializer {

    public static final String MOD_ID = "utility-golems";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public record SyncPatternPayload(int entityId, int patternOrdinal, int width, int length, ItemStack filter, boolean started, String schematicName) implements CustomPacketPayload {
        public static final Type<SyncPatternPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "sync_pattern"));
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
        public static final Type<SyncDiscoveredTradesPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "sync_discovered_trades"));
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

    public record SelectBuyItemPayload(int entityId, ItemStack selectedItem) implements CustomPacketPayload {
        public static final Type<SelectBuyItemPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "select_buy_item"));
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
        public static final Type<JukeboxActionPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "jukebox_action"));
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
        public static final Type<RedstoneActionPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "redstone_action"));
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
        public static final Type<SyncRedstoneProgramPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "sync_redstone_program"));
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
        public static final Type<ClearCactusSlotPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "clear_cactus_slot"));
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

    public static final MenuType<GolemInventoryScreenHandler> GOLEM_SCREEN_HANDLER_TYPE =
            Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(MOD_ID, "golem_inventory"), new ExtendedScreenHandlerType<>(
                    (syncId, playerInventory, entityId) -> {
                        Entity entity = playerInventory.player.level().getEntity(entityId);
                        if (entity instanceof UtilityGolem golem) {
                            return new GolemInventoryScreenHandler(syncId, playerInventory, golem.getInventory(), golem);
                        }
                        return new GolemInventoryScreenHandler(syncId, playerInventory);
                    }, ByteBufCodecs.INT
            ));
    public static final MenuType<GolemFurnaceScreenHandler> GOLEM_FURNACE_HANDLER =
            Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(MOD_ID, "golem_furnace"), new MenuType<>(GolemFurnaceScreenHandler::new, FeatureFlagSet.of()));

    public static final MenuType<GolemJukeboxScreenHandler> GOLEM_JUKEBOX_HANDLER =
            Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(MOD_ID, "golem_jukebox"), new ExtendedScreenHandlerType<>(
                    (syncId, playerInventory, entityId) -> {
                        Entity entity = playerInventory.player.level().getEntity(entityId);
                        if (entity instanceof UtilityGolem golem) {
                            return new GolemJukeboxScreenHandler(syncId, playerInventory, golem.getJukeboxInventory(), golem);
                        }
                        return new GolemJukeboxScreenHandler(syncId, playerInventory);
                    }, ByteBufCodecs.INT
            ));

    public static record GolemChestPayload(BlockPos pos, boolean golemDead, int inventorySize) implements CustomPacketPayload {
        public static final Type<GolemChestPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "golem_chest_payload"));
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

    public static final MenuType<GolemChestScreenHandler> GOLEM_CHEST_SCREEN_HANDLER =
            Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(MOD_ID, "golem_chest"), new ExtendedScreenHandlerType<>(
                    (syncId, playerInventory, payload) -> {
                        return new GolemChestScreenHandler(syncId, playerInventory, new SimpleContainer(payload.inventorySize()), payload.pos(), payload.golemDead());
                    }, GolemChestPayload.CODEC
            ));

    public static final MenuType<RedstoneGolemScreenHandler> REDSTONE_GOLEM_HANDLER =
            Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(MOD_ID, "redstone_golem"), new ExtendedScreenHandlerType<>(
                    (syncId, playerInventory, entityId) -> {
                        Entity entity = playerInventory.player.level().getEntity(entityId);
                        if (entity instanceof UtilityGolem golem) {
                            return new RedstoneGolemScreenHandler(syncId, playerInventory, golem);
                        }
                        return new RedstoneGolemScreenHandler(syncId, playerInventory);
                    }, ByteBufCodecs.INT
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
                Entity entity = context.player().level().getEntity(payload.entityId());
                if (entity instanceof UtilityGolem golem) {
                    golem.setSelectedBuyItem(payload.selectedItem());
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(JukeboxActionPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                Entity entity = context.player().level().getEntity(payload.entityId());
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
                Entity entity = context.player().level().getEntity(payload.entityId());
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
                Entity entity = context.player().level().getEntity(payload.entityId());
                if (entity instanceof UtilityGolem golem) {
                    golem.setRedstoneProgram(payload.program());
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(ClearCactusSlotPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                Entity entity = context.player().level().getEntity(payload.entityId());
                if (entity instanceof UtilityGolem golem && golem.getGolemType() == GolemType.CACTUS) {
                    if (payload.slotIndex() >= 0 && payload.slotIndex() < golem.getInventory().getContainerSize()) {
                        golem.getInventory().setItem(payload.slotIndex(), ItemStack.EMPTY);
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(SyncPatternPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                Entity entity = context.player().level().getEntity(payload.entityId());
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
                    context.player().displayClientMessage(Component.literal("Golem mode set to: " + pattern.getDisplayName() + (payload.started() ? " (Started)" : " (Stopped)") + (golem.getSchematicName().isEmpty() ? "" : (" | Schematic: " + golem.getSchematicName()))), true);
                }
            });
        });


        /// REGISTER DEBUG COMMANDS
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    Commands.literal("golem")
                            .then(Commands.argument("type", com.mojang.brigadier.arguments.StringArgumentType.word())
                                    .suggests((context, builder) -> {
                                        for (GolemType gt : GolemType.values()) {
                                            builder.suggest(gt.getName());
                                        }
                                        return builder.buildFuture();
                                    })
                                    .then(Commands.argument("equipped", BoolArgumentType.bool())
                                            .executes(context -> {
                                                CommandSourceStack source = (CommandSourceStack) context.getSource();
                                                ServerPlayer player = source.getPlayer();
                                                if (player == null) {
                                                    source.sendFailure(Component.literal("Command can only be used by players."));
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
                                                    source.sendFailure(Component.literal("Unknown golem type: " + typeName));
                                                    return 0;
                                                }
                                                final GolemType type = foundType;

                                                boolean equipped = BoolArgumentType.getBool(context, "equipped");

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
                                                    // Give them some extra in inventory just in case
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

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> {
            for (GolemType type : GolemType.values()) {
                entries.accept(UGItems.GOLEM_SPAWN_EGGS.get(type));
            }
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(UGItems.WRENCH_ITEM);
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
            for (GolemType type : GolemType.values()) {
                if (type == GolemType.LAMP || type == GolemType.FURNACE || type == GolemType.JUKEBOX || type == GolemType.SMOKER || type == GolemType.BLAST_FURNACE || type == GolemType.MEDIC || type == GolemType.CACTUS) continue;
                net.minecraft.world.level.block.Block chest = UGBlocks.GOLEM_CHESTS.get(type);
                if (chest != null) {
                    entries.accept(chest);
                }
            }
        });

        // Register all golem types
        for (GolemType type : GolemType.values()) {
            // Create the entity type
            EntityType<UtilityGolem> entityType = FabricEntityTypeBuilder.create(
                            MobCategory.CREATURE,
                            (EntityType<UtilityGolem> et, net.minecraft.world.level.Level world) -> new UtilityGolem(et, world, type)
                    )
                    .dimensions(EntityDimensions.fixed(0.6F, 1.8F))
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, type.getName())));


            GOLEM_TYPES.put(type, entityType);

            // Register attributes
            FabricDefaultAttributeRegistry.register(entityType, type.getAttributes());

            // Register in the global registry
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, type.getName()),
                    entityType
            );
        }
        LOGGER.info("Utility Golems registered: " + GOLEM_TYPES.keySet());
    }


    public static void syncDiscoveredTrades(UtilityGolem golem) {
        if (!golem.level().isClientSide() && golem.level() instanceof ServerLevel) {
            SyncDiscoveredTradesPayload payload = new SyncDiscoveredTradesPayload(golem.getId(), golem.getDiscoveredTrades());
            for (ServerPlayer player : net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(golem)) {
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
