package rehdpanda.utilitygolems;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;

public class GolemPatterns {

    //find golem block patterns
    private static BlockPattern createGolemPattern(Block bottomBlock) {
        return BlockPatternBuilder.start()
                .aisle("^", "B") // top to bottom: Pumpkin (^), Bottom block (B)
                .where('B', cbp -> cbp.getBlockState().getBlock() == bottomBlock)
                .where('^', cbp -> cbp.getBlockState().getBlock() == bottomBlock)
                .build();
    }

    // Called on block placement
    public static void registerCallbacks() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            BlockPos pos = hitResult.getBlockPos();
            if (world.isClient()) return ActionResult.PASS; // only server

            trySpawnGolem((ServerWorld) world, pos, GolemType.LAPIS);
            trySpawnGolem((ServerWorld) world, pos, GolemType.REDSTONE);
            trySpawnGolem((ServerWorld) world, pos, GolemType.EMERALD);

            return ActionResult.PASS;
        });
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

        // Spawn the golem at the bottom block position + 1 for height
        BlockPos spawnPos = result.translate(0, 1, 0).getBlockPos();
        UtilityGolem golem = new UtilityGolem(UGInit.GOLEM_TYPES.get(type), world, type);
        golem.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
        world.spawnEntity(golem);

        // Remove blocks used in the pattern
        for (int y = 0; y < result.getHeight(); y++) {
            BlockPos removePos = result.translate(0, y+1, 0).getBlockPos();
            world.setBlockState(removePos, Blocks.AIR.getDefaultState());
        }
    }
}
