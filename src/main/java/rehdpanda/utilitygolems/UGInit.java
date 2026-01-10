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
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.math.Vec3d;
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

    public static final Map<GolemType, EntityType<UtilityGolem>> GOLEM_TYPES = new HashMap<>();

    public static final ScreenHandlerType<GolemInventoryScreenHandler> GOLEM_SCREEN_HANDLER_TYPE =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "golem_inventory"), new ScreenHandlerType<>(GolemInventoryScreenHandler::new, FeatureSet.empty()));
    /*public static final ScreenHandlerType<GolemFurnaceScreenHandler> GOLEM_FURNACE_HANDLER =
            new ScreenHandlerType<>(
                    (syncId, playerInventory) -> null,
                    FeatureFlags.VANILLA_FEATURES
            );*/



    @Override
    public void onInitialize() {
        LOGGER.info("Utility Golems initializing...");
        UGBlocks.register();

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
                                give(player, Items.CARVED_PUMPKIN);

                                return 1;
                            })
            );
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            for (GolemType type : GolemType.values()) {
                entries.add(UGBlocks.GOLEM_CHESTS.get(type));
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
