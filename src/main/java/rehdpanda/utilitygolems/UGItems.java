package rehdpanda.utilitygolems;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

import java.util.HashMap;
import java.util.Map;

public class UGItems {
    public static Item WRENCH_ITEM;
    public static final Map<GolemType, Item> GOLEM_SPAWN_EGGS = new HashMap<>();

    public static void register() {
        Identifier wrenchId = Identifier.of(UGInit.MOD_ID, "wrench");
        UGInit.LOGGER.info("Registering wrench item with ID: " + wrenchId);
        WRENCH_ITEM = Registry.register(
                Registries.ITEM,
                wrenchId,
                new Item(new Item.Settings()
                        .registryKey(RegistryKey.of(RegistryKeys.ITEM, wrenchId))
                        .maxCount(1)
                        .maxDamage(500)
                        .component(DataComponentTypes.LORE, new LoreComponent(List.of(Text.translatable("item.utility-golems.wrench.description")))))
        );
    }

    public static void registerSpawnEggs() {
        for (GolemType type : GolemType.values()) {
            Identifier eggId = Identifier.of(UGInit.MOD_ID, type.getName() + "_spawn_egg");
            // Default colors (copper-like) — actual look comes from the item model/texture; you can recolor the texture later
            int primary = 0xB87333; // copper-ish
            int secondary = 0xD9A066; // lighter copper accent
            Item.Settings settings = new Item.Settings()
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, eggId));
            Item egg = new UtilityGolemSpawnEggItem(settings, type);
            Registry.register(Registries.ITEM, eggId, egg);
            GOLEM_SPAWN_EGGS.put(type, egg);
        }
    }
}
