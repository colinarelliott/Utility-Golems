package rehdpanda.utilitygolems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/// BASE CLASS
/// INITIALIZES AND REGISTERS GOLEM TYPES TO REGISTRY

public class UGInit implements ModInitializer {

    public static final String MOD_ID = "utility-golems";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Map<GolemType, EntityType<UtilityGolem>> GOLEM_TYPES = new HashMap<>();

    public static final ScreenHandlerType<GolemInventoryScreenHandler> GOLEM_SCREEN_HANDLER_TYPE =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "golem_inventory"), new ScreenHandlerType<>(GolemInventoryScreenHandler::new, FeatureSet.empty()));

    @Override
    public void onInitialize() {
        LOGGER.info("Utility Golems initializing...");
        UGBlocks.register();

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
}
