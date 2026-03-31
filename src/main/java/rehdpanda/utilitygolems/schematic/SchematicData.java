package rehdpanda.utilitygolems.schematic;

import net.minecraft.world.level.block.Block;

import java.util.Map;

public class SchematicData {
    public final int width;
    public final int height;
    public final int length;
    private final int[] blockIndices; // index into palette
    private final Map<Integer, Block> palette; // simplified: map palette id -> Block (default state used)

    public SchematicData(int width, int height, int length, int[] blockIndices, Map<Integer, Block> palette) {
        this.width = width;
        this.height = height;
        this.length = length;
        this.blockIndices = blockIndices;
        this.palette = palette;
    }

    public int getTotalBlocks() {
        return blockIndices == null ? 0 : blockIndices.length;
    }

    public Block getBlockAtIndex(int linearIndex) {
        if (linearIndex < 0 || linearIndex >= blockIndices.length) return null;
        int id = blockIndices[linearIndex];
        return palette.get(id);
    }

    public int indexToX(int index) {
        // Sponge .schem typically uses order: (y * length + z) * width + x
        // x changes fastest, then z, then y
        return index % width;
    }

    public int indexToY(int index) {
        return index / (width * length);
    }

    public int indexToZ(int index) {
        return (index / width) % length;
    }
}
