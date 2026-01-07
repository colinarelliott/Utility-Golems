package rehdpanda.utilitygolems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/// BASE CLASS
/// INITIALIZES AND REGISTERS GOLEM TYPES TO REGISTRY

public class UGInit implements ModInitializer {

    public static final String MOD_ID = "utilitygolems";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Map<GolemType, EntityType<UtilityGolem>> GOLEM_TYPES = new HashMap<>();

    @Override
    public void onInitialize() {
        LOGGER.info("Utility Golems initializing...");

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
                    Registries.ENTITY_TYPE, // <-- this works in Fabric 1.21+ NO IT DOESN"T
                    Identifier.of(MOD_ID, type.getName()),
                    entityType
            );
        }

        LOGGER.info("Utility Golems registered: " + GOLEM_TYPES.keySet());
    }
}
