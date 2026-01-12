package rehdpanda.utilitygolems;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UGBlocks {
    public static final Map<GolemType, Block> GOLEM_CHESTS = new HashMap<>();
    public static BlockEntityType<GolemChestBlockEntity> GOLEM_CHEST_BLOCK_ENTITY;

    public static void register() {
        List<Block> chestBlocks = new ArrayList<>();
        for (GolemType type : GolemType.values()) {
            String name = type.getName().trim() + "_chest";
            Block block = Registry.register(
                    Registries.BLOCK,
                    Identifier.of(UGInit.MOD_ID, name),
                    new GolemChestBlock(AbstractBlock.Settings.copy(Blocks.CHEST)
                            .requiresTool()
                            .strength(2.5f, 12.5f)
                            .nonOpaque()
                            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(UGInit.MOD_ID, name))))
            );
            GOLEM_CHESTS.put(type, block);
            chestBlocks.add(block);

            Registry.register(
                    Registries.ITEM,
                    Identifier.of(UGInit.MOD_ID, name),
                    new BlockItem(block, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(UGInit.MOD_ID, name))))
            );
        }

        GOLEM_CHEST_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(UGInit.MOD_ID, "golem_chest"),
                FabricBlockEntityTypeBuilder.create(GolemChestBlockEntity::new, chestBlocks.toArray(new Block[0])).build()
        );
    }
}
