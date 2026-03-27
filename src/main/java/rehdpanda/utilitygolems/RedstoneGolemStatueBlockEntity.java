package rehdpanda.utilitygolems;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.nbt.NbtCompound;

public class RedstoneGolemStatueBlockEntity extends BlockEntity {
    private Text customName;

    public RedstoneGolemStatueBlockEntity(BlockPos pos, BlockState state) {
        super(UGBlocks.REDSTONE_GOLEM_STATUE_BLOCK_ENTITY, pos, state);
    }

    public void setCustomName(Text customName) {
        this.customName = customName;
        this.markDirty();
    }

    public Text getCustomName() {
        return this.customName;
    }

    @Override
    public void readData(ReadView readView) {
        super.readData(readView);
        String name = readView.getString("CustomName", "");
        if (!name.isEmpty()) {
            this.customName = Text.literal(name);
        }
    }

    @Override
    protected void writeData(WriteView writeView) {
        super.writeData(writeView);
        if (this.customName != null) {
            writeView.putString("CustomName", this.customName.getString());
        }
    }

    @Override
    public net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket toUpdatePacket() {
        return net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return (NbtCompound) this.createNbt(registries);
    }
}
