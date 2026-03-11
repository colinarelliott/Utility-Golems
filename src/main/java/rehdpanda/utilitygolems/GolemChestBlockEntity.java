package rehdpanda.utilitygolems;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.ChestType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.text.Text;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public class GolemChestBlockEntity extends ChestBlockEntity {
    private GolemType golemType;
    private boolean golemDead = false;

    public GolemChestBlockEntity(BlockPos pos, BlockState state) {
        super(UGBlocks.GOLEM_CHEST_BLOCK_ENTITY, pos, state);
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
    }

    @Override
    protected void writeData(net.minecraft.storage.WriteView writeView) {
        super.writeData(writeView);
        writeView.putBoolean("golemDead", this.golemDead);
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
