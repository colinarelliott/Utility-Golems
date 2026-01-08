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
        }
    }

    private static void trySpawnGolem(ServerWorld world, BlockPos pos, GolemType type) {
        Block bottomBlock = Blocks.LAPIS_BLOCK;
        if (type == GolemType.LAPIS) {
            bottomBlock = Blocks.LAPIS_BLOCK;
        } else if (type == GolemType.REDSTONE) {
            bottomBlock = Blocks.REDSTONE_BLOCK;
        } else if (type == GolemType.EMERALD) {
            bottomBlock = Blocks.EMERALD_BLOCK;
        }

        BlockPattern pattern = createGolemPattern(bottomBlock);

        BlockPattern.Result result = pattern.searchAround(world, pos);
        if (result == null) return;

        // Spawn the golem at the pumpkin position
        BlockPos spawnPos = result.translate(0, 0, 0).getBlockPos();
        UtilityGolem golem = new UtilityGolem(UGInit.GOLEM_TYPES.get(type), world, type);
        golem.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
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
    }
}
