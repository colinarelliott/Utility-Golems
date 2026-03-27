package rehdpanda.utilitygolems;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class RedstoneGolemStatueBlockEntity extends BlockEntity {
    private Component customName;

    public RedstoneGolemStatueBlockEntity(BlockPos pos, BlockState state) {
        super(UGBlocks.REDSTONE_GOLEM_STATUE_BLOCK_ENTITY, pos, state);
    }

    public void setCustomName(Component customName) {
        this.customName = customName;
        this.setChanged();
    }

    public Component getCustomName() {
        return this.customName;
    }

    @Override
    public void loadAdditional(ValueInput readView) {
        super.loadAdditional(readView);
        String name = readView.getStringOr("CustomName", "");
        if (!name.isEmpty()) {
            this.customName = Component.literal(name);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput writeView) {
        super.saveAdditional(writeView);
        if (this.customName != null) {
            writeView.putString("CustomName", this.customName.getString());
        }
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockSynchedEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockSynchedEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return (CompoundTag) this.saveWithoutMetadata(registries);
    }
}
