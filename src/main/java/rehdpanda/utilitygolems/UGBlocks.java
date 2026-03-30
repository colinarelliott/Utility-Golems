package rehdpanda.utilitygolems;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UGBlocks {
    public static final Map<GolemType, Block> GOLEM_CHESTS = new HashMap<>();
    public static BlockEntityType<GolemChestBlockEntity> GOLEM_CHEST_BLOCK_ENTITY;
    public static Block REDSTONE_GOLEM_STATUE;
    public static BlockEntityType<RedstoneGolemStatueBlockEntity> REDSTONE_GOLEM_STATUE_BLOCK_ENTITY;
    public static Block LIGHT_BLOCK;

    public static void register() {
        Identifier statueId = new Identifier(UGInit.MOD_ID, "redstone_golem_statue");
        REDSTONE_GOLEM_STATUE = Registry.register(
                BuiltInRegistries.BLOCK,
                statueId,
                new RedstoneGolemStatueBlock(BlockBehaviour.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK, statueId))
                        .strength(3.5F, 3.5F)
                        .requiresCorrectToolForDrops()
                        .noOcclusion())
        );
        Registry.register(
                BuiltInRegistries.ITEM,
                statueId,
                new BlockItem(REDSTONE_GOLEM_STATUE, new net.minecraft.world.item.Item.Properties().setId(ResourceKey.create(Registries.ITEM, statueId)))
        );

        REDSTONE_GOLEM_STATUE_BLOCK_ENTITY = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                statueId,
                FabricBlockEntityTypeBuilder.create(RedstoneGolemStatueBlockEntity::new, REDSTONE_GOLEM_STATUE).build()
        );

        Identifier lightId = new Identifier(UGInit.MOD_ID, "light_block");
        LIGHT_BLOCK = Registry.register(
                BuiltInRegistries.BLOCK,
                lightId,
                new LightBlock(BlockBehaviour.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK, lightId))
                        .lightLevel(state -> state.getValue(LightBlock.LEVEL))
                        .noCollision()
                        .noOcclusion()
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

            float hardness = materialBlock.defaultDestroyTime();
            float resistance = materialBlock.getExplosionResistance();

            Identifier id = new Identifier(UGInit.MOD_ID, name);
            BlockBehaviour.Properties settings = BlockBehaviour.Properties.ofFullCopy(materialBlock)
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(hardness, resistance)
                    .lightLevel(state -> 0);

            if (type == GolemType.TINTED_GLASS || type == GolemType.AMETHYST) {
                settings = settings.noOcclusion();
            }

            if (type != GolemType.BAMBOO && type != GolemType.SPONGE) {
                settings = settings.requiresCorrectToolForDrops();
            }

            Block block = Registry.register(
                    BuiltInRegistries.BLOCK,
                    id,
                    new GolemChestBlock(settings)
            );
            GOLEM_CHESTS.put(type, block);
            chestBlocks.add(block);

            Registry.register(
                    BuiltInRegistries.ITEM,
                    new Identifier(UGInit.MOD_ID, name),
                    new BlockItem(block, new net.minecraft.world.item.Item.Properties().setId(ResourceKey.create(Registries.ITEM, new Identifier(UGInit.MOD_ID, name))))
            );
        }

        GOLEM_CHEST_BLOCK_ENTITY = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                new Identifier(UGInit.MOD_ID, "golem_chest"),
                FabricBlockEntityTypeBuilder.create(GolemChestBlockEntity::new, chestBlocks.toArray(new Block[0])).build()
        );
    }
}
