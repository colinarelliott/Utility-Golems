package rehdpanda.utilitygolems;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class GolemChestBlockEntity extends ChestBlockEntity {
    private GolemType golemType;
    private boolean golemDead = false;
    private int transferCooldown = -1;

    public GolemChestBlockEntity(BlockPos pos, BlockState state) {
        super(UGBlocks.GOLEM_CHEST_BLOCK_ENTITY, pos, state);
    }

    public static void serverTick(Level world, BlockPos pos, BlockState state, GolemChestBlockEntity blockEntity) {
        if (blockEntity.getGolemType() == GolemType.HOPPER) {
            blockEntity.transferCooldown--;
            if (blockEntity.transferCooldown <= 0) {
                blockEntity.transferCooldown = 0;
                if (blockEntity.transferItems(world, pos, state, blockEntity)) {
                    blockEntity.transferCooldown = 16;
                    blockEntity.setChanged();
                }
            }
        }
    }

    private boolean transferItems(Level world, BlockPos pos, BlockState state, GolemChestBlockEntity blockEntity) {
        if (!world.isClientSide()) {
            if (blockEntity.transferCooldown <= 0) {
                boolean changed = false;
                if (!blockEntity.isEmpty()) {
                    changed = pushItemsOut(world, pos, state, blockEntity);
                }
                if (!blockEntity.isFull()) {
                    changed |= pullItemsIn(world, pos, state, blockEntity);
                }
                return changed;
            }
        }
        return false;
    }

    private boolean isFull() {
        for (int i = 0; i < this.getContainerSize(); ++i) {
            ItemStack itemStack = this.getItem(i);
            if (itemStack.isEmpty() || itemStack.getCount() < itemStack.getMaxStackSize()) {
                return false;
            }
        }
        return true;
    }

    private static boolean pushItemsOut(Level world, BlockPos pos, BlockState state, GolemChestBlockEntity blockEntity) {
        BlockPos downPos = pos.below();
        Container targetInventory = getInventoryAt(world, downPos);
        if (targetInventory != null) {
            Direction direction = Direction.UP;
            for (int i = 0; i < blockEntity.getContainerSize(); ++i) {
                if (!blockEntity.getItem(i).isEmpty()) {
                    ItemStack itemStack = blockEntity.getItem(i).copy();
                    ItemStack toTransfer = blockEntity.removeItem(i, 1);
                    ItemStack itemStack2 = transfer(blockEntity, targetInventory, toTransfer, direction);
                    if (itemStack2.isEmpty()) {
                        targetInventory.setChanged();
                        return true;
                    }
                    blockEntity.setItem(i, itemStack);
                }
            }
        }
        return false;
    }

    private static boolean pullItemsIn(Level world, BlockPos pos, BlockState state, GolemChestBlockEntity blockEntity) {
        Container sourceInventory = getInventoryAt(world, pos.above());
        if (sourceInventory != null) {
            Direction direction = Direction.DOWN;
            int[] slots = getAvailableSlots(sourceInventory, direction);
            for (int slot : slots) {
                ItemStack itemStack = sourceInventory.getItem(slot);
                if (!itemStack.isEmpty() && canExtract(sourceInventory, itemStack, slot, direction)) {
                    ItemStack itemStack2 = itemStack.copy();
                    ItemStack toTransfer = sourceInventory.removeItem(slot, 1);
                    ItemStack itemStack3 = transfer(sourceInventory, blockEntity, toTransfer, null);
                    if (itemStack3.isEmpty()) {
                        sourceInventory.setChanged();
                        return true;
                    }
                    sourceInventory.setItem(slot, itemStack2);
                }
            }
        }
        return false;
    }

    private static int[] getAvailableSlots(Container inventory, Direction side) {
        if (inventory instanceof WorldlyContainer sidedInventory) {
            return sidedInventory.getSlotsForFace(side);
        } else {
            int[] slots = new int[inventory.getContainerSize()];
            for (int i = 0; i < slots.length; ++i) {
                slots[i] = i;
            }
            return slots;
        }
    }

    private static boolean canExtract(Container inventory, ItemStack stack, int slot, Direction side) {
        return !(inventory instanceof WorldlyContainer sidedInventory) || sidedInventory.canTakeItemThroughFace(slot, stack, side);
    }

    private static ItemStack transfer(@Nullable Container from, Container to, ItemStack stack, @Nullable Direction side) {
        boolean ignoreNbt = false;
        if (to instanceof GolemChestBlockEntity gTo) {
            ignoreNbt = gTo.getGolemType() == GolemType.HOPPER;
        } else if (from instanceof GolemChestBlockEntity gFrom) {
            ignoreNbt = gFrom.getGolemType() == GolemType.HOPPER;
        }

        if (to instanceof WorldlyContainer sidedInventory && side != null) {
            int[] slots = sidedInventory.getSlotsForFace(side);
            for (int i = 0; i < slots.length && !stack.isEmpty(); ++i) {
                stack = transfer(from, to, stack, slots[i], side, ignoreNbt);
            }
        } else {
            int size = to.getContainerSize();
            for (int i = 0; i < size && !stack.isEmpty(); ++i) {
                stack = transfer(from, to, stack, i, side, ignoreNbt);
            }
        }
        return stack;
    }

    private static ItemStack transfer(@Nullable Container from, Container to, ItemStack stack, int slot, @Nullable Direction side, boolean ignoreNbt) {
        ItemStack itemStack = to.getItem(slot);
        if (canInsert(to, stack, slot, side)) {
            boolean changed = false;
            if (itemStack.isEmpty()) {
                to.setItem(slot, stack);
                stack = ItemStack.EMPTY;
                changed = true;
            } else if (canMergeItems(itemStack, stack, ignoreNbt)) {
                int i = Math.min(to.getMaxStackSize(), stack.getMaxStackSize()) - itemStack.getCount();
                int j = Math.min(stack.getCount(), i);
                stack.shrink(j);
                itemStack.grow(j);
                changed = j > 0;
            }
            if (changed) {
                to.setChanged();
            }
        }
        return stack;
    }

    private static boolean canInsert(Container inventory, ItemStack stack, int slot, @Nullable Direction side) {
        if (!inventory.canPlaceItem(slot, stack)) {
            return false;
        }
        return !(inventory instanceof WorldlyContainer sidedInventory) || sidedInventory.canPlaceItemThroughFace(slot, stack, side);
    }

    private static boolean canMergeItems(ItemStack first, ItemStack second, boolean ignoreNbt) {
        if (ignoreNbt) {
            return first.is(second.getItem()) && first.getDamageValue() == second.getDamageValue() && first.getCount() < first.getMaxStackSize();
        }
        return first.is(second.getItem()) && first.getDamageValue() == second.getDamageValue() && first.getCount() < first.getMaxStackSize() && ItemStack.isSameItemSameComponents(first, second);
    }

    @Nullable
    private static Container getInventoryAt(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (state.hasBlockEntity()) {
            net.minecraft.world.level.block.entity.BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof Container inventory) {
                if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock) {
                    return ChestBlock.getContainer((ChestBlock)block, state, world, pos, false);
                }
                return inventory;
            }
        }
        return null;
    }

    public void setGolemDead(boolean dead) {
        this.golemDead = dead;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);

            // If double chest, notify the other half
            BlockState state = this.getBlockState();
            if (state.hasProperty(GolemChestBlock.CHEST_TYPE)) {
                ChestType type = state.getValue(GolemChestBlock.CHEST_TYPE);
                if (type != ChestType.SINGLE) {
                    BlockPos otherPos = this.worldPosition.relative(GolemChestBlock.getFacing(state));
                    net.minecraft.world.level.block.entity.BlockEntity otherBE = this.level.getBlockEntity(otherPos);
                    if (otherBE instanceof GolemChestBlockEntity otherGChest && otherGChest.golemDead != dead) {
                        otherGChest.golemDead = dead;
                        otherGChest.setChanged();
                        this.level.sendBlockUpdated(otherPos, otherGChest.getBlockState(), otherGChest.getBlockState(), 3);
                    }
                }
            }
        }
    }

    public boolean isGolemDead() {
        return golemDead;
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        this.golemDead = input.getBooleanOr("golemDead", false);
        this.transferCooldown = input.getIntOr("transferCooldown", -1);
        String typeName = input.getStringOr("golemType", "REGULAR");
        try {
            this.golemType = GolemType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            this.golemType = GolemType.LAPIS;
        }
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("golemDead", this.golemDead);
        output.putInt("transferCooldown", this.transferCooldown);
        if (this.golemType != null) {
            output.putString("golemType", this.golemType.name());
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    public static Container getInventory(GolemChestBlock block, BlockState state, net.minecraft.world.level.Level world, BlockPos pos, boolean ignoreBlocked) {
        GolemChestBlockEntity golemChestBlockEntity = (GolemChestBlockEntity) world.getBlockEntity(pos);
        if (golemChestBlockEntity == null) {
            return null;
        }
        if (!ignoreBlocked && isBlocked(world, pos)) {
            return null;
        }
        net.minecraft.world.level.block.state.properties.ChestType chestType = state.getValue(GolemChestBlock.CHEST_TYPE);
        if (chestType == net.minecraft.world.level.block.state.properties.ChestType.SINGLE) {
            return golemChestBlockEntity;
        }
        BlockPos blockPos = pos.relative(GolemChestBlock.getFacing(state));
        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.is(state.getBlock())) {
            net.minecraft.world.level.block.state.properties.ChestType chestType2 = blockState.getValue(GolemChestBlock.CHEST_TYPE);
            if (chestType2 != net.minecraft.world.level.block.state.properties.ChestType.SINGLE && chestType != chestType2 && state.getValue(GolemChestBlock.FACING) == blockState.getValue(GolemChestBlock.FACING)) {
                if (!ignoreBlocked && isBlocked(world, blockPos)) {
                    return null;
                }
                GolemChestBlockEntity golemChestBlockEntity2 = (GolemChestBlockEntity) world.getBlockEntity(blockPos);
                if (golemChestBlockEntity2 != null) {
                    return chestType == net.minecraft.world.level.block.state.properties.ChestType.LEFT ? new CompoundContainer(golemChestBlockEntity, golemChestBlockEntity2) : new CompoundContainer(golemChestBlockEntity2, golemChestBlockEntity);
                }
            }
        }
        return golemChestBlockEntity;
    }

    public GolemType getGolemType() {
        if (golemType == null) {
            for (Map.Entry<GolemType, net.minecraft.world.level.block.Block> entry : UGBlocks.GOLEM_CHESTS.entrySet()) {
                if (entry.getValue() == this.getBlockState().getBlock()) {
                    golemType = entry.getKey();
                    break;
                }
            }
        }
        return golemType;
    }

    @Override
    public boolean triggerEvent(int type, int data) {
        if (type == 1) {
            if (this.level != null) {
                // For double chests, only play sound from the LEFT half to avoid duplication
                ChestType chestType = this.getBlockState().getValue(GolemChestBlock.CHEST_TYPE);
                if (chestType == ChestType.SINGLE || chestType == ChestType.LEFT) {
                    if (data > 0) {
                        this.level.playSound(null, this.worldPosition, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5f, this.level.getRandom().nextFloat() * 0.1f + 0.9f);
                    } else {
                        this.level.playSound(null, this.worldPosition, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5f, this.level.getRandom().nextFloat() * 0.1f + 0.9f);
                    }
                }
            }
        }
        return super.triggerEvent(type, data);
    }

    private static boolean isBlocked(net.minecraft.world.level.Level world, BlockPos pos) {
        return false;
    }

    @Override
    protected Component getDefaultName() {
        if (this.level != null) {
            Container inventory = GolemChestBlockEntity.getInventory((GolemChestBlock) this.getBlockState().getBlock(), this.getBlockState(), this.level, this.worldPosition, true);
            if (inventory instanceof CompoundContainer) {
                return Component.translatable(this.getBlockState().getBlock().getDescriptionId().replace("block.", "container.") + "_double");
            }
        }
        return Component.translatable(this.getBlockState().getBlock().getDescriptionId());
    }
}
