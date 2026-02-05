package rehdpanda.utilitygolems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.packet.CustomPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.entity.ai.brain.task.TargetUtil.give;

/// BASE CLASS
/// INITIALIZES AND REGISTERS GOLEM TYPES TO REGISTRY

public class UGInit implements ModInitializer {

    public static final String MOD_ID = "utility-golems";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public record SyncPatternPayload(int entityId, int patternOrdinal, int width, int length, ItemStack filter, boolean started) implements CustomPayload {
        public static final Id<SyncPatternPayload> ID = new Id<>(Identifier.of(MOD_ID, "sync_pattern"));
        public static final PacketCodec<RegistryByteBuf, SyncPatternPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, SyncPatternPayload::entityId,
                PacketCodecs.VAR_INT, SyncPatternPayload::patternOrdinal,
                PacketCodecs.VAR_INT, SyncPatternPayload::width,
                PacketCodecs.VAR_INT, SyncPatternPayload::length,
                ItemStack.OPTIONAL_PACKET_CODEC, SyncPatternPayload::filter,
                PacketCodecs.BOOLEAN, SyncPatternPayload::started,
                SyncPatternPayload::new
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



    @Override
    public void onInitialize() {
        LOGGER.info("Utility Golems initializing...");
        UGBlocks.register();
        UGItems.register();

        PayloadTypeRegistry.playC2S().register(SyncPatternPayload.ID, SyncPatternPayload.CODEC);
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
                    context.player().sendMessage(Text.literal("Golem mode set to: " + pattern.getDisplayName() + (payload.started() ? " (Started)" : " (Stopped)")), true);
                }
            });
        });

        /// REGISTER DEBUG COMMANDS
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("UG-debug")
                            .executes(context -> {
                                ServerCommandSource source = context.getSource();
                                source.sendFeedback(
                                        () -> Text.literal("Spawning Utility Golems test items..."),
                                        false
                                );
                                ServerPlayerEntity player = source.getPlayer();
                                give(player, Items.GOLD_BLOCK);
                                give(player, Items.LAPIS_BLOCK);
                                give(player, Items.EMERALD_BLOCK);
                                give(player, Items.NETHERITE_BLOCK);
                                give(player, Items.REDSTONE_BLOCK);
                                give(player, Items.AMETHYST_BLOCK);
                                give(player, Items.DIAMOND_BLOCK);
                                give(player, Items.BAMBOO_BLOCK);
                                give(player, Items.FURNACE);
                                give(player, Items.JUKEBOX);
                                give(player, Items.REDSTONE_LAMP);
                                give(player, Items.SPONGE);
                                give(player, Items.COBBLED_DEEPSLATE);
                                give(player, Items.CARVED_PUMPKIN);
                                give(player, UGItems.WRENCH_ITEM);

                                return 1;
                            })
            );
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(UGItems.WRENCH_ITEM);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            for (GolemType type : GolemType.values()) {
                if (type == GolemType.LAMP) continue;
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

    private void give(ServerPlayerEntity player, Item item) {
        player.getInventory().insertStack(new ItemStack(item, 1));
    }
}
