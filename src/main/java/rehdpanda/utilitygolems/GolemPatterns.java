package rehdpanda.utilitygolems;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.world.World;

public class GolemPatterns {

    //find golem block patterns
    private static BlockPattern createGolemPattern(Block bottomBlock) {
        return BlockPatternBuilder.start()
                .aisle("^", "B") // top to bottom: Pumpkin (^), Bottom block (B)
                .where('B', cbp -> cbp.getBlockState().getBlock() == bottomBlock)
                .where('^', cbp -> cbp.getBlockState().getBlock() == Blocks.CARVED_PUMPKIN || cbp.getBlockState().getBlock() == Blocks.PUMPKIN)
                .build();
    }

    // Called from Mixin after block is placed
    public static void onPumpkinPlaced(World world, BlockPos pos) {
        if (world instanceof ServerWorld serverWorld) {
            trySpawnGolem(serverWorld, pos, GolemType.LAPIS);
            trySpawnGolem(serverWorld, pos, GolemType.REDSTONE);
            trySpawnGolem(serverWorld, pos, GolemType.EMERALD);
            trySpawnGolem(serverWorld, pos, GolemType.GOLD);
            trySpawnGolem(serverWorld, pos, GolemType.AMETHYST);
            trySpawnGolem(serverWorld, pos, GolemType.NETHERITE);
            trySpawnGolem(serverWorld, pos, GolemType.FURNACE);
            trySpawnGolem(serverWorld, pos, GolemType.BAMBOO);
            trySpawnGolem(serverWorld, pos, GolemType.DIAMOND);
            trySpawnGolem(serverWorld, pos, GolemType.SPONGE);
            trySpawnGolem(serverWorld, pos, GolemType.DEEPSLATE);
            trySpawnGolem(serverWorld, pos, GolemType.JUKEBOX);
        }
    }

    private static void trySpawnGolem(ServerWorld world, BlockPos pos, GolemType type) {
        Block bottomBlock = switch (type) {
            case LAPIS -> Blocks.LAPIS_BLOCK;
            case REDSTONE -> Blocks.REDSTONE_BLOCK;
            case EMERALD -> Blocks.EMERALD_BLOCK;
            case GOLD -> Blocks.GOLD_BLOCK;
            case AMETHYST -> Blocks.AMETHYST_BLOCK;
            case NETHERITE -> Blocks.NETHERITE_BLOCK;
            case FURNACE -> Blocks.FURNACE;
            case DIAMOND -> Blocks.DIAMOND_BLOCK;
            case SPONGE -> Blocks.SPONGE;
            case BAMBOO -> Blocks.BAMBOO_BLOCK;
            //case STRIPPEDBAMBOO -> Blocks.STRIPPED_BAMBOO_BLOCK;
            case DEEPSLATE -> Blocks.COBBLED_DEEPSLATE;
            case JUKEBOX -> Blocks.JUKEBOX;
            default -> Blocks.LAPIS_BLOCK;
        };

        BlockPattern pattern = createGolemPattern(bottomBlock);

        BlockPattern.Result result = pattern.searchAround(world, pos);
        if (result == null) return;

        // Spawn the golem at the pumpkin position
        BlockPos spawnPos = result.translate(0, 0, 0).getBlockPos();
        UtilityGolem golem = new UtilityGolem(UGInit.GOLEM_TYPES.get(type), world, type);
        golem.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
        golem.initialize(world, world.getLocalDifficulty(spawnPos), net.minecraft.entity.SpawnReason.MOB_SUMMONED, null);
        world.spawnEntity(golem);

        // Remove blocks used in the pattern
        for (int i = 0; i < pattern.getWidth(); i++) {
            for (int j = 0; j < pattern.getHeight(); j++) {
                for (int k = 0; k < pattern.getDepth(); k++) {
                    BlockPos removePos = result.translate(i, j, k).getBlockPos();
                    world.setBlockState(removePos, Blocks.AIR.getDefaultState());
                }
            }
        }

        Block chestBlock = type.getChestBlock();
        if (chestBlock != null && golem.getGolemType() != GolemType.FURNACE && golem.getGolemType() != GolemType.JUKEBOX) {
            // Find a suitable place for the chest - let's put it where the bottom block was
            // In the aisle "^", "B", B is at (0, 1, 0) if ^ is (0, 0, 0)
            BlockPos bottomPos = result.translate(0, 1, 0).getBlockPos();
            world.setBlockState(bottomPos, chestBlock.getDefaultState());
        }
    }
}
