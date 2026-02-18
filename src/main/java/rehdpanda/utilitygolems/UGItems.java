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
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;

import java.util.List;

public class UGItems {
    public static Item WRENCH_ITEM;

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
}
