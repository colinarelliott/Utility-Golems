package rehdpanda.utilitygolems;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public class GolemPatterns {

    // find golem block patterns
    private static BlockPattern createGolemPattern(Block bottomBlock) {
        return BlockPatternBuilder.start()
                .aisle("^", "B") // top to bottom: Pumpkin (^), Bottom block (B)
                .where('B', cbp -> cbp.getState().getBlock() == bottomBlock)
                .where('^', cbp -> cbp.getState().getBlock() == Blocks.CARVED_PUMPKIN)
                .build();
    }

    // Called from Mixin after block is placed
    public static void onPumpkinPlaced(Level world, BlockPos pos) {
        if (world instanceof ServerLevel serverWorld) {
            // Check if placed on a GolemChestBlock that has a dead golem
            BlockPos belowPos = pos.below();
            BlockState belowState = world.getBlockState(belowPos);
            if (belowState.getBlock() instanceof GolemChestBlock) {
                BlockEntity be = world.getBlockEntity(belowPos);
                if (be instanceof GolemChestBlockEntity chestEntity && chestEntity.isGolemDead()) {
                    GolemType type = chestEntity.getGolemType();
                    if (type != null) {
                        Direction facing = belowState.getValue(GolemChestBlock.FACING);
                        boolean isStripped = false;
                        if (belowState.hasProperty(GolemChestBlock.STRIPPED)) {
                            isStripped = belowState.getValue(GolemChestBlock.STRIPPED);
                        }

                        UtilityGolem golem = new UtilityGolem(UGInit.GOLEM_TYPES.get(type), serverWorld, type);
                        net.minecraft.world.entity.player.Player creator = world.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 10.0, false);
                        if (creator != null) {
                            golem.setOwnerUuid(creator.getUUID());
                        }
                        golem.setStripped(isStripped);
                        golem.setChestPos(belowPos);
                        golem.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, facing.toYRot(), 0);
                        golem.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(pos), net.minecraft.world.entity.EntityMobSpawnType.MOB_SUMMONED, null);
                        serverWorld.addFreshEntity(golem);

                        chestEntity.setGolemDead(false);
                        world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

                        // Play some effects
                        serverWorld.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0.1);
                        world.playSound(null, pos, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1.0f, 1.0f);
                        return;
                    }
                }
            }

            net.minecraft.world.entity.player.Player player = world.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 5.0, false);
            Direction facing = player != null ? player.getDirection().getOpposite() : Direction.NORTH;

            trySpawnGolem(serverWorld, pos, GolemType.LAPIS, facing, false);
            trySpawnGolem(serverWorld, pos, GolemType.REDSTONE, facing, false);
            trySpawnGolem(serverWorld, pos, GolemType.EMERALD, facing, false);
            trySpawnGolem(serverWorld, pos, GolemType.GOLD, facing, false);
            trySpawnGolem(serverWorld, pos, GolemType.AMETHYST, facing, false);
            trySpawnGolem(serverWorld, pos, GolemType.NETHERITE, facing, false);
            trySpawnGolem(serverWorld, pos, GolemType.ANCIENT, facing, false);
            trySpawnGolem(serverWorld, pos, GolemType.FURNACE, facing, false);
            trySpawnGolem(serverWorld, pos, GolemType.BAMBOO, facing, false);
            trySpawnGolem(serverWorld, pos, GolemType.BAMBOO, facing, true);
            trySpawnGolem(serverWorld, pos, GolemType.DIAMOND, facing, false);
            trySpawnGolem(serverWorld, pos, GolemType.SPONGE, facing, false);
            trySpawnGolem(serverWorld, pos, GolemType.DEEPSLATE, facing, false);
            trySpawnGolem(serverWorld, pos, GolemType.JUKEBOX, facing, false);
            trySpawnGolem(serverWorld, pos, GolemType.LAMP, facing, false);
            trySpawnGolem(serverWorld, pos, GolemType.SMOKER, facing, false);
            trySpawnGolem(serverWorld, pos, GolemType.BLAST_FURNACE, facing, false);
            trySpawnGolem(serverWorld, pos, GolemType.NETHER_WART, facing, false);
            trySpawnGolem(serverWorld, pos, GolemType.MEDIC, facing, false);
            trySpawnGolem(serverWorld, pos, GolemType.CACTUS, facing, false);
            trySpawnGolem(serverWorld, pos, GolemType.HONEYCOMB, facing, false);
            trySpawnGolem(serverWorld, pos, GolemType.HOPPER, facing, false);
            trySpawnGolem(serverWorld, pos, GolemType.TINTED_GLASS, facing, false);
        }
    }

    private static void trySpawnGolem(ServerLevel world, BlockPos pos, GolemType type, Direction facing, boolean isStripped) {
        Block bottomBlock = switch (type) {
            case LAPIS -> Blocks.LAPIS_BLOCK;
            case REDSTONE -> Blocks.REDSTONE_BLOCK;
            case EMERALD -> Blocks.EMERALD_BLOCK;
            case GOLD -> Blocks.GOLD_BLOCK;
            case AMETHYST -> Blocks.AMETHYST_BLOCK;
            case NETHERITE -> Blocks.NETHERITE_BLOCK;
            case ANCIENT -> Blocks.ANCIENT_DEBRIS;
            case FURNACE -> Blocks.FURNACE;
            case DIAMOND -> Blocks.DIAMOND_BLOCK;
            case SPONGE -> Blocks.SPONGE;
            case BAMBOO -> isStripped ? Blocks.STRIPPED_BAMBOO_BLOCK : Blocks.BAMBOO_BLOCK;
            case DEEPSLATE -> Blocks.COBBLED_DEEPSLATE;
            case JUKEBOX -> Blocks.JUKEBOX;
            case LAMP -> Blocks.REDSTONE_LAMP;
            case SMOKER -> Blocks.SMOKER;
            case BLAST_FURNACE -> Blocks.BLAST_FURNACE;
            case NETHER_WART -> Blocks.NETHER_WART_BLOCK;
            case MEDIC -> Blocks.TARGET;
            case CACTUS -> Blocks.CACTUS;
            case HONEYCOMB -> Blocks.HONEYCOMB_BLOCK;
            case HOPPER -> Blocks.HOPPER;
            case TINTED_GLASS -> Blocks.TINTED_GLASS;
            default -> Blocks.LAPIS_BLOCK;
        };

        BlockPattern pattern = createGolemPattern(bottomBlock);

        BlockPattern.BlockPatternMatch result = pattern.find(world, pos);
        if (result == null) return;

        // Spawn the golem at the pumpkin position
        BlockPos spawnPos = result.getBlock(0, 0, 0).getPos();
        UtilityGolem golem = new UtilityGolem(UGInit.GOLEM_TYPES.get(type), world, type);
        net.minecraft.world.entity.player.Player creator = world.getNearestPlayer(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 10.0, false);
        if (creator != null) {
            golem.setOwnerUuid(creator.getUUID());
        }
        golem.setStripped(isStripped);
        golem.snapTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, facing.toYRot(), 0);
        golem.finalizeSpawn(world, world.getCurrentDifficultyAt(spawnPos), net.minecraft.world.entity.EntityMobSpawnType.MOB_SUMMONED, null);
        world.addFreshEntity(golem);

        // Remove blocks used in the pattern
        for (int i = 0; i < pattern.getWidth(); i++) {
            for (int j = 0; j < pattern.getHeight(); j++) {
                for (int k = 0; k < pattern.getDepth(); k++) {
                    BlockPos removePos = result.getBlock(i, j, k).getPos();
                    world.setBlockAndUpdate(removePos, Blocks.AIR.defaultBlockState());
                }
            }
        }

        Block chestBlock = type.getChestBlock();
        if (chestBlock != null) {
            // Find a suitable place for the chest - let's put it where the bottom block was
            // In the aisle "^", "B", B is at (0, 1, 0) if ^ is (0, 0, 0)
            BlockPos bottomPos = result.getBlock(0, 1, 0).getPos();
            golem.setChestPos(bottomPos);
            BlockState chestState = chestBlock.defaultBlockState().setValue(GolemChestBlock.FACING, facing);
            if (chestState.hasProperty(GolemChestBlock.STRIPPED)) {
                chestState = chestState.setValue(GolemChestBlock.STRIPPED, isStripped);
            }
            world.setBlockAndUpdate(bottomPos, chestState);
        }
    }
}
