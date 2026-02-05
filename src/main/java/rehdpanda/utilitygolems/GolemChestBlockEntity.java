package rehdpanda.utilitygolems;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public class GolemChestBlockEntity extends ChestBlockEntity {
    private GolemType golemType;

    public GolemChestBlockEntity(BlockPos pos, BlockState state) {
        super(UGBlocks.GOLEM_CHEST_BLOCK_ENTITY, pos, state);
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
