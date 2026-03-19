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
    public static Block LIGHT_BLOCK;

    public static void register() {
        Identifier lightId = Identifier.of(UGInit.MOD_ID, "light_block");
        LIGHT_BLOCK = Registry.register(
                Registries.BLOCK,
                lightId,
                new LightBlock(AbstractBlock.Settings.create()
                        .registryKey(RegistryKey.of(RegistryKeys.BLOCK, lightId))
                        .luminance(state -> state.get(LightBlock.LEVEL))
                        .noCollision()
                        .nonOpaque()
                        .air())
        );
        List<Block> chestBlocks = new ArrayList<>();
        for (GolemType type : GolemType.values()) {
            if (type == GolemType.LAMP || type == GolemType.FURNACE || type == GolemType.JUKEBOX || type == GolemType.SMOKER || type == GolemType.BLAST_FURNACE || type == GolemType.MEDIC || type == GolemType.CACTUS || type == GolemType.TINTED_GLASS) continue;
            String name = type.getName().trim() + "_chest";
            Block materialBlock = switch (type) {
                case LAPIS -> Blocks.LAPIS_BLOCK;
                case REDSTONE -> Blocks.REDSTONE_BLOCK;
                case EMERALD -> Blocks.EMERALD_BLOCK;
                case GOLD -> Blocks.GOLD_BLOCK;
                case AMETHYST -> Blocks.AMETHYST_BLOCK;
                case NETHERITE -> Blocks.NETHERITE_BLOCK;
                case ANCIENT -> Blocks.ANCIENT_DEBRIS;
                case DIAMOND -> Blocks.DIAMOND_BLOCK;
                case BAMBOO -> Blocks.BAMBOO_PLANKS;
                case SPONGE -> Blocks.SPONGE;
                case DEEPSLATE -> Blocks.COBBLED_DEEPSLATE;
                case FURNACE -> Blocks.FURNACE;
                case JUKEBOX -> Blocks.JUKEBOX;
                case NETHER_WART -> Blocks.NETHER_WART_BLOCK;
                case MEDIC -> Blocks.TARGET;
                case CACTUS -> Blocks.CACTUS;
                case TINTED_GLASS -> Blocks.TINTED_GLASS;
                case HONEYCOMB -> Blocks.HONEYCOMB_BLOCK;
                case HOPPER -> Blocks.HOPPER;
                default -> Blocks.AIR;
            };

            float hardness = materialBlock.getHardness();
            float resistance = materialBlock.getBlastResistance();

            Identifier id = Identifier.of(UGInit.MOD_ID, name);
            AbstractBlock.Settings settings = AbstractBlock.Settings.copy(materialBlock)
                    .registryKey(RegistryKey.of(RegistryKeys.BLOCK, id))
                    .strength(hardness, resistance)
                    .luminance(state -> 0);

            if (type == GolemType.TINTED_GLASS || type == GolemType.AMETHYST) {
                settings = settings.nonOpaque();
            }

            if (type != GolemType.BAMBOO && type != GolemType.SPONGE) {
                settings = settings.requiresTool();
            }

            Block block = Registry.register(
                    Registries.BLOCK,
                    id,
                    new GolemChestBlock(settings)
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
