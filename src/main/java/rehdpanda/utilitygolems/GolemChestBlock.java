// java
package rehdpanda.utilitygolems;

import net.minecraft.world.level.block.*;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;
import rehdpanda.utilitygolems.GolemChestBlockEntity;

import java.util.function.Supplier;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;

import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;
import rehdpanda.utilitygolems.GolemChestBlockEntity;

import java.util.function.Supplier;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;

import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;

public class GolemChestBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<ChestType> CHEST_TYPE = BlockStateProperties.CHEST_TYPE;
    public static final net.minecraft.world.level.block.state.properties.BooleanProperty STRIPPED = net.minecraft.world.level.block.state.properties.BooleanProperty.create("stripped");

    public GolemChestBlock(BlockBehaviour.Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(CHEST_TYPE, ChestType.SINGLE).setValue(STRIPPED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CHEST_TYPE, STRIPPED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        ChestType chestType = ChestType.SINGLE;
        Direction direction = ctx.getHorizontalDirection().getOpposite();
        Direction direction2 = ctx.getClickedFace();
        boolean bl = ctx.isSecondaryUseActive();
        if (direction2.getAxis().isHorizontal() && bl) {
            Direction direction3 = this.getNeighborChestDirection(ctx, direction2.getOpposite());
            if (direction3 != null && direction3.getAxis() != direction2.getAxis()) {
                direction = direction3;
                chestType = direction3.getCounterClockWise() == direction2.getOpposite() ? ChestType.RIGHT : ChestType.LEFT;
            }
        }
        if (chestType == ChestType.SINGLE) {
            Direction direction4 = this.getNeighborChestDirection(ctx, direction.getClockWise());
            if (direction4 != null && direction4.getAxis() == direction.getAxis()) {
                chestType = ChestType.LEFT;
            } else {
                Direction direction5 = this.getNeighborChestDirection(ctx, direction.getCounterClockWise());
                if (direction5 != null && direction5.getAxis() == direction.getAxis()) {
                    chestType = ChestType.RIGHT;
                }
            }
        }
        return this.defaultBlockState().setValue(FACING, direction).setValue(CHEST_TYPE, chestType).setValue(STRIPPED, false);
    }

    @Nullable
    private Direction getNeighborChestDirection(BlockPlaceContext ctx, Direction dir) {
        BlockState blockState = ctx.getLevel().getBlockState(ctx.getClickedPos().relative(dir));
        return blockState.is(this) && blockState.getValue(CHEST_TYPE) == ChestType.SINGLE && blockState.getValue(STRIPPED) == false ? blockState.getValue(FACING) : null;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, @Nullable RandomSource random) {
        if (neighborState.is(this) && direction.getAxis().isHorizontal()) {
            ChestType neighborType = neighborState.getValue(CHEST_TYPE);
            if (state.getValue(CHEST_TYPE) == ChestType.SINGLE) {
                if (neighborType != ChestType.SINGLE && state.getValue(FACING) == neighborState.getValue(FACING) && getFacing(neighborState) == direction.getOpposite() && state.getValue(STRIPPED) == neighborState.getValue(STRIPPED)) {
                    return state.setValue(CHEST_TYPE, neighborType.getOpposite());
                }
            } else if (direction == getFacing(state)) {
                if (neighborState.getValue(FACING) != state.getValue(FACING) || neighborType != state.getValue(CHEST_TYPE).getOpposite() || state.getValue(STRIPPED) != neighborState.getValue(STRIPPED)) {
                    return state.setValue(CHEST_TYPE, ChestType.SINGLE);
                }
            }
        } else if (state.getValue(CHEST_TYPE) != ChestType.SINGLE && direction == getFacing(state)) {
            return state.setValue(CHEST_TYPE, ChestType.SINGLE);
        }
        return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    public static Direction getFacing(BlockState state) {
        Direction direction = state.getValue(FACING);
        return state.getValue(CHEST_TYPE) == ChestType.LEFT ? direction.getClockWise() : direction.getCounterClockWise();
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GolemChestBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (type == UGBlocks.GOLEM_CHEST_BLOCK_ENTITY) {
            if (world.isClientSide()) {
                return (world1, pos, state1, blockEntity) -> ChestBlockEntity.lidAnimateTick(world1, pos, state1, (GolemChestBlockEntity) blockEntity);
            } else {
                return (world1, pos, state1, blockEntity) -> GolemChestBlockEntity.serverTick(world1, pos, state1, (GolemChestBlockEntity) blockEntity);
            }
        }
        return null;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level world, BlockPos pos, int type, int data) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof GolemChestBlockEntity) {
            boolean result = blockEntity.triggerEvent(type, data);
            
            // InteractionHandle double chest synchronization
            ChestType chestType = state.getValue(CHEST_TYPE);
            if (chestType != ChestType.SINGLE) {
                BlockPos otherPos = pos.relative(getFacing(state));
                BlockState otherState = world.getBlockState(otherPos);
                if (otherState.is(state.getBlock()) && otherState.getValue(CHEST_TYPE) == chestType.getOpposite()) {
                    BlockEntity otherBE = world.getBlockEntity(otherPos);
                    if (otherBE instanceof GolemChestBlockEntity) {
                        otherBE.triggerEvent(type, data);
                    }
                }
            }
            return result;
        }
        return false;
    }


    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel world, BlockPos pos, boolean moved) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof GolemChestBlockEntity) {
            Containers.dropContents(world, pos, (GolemChestBlockEntity)blockEntity);
            world.updateNeighbourForOutputSignal(pos, this);
        }
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (state.is(UGBlocks.GOLEM_CHESTS.get(GolemType.BAMBOO)) && !state.getValue(STRIPPED) && stack.getItem() instanceof net.minecraft.world.item.AxeItem) {
            if (!world.isClientSide()) {
                world.setBlock(pos, state.setValue(STRIPPED, true), 3);
                world.playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.getAbilities().instabuild) {
                    stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, world, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else {
            Container inventory = GolemChestBlockEntity.getInventory(this, state, world, pos, false);
            if (inventory != null) {
                boolean isDead = false;
                if (inventory instanceof CompoundContainer doubleInventory) {
                    // Check both halves
                    if (world.getBlockEntity(pos) instanceof GolemChestBlockEntity be && be.isGolemDead()) {
                        isDead = true;
                    } else {
                        BlockPos otherPos = pos.relative(getFacing(state));
                        if (world.getBlockEntity(otherPos) instanceof GolemChestBlockEntity otherBe && otherBe.isGolemDead()) {
                            isDead = true;
                        }
                    }
                } else {
                    GolemChestBlockEntity be = (GolemChestBlockEntity) world.getBlockEntity(pos);
                    isDead = be != null && be.isGolemDead();
                }
                
                final boolean finalIsDead = isDead;
                player.openMenu(new net.minecraft.world.MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return inventory instanceof CompoundContainer
                            ? Component.translatable(state.getBlock().getDescriptionId().replace("block.", "container.") + "_double")
                            : Component.translatable(state.getBlock().getDescriptionId());
                    }

                    @Override
                    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
                        return new GolemChestMenu(syncId, playerInventory, inventory, pos, finalIsDead);
                    }
                });
            }
            return InteractionResult.CONSUME;
        }
    }
}
