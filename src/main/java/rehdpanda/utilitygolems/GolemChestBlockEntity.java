package rehdpanda.utilitygolems;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public class GolemChestBlockEntity extends ChestBlockEntity {
    private GolemType golemType;

    public GolemChestBlockEntity(BlockPos pos, BlockState state) {
        super(UGBlocks.GOLEM_CHEST_BLOCK_ENTITY, pos, state);
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
    protected Text getContainerName() {
        return Text.translatable(this.getCachedState().getBlock().getTranslationKey());
    }
}
