package rehdpanda.utilitygolems;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class GolemPatterns {

    // find golem block patterns
    private static BlockPattern createGolemPattern(Block bottomBlock) {
        return BlockPatternBuilder.start()
                .aisle("^", "B") // top to bottom: Pumpkin (^), Bottom block (B)
                .where('B', cbp -> cbp.getBlockState().getBlock() == bottomBlock)
                .where('^', cbp -> cbp.getBlockState().getBlock() == Blocks.CARVED_PUMPKIN)
                .build();
    }

    // Called from Mixin after block is placed
    public static void onPumpkinPlaced(World world, BlockPos pos) {
        if (world instanceof ServerWorld serverWorld) {
            // Check if placed on a GolemChestBlock that has a dead golem
            BlockPos belowPos = pos.down();
            BlockState belowState = world.getBlockState(belowPos);
            if (belowState.getBlock() instanceof GolemChestBlock) {
                BlockEntity be = world.getBlockEntity(belowPos);
                if (be instanceof GolemChestBlockEntity chestEntity && chestEntity.isGolemDead()) {
                    GolemType type = chestEntity.getGolemType();
                    if (type != null) {
                        Direction facing = belowState.get(GolemChestBlock.FACING);
                        boolean isStripped = false;
                        if (belowState.contains(GolemChestBlock.STRIPPED)) {
                            isStripped = belowState.get(GolemChestBlock.STRIPPED);
                        }

                        UtilityGolem golem = new UtilityGolem(UGInit.GOLEM_TYPES.get(type), serverWorld, type);
                        net.minecraft.entity.player.PlayerEntity creator = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 10.0, false);
                        if (creator != null) {
                            golem.setOwnerUuid(creator.getUuid());
                        }
                        golem.setStripped(isStripped);
                        golem.setChestPos(belowPos);
                        golem.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, facing.getPositiveHorizontalDegrees(), 0);
                        golem.initialize(serverWorld, serverWorld.getLocalDifficulty(pos), net.minecraft.entity.SpawnReason.MOB_SUMMONED, null);
                        serverWorld.spawnEntity(golem);

                        chestEntity.setGolemDead(false);
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());

                        // Play some effects
                        serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0.1);
                        world.playSound(null, pos, SoundEvents.BLOCK_PUMPKIN_CARVE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                        return;
                    }
                }
            }

            net.minecraft.entity.player.PlayerEntity player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 5.0, false);
            Direction facing = player != null ? player.getHorizontalFacing().getOpposite() : Direction.NORTH;

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

    private static void trySpawnGolem(ServerWorld world, BlockPos pos, GolemType type, Direction facing, boolean isStripped) {
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

        BlockPattern.Result result = pattern.searchAround(world, pos);
        if (result == null) return;

        // Spawn the golem at the pumpkin position
        BlockPos spawnPos = result.translate(0, 0, 0).getBlockPos();
        UtilityGolem golem = new UtilityGolem(UGInit.GOLEM_TYPES.get(type), world, type);
        net.minecraft.entity.player.PlayerEntity creator = world.getClosestPlayer(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 10.0, false);
        if (creator != null) {
            golem.setOwnerUuid(creator.getUuid());
        }
        golem.setStripped(isStripped);
        golem.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, facing.getPositiveHorizontalDegrees(), 0);
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
        if (chestBlock != null) {
            // Find a suitable place for the chest - let's put it where the bottom block was
            // In the aisle "^", "B", B is at (0, 1, 0) if ^ is (0, 0, 0)
            BlockPos bottomPos = result.translate(0, 1, 0).getBlockPos();
            golem.setChestPos(bottomPos);
            BlockState chestState = chestBlock.getDefaultState().with(GolemChestBlock.FACING, facing);
            if (chestState.contains(GolemChestBlock.STRIPPED)) {
                chestState = chestState.with(GolemChestBlock.STRIPPED, isStripped);
            }
            world.setBlockState(bottomPos, chestState);
        }
    }
}
