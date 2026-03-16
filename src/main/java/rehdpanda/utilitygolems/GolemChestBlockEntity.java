package rehdpanda.utilitygolems;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.ChestType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.ChestBlock;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class GolemChestBlockEntity extends ChestBlockEntity {
    private GolemType golemType;
    private boolean golemDead = false;
    private int transferCooldown = -1;

    public GolemChestBlockEntity(BlockPos pos, BlockState state) {
        super(UGBlocks.GOLEM_CHEST_BLOCK_ENTITY, pos, state);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, GolemChestBlockEntity blockEntity) {
        if (blockEntity.getGolemType() == GolemType.HOPPER) {
            blockEntity.transferCooldown--;
            if (blockEntity.transferCooldown <= 0) {
                blockEntity.transferCooldown = 0;
                if (blockEntity.transferItems(world, pos, state, blockEntity)) {
                    blockEntity.transferCooldown = 16;
                    blockEntity.markDirty();
                }
            }
        }
    }

    private boolean transferItems(World world, BlockPos pos, BlockState state, GolemChestBlockEntity blockEntity) {
        if (!world.isClient()) {
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
        for (int i = 0; i < this.size(); ++i) {
            ItemStack itemStack = this.getStack(i);
            if (itemStack.isEmpty() || itemStack.getCount() < itemStack.getMaxCount()) {
                return false;
            }
        }
        return true;
    }

    private static boolean pushItemsOut(World world, BlockPos pos, BlockState state, GolemChestBlockEntity blockEntity) {
        BlockPos downPos = pos.down();
        Inventory targetInventory = getInventoryAt(world, downPos);
        if (targetInventory != null) {
            Direction direction = Direction.UP;
            for (int i = 0; i < blockEntity.size(); ++i) {
                if (!blockEntity.getStack(i).isEmpty()) {
                    ItemStack itemStack = blockEntity.getStack(i).copy();
                    ItemStack toTransfer = blockEntity.removeStack(i, 1);
                    ItemStack itemStack2 = transfer(blockEntity, targetInventory, toTransfer, direction);
                    if (itemStack2.isEmpty()) {
                        targetInventory.markDirty();
                        return true;
                    }
                    blockEntity.setStack(i, itemStack);
                }
            }
        }
        return false;
    }

    private static boolean pullItemsIn(World world, BlockPos pos, BlockState state, GolemChestBlockEntity blockEntity) {
        Inventory sourceInventory = getInventoryAt(world, pos.up());
        if (sourceInventory != null) {
            Direction direction = Direction.DOWN;
            int[] slots = getAvailableSlots(sourceInventory, direction);
            for (int slot : slots) {
                ItemStack itemStack = sourceInventory.getStack(slot);
                if (!itemStack.isEmpty() && canExtract(sourceInventory, itemStack, slot, direction)) {
                    ItemStack itemStack2 = itemStack.copy();
                    ItemStack toTransfer = sourceInventory.removeStack(slot, 1);
                    ItemStack itemStack3 = transfer(sourceInventory, blockEntity, toTransfer, null);
                    if (itemStack3.isEmpty()) {
                        sourceInventory.markDirty();
                        return true;
                    }
                    sourceInventory.setStack(slot, itemStack2);
                }
            }
        }
        return false;
    }

    private static int[] getAvailableSlots(Inventory inventory, Direction side) {
        if (inventory instanceof SidedInventory sidedInventory) {
            return sidedInventory.getAvailableSlots(side);
        } else {
            int[] slots = new int[inventory.size()];
            for (int i = 0; i < slots.length; ++i) {
                slots[i] = i;
            }
            return slots;
        }
    }

    private static boolean canExtract(Inventory inventory, ItemStack stack, int slot, Direction side) {
        return !(inventory instanceof SidedInventory sidedInventory) || sidedInventory.canExtract(slot, stack, side);
    }

    private static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, @Nullable Direction side) {
        if (to instanceof SidedInventory sidedInventory && side != null) {
            int[] slots = sidedInventory.getAvailableSlots(side);
            for (int i = 0; i < slots.length && !stack.isEmpty(); ++i) {
                stack = transfer(from, to, stack, slots[i], side);
            }
        } else {
            int size = to.size();
            for (int i = 0; i < size && !stack.isEmpty(); ++i) {
                stack = transfer(from, to, stack, i, side);
            }
        }
        return stack;
    }

    private static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, int slot, @Nullable Direction side) {
        ItemStack itemStack = to.getStack(slot);
        if (canInsert(to, stack, slot, side)) {
            boolean changed = false;
            if (itemStack.isEmpty()) {
                to.setStack(slot, stack);
                stack = ItemStack.EMPTY;
                changed = true;
            } else if (canMergeItems(itemStack, stack)) {
                int i = Math.min(to.getMaxCountPerStack(), stack.getMaxCount()) - itemStack.getCount();
                int j = Math.min(stack.getCount(), i);
                stack.decrement(j);
                itemStack.increment(j);
                changed = j > 0;
            }
            if (changed) {
                to.markDirty();
            }
        }
        return stack;
    }

    private static boolean canInsert(Inventory inventory, ItemStack stack, int slot, @Nullable Direction side) {
        if (!inventory.isValid(slot, stack)) {
            return false;
        }
        return !(inventory instanceof SidedInventory sidedInventory) || sidedInventory.canInsert(slot, stack, side);
    }

    private static boolean canMergeItems(ItemStack first, ItemStack second) {
        return first.isOf(second.getItem()) && first.getDamage() == second.getDamage() && first.getCount() < first.getMaxCount() && ItemStack.areItemsAndComponentsEqual(first, second);
    }

    @Nullable
    private static Inventory getInventoryAt(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof InventoryProvider inventoryProvider) {
            return inventoryProvider.getInventory(state, world, pos);
        }
        if (state.hasBlockEntity()) {
            net.minecraft.block.entity.BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof Inventory inventory) {
                if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock) {
                    return ChestBlock.getInventory((ChestBlock)block, state, world, pos, false);
                }
                return inventory;
            }
        }
        return null;
    }

    public void setGolemDead(boolean dead) {
        this.golemDead = dead;
        this.markDirty();
        if (this.world != null) {
            this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);

            // If double chest, notify the other half
            BlockState state = this.getCachedState();
            if (state.contains(GolemChestBlock.CHEST_TYPE)) {
                ChestType type = state.get(GolemChestBlock.CHEST_TYPE);
                if (type != ChestType.SINGLE) {
                    BlockPos otherPos = this.pos.offset(GolemChestBlock.getFacing(state));
                    net.minecraft.block.entity.BlockEntity otherBE = this.world.getBlockEntity(otherPos);
                    if (otherBE instanceof GolemChestBlockEntity otherGChest && otherGChest.golemDead != dead) {
                        otherGChest.golemDead = dead;
                        otherGChest.markDirty();
                        this.world.updateListeners(otherPos, otherGChest.getCachedState(), otherGChest.getCachedState(), 3);
                    }
                }
            }
        }
    }

    public boolean isGolemDead() {
        return golemDead;
    }

    @Override
    public void readData(net.minecraft.storage.ReadView readView) {
        super.readData(readView);
        this.golemDead = readView.getBoolean("golemDead", false);
        this.transferCooldown = readView.getInt("transferCooldown", -1);
    }

    @Override
    protected void writeData(net.minecraft.storage.WriteView writeView) {
        super.writeData(writeView);
        writeView.putBoolean("golemDead", this.golemDead);
        writeView.putInt("transferCooldown", this.transferCooldown);
    }

    @Override
    public net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket toUpdatePacket() {
        return net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.NbtCompound toInitialChunkDataNbt(net.minecraft.registry.RegistryWrapper.WrapperLookup registries) {
        return (net.minecraft.nbt.NbtCompound) this.createNbt(registries);
    }

    public static Inventory getInventory(GolemChestBlock block, BlockState state, net.minecraft.world.World world, BlockPos pos, boolean ignoreBlocked) {
        GolemChestBlockEntity golemChestBlockEntity = (GolemChestBlockEntity) world.getBlockEntity(pos);
        if (golemChestBlockEntity == null) {
            return null;
        }
        if (!ignoreBlocked && isBlocked(world, pos)) {
            return null;
        }
        net.minecraft.block.enums.ChestType chestType = state.get(GolemChestBlock.CHEST_TYPE);
        if (chestType == net.minecraft.block.enums.ChestType.SINGLE) {
            return golemChestBlockEntity;
        }
        BlockPos blockPos = pos.offset(GolemChestBlock.getFacing(state));
        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.isOf(state.getBlock())) {
            net.minecraft.block.enums.ChestType chestType2 = blockState.get(GolemChestBlock.CHEST_TYPE);
            if (chestType2 != net.minecraft.block.enums.ChestType.SINGLE && chestType != chestType2 && state.get(GolemChestBlock.FACING) == blockState.get(GolemChestBlock.FACING)) {
                if (!ignoreBlocked && isBlocked(world, blockPos)) {
                    return null;
                }
                GolemChestBlockEntity golemChestBlockEntity2 = (GolemChestBlockEntity) world.getBlockEntity(blockPos);
                if (golemChestBlockEntity2 != null) {
                    return chestType == net.minecraft.block.enums.ChestType.LEFT ? new DoubleInventory(golemChestBlockEntity, golemChestBlockEntity2) : new DoubleInventory(golemChestBlockEntity2, golemChestBlockEntity);
                }
            }
        }
        return golemChestBlockEntity;
    }

    public GolemType getGolemType() {
        if (golemType == null) {
            for (Map.Entry<GolemType, net.minecraft.block.Block> entry : UGBlocks.GOLEM_CHESTS.entrySet()) {
                if (entry.getValue() == this.getCachedState().getBlock()) {
                    golemType = entry.getKey();
                    break;
                }
            }
        }
        return golemType;
    }

    @Override
    public boolean onSyncedBlockEvent(int type, int data) {
        if (type == 1) {
            if (this.world != null) {
                // For double chests, only play sound from the LEFT half to avoid duplication
                ChestType chestType = this.getCachedState().get(GolemChestBlock.CHEST_TYPE);
                if (chestType == ChestType.SINGLE || chestType == ChestType.LEFT) {
                    if (data > 0) {
                        this.world.playSound(null, this.pos, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5f, this.world.random.nextFloat() * 0.1f + 0.9f);
                    } else {
                        this.world.playSound(null, this.pos, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5f, this.world.random.nextFloat() * 0.1f + 0.9f);
                    }
                }
            }
        }
        return super.onSyncedBlockEvent(type, data);
    }

    private static boolean isBlocked(net.minecraft.world.World world, BlockPos pos) {
        return false;
    }

    @Override
    protected Text getContainerName() {
        if (this.world != null) {
            Inventory inventory = GolemChestBlockEntity.getInventory((GolemChestBlock) this.getCachedState().getBlock(), this.getCachedState(), this.world, this.pos, true);
            if (inventory instanceof DoubleInventory) {
                return Text.translatable(this.getCachedState().getBlock().getTranslationKey().replace("block.", "container.") + "_double");
            }
        }
        return Text.translatable(this.getCachedState().getBlock().getTranslationKey());
    }
}
