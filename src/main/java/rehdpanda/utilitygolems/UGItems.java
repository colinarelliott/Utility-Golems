package rehdpanda.utilitygolems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UGItems {
    public static Item WRENCH_ITEM;
    public static final Map<GolemType, Item> GOLEM_SPAWN_EGGS = new HashMap<>();

    public static void register() {
        Identifier wrenchId = Identifier.fromNamespaceAndPath(UGInit.MOD_ID, "wrench");
        UGInit.LOGGER.info("Registering wrench item with ID: " + wrenchId);
        WRENCH_ITEM = Registry.register(
                BuiltInRegistries.ITEM,
                wrenchId,
                new Item(new Item.Properties()
                        .setId(ResourceKey.create(Registries.ITEM, wrenchId))
                        .stacksTo(1)
                        .durability(500)
                        .component(DataComponents.LORE, new ItemLore(List.of(Component.translatable("item.utility-golems.wrench.description")))))
        );
    }

    public static void registerSpawnEggs() {
        for (GolemType type : GolemType.values()) {
            Identifier eggId = Identifier.fromNamespaceAndPath(UGInit.MOD_ID, type.getName() + "_spawn_egg");
            Item.Properties settings = new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, eggId));
            Item egg = new UtilityGolemSpawnEggItem(settings, type);
            Registry.register(BuiltInRegistries.ITEM, eggId, egg);
            GOLEM_SPAWN_EGGS.put(type, egg);
        }
    }
}
