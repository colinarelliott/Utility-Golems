package rehdpanda.utilitygolems.schematic;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

// Minimal loader for Sponge .schem v2 (best-effort). If parsing fails, returns null.
public class SchematicLoader {

    public static SchematicData load(Path baseDir, String fileName) {
        if (fileName == null || fileName.isEmpty()) return null;
        Path file = baseDir.resolve(fileName);
        if (!Files.isRegularFile(file)) {
            System.out.println("[DEBUG_LOG] Schematic file not found or not a regular file: " + file);
            return null;
        }
        try {
            // Prefer Sponge .schem with NBT structure; try both Sponge and old MCEdit .schematic
            SchematicData data = loadSpongeSchem(file);
            if (data == null) {
                System.out.println("[DEBUG_LOG] loadSpongeSchem returned null for " + fileName);
            }
            return data;
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] Exception loading schematic " + fileName + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static SchematicData loadSpongeSchem(Path file) {
        try {
            // NBT IO is version-mapped; method name may be NbtIo.readCompressed(Path). We'll try reflective fallback.
            Object nbt = tryReadNbt(file);
            if (nbt == null) {
                System.out.println("[DEBUG_LOG] tryReadNbt returned null for " + file);
                // Try again without compression if it failed
                try {
                    Class<?> nbtIo = Class.forName("net.minecraft.nbt.NbtIo");
                    nbt = nbtIo.getMethod("read", Path.class).invoke(null, file);
                    if (nbt != null) {
                        System.out.println("[DEBUG_LOG] Successfully read NBT using NbtIo.read (uncompressed) for " + file);
                    }
                } catch (Throwable ignored) {}
            }
            if (nbt == null) {
                System.out.println("[DEBUG_LOG] All NBT read attempts failed for " + file);
                return null;
            }
            // Accessors via reflection to avoid hard mapping ties
            Class<?> nbtCompoundCls = nbt.getClass();

            // Sponge schematics sometimes wrap the actual schematic data inside a 'Schematic' compound
            Object root = nbt;
            Object schematicTag = invokeNbtGetTag(nbt, "Schematic");
            if (schematicTag != null && schematicTag.getClass().getName().contains("Compound")) {
                System.out.println("[DEBUG_LOG] Found 'Schematic' compound, using it as root.");
                root = schematicTag;
            }

            // Check if it's Sponge format (has Palette/BlockData) or Classic format (has Blocks/Data)
            boolean isSponge = invokeNbtGetTag(root, "Palette") != null || invokeNbtGetTag(root, "BlockData") != null;
            boolean isClassic = invokeNbtGetTag(root, "Blocks") != null || invokeNbtGetTag(root, "Data") != null;

            if (isSponge) {
                System.out.println("[DEBUG_LOG] Parsing as Sponge .schem format");
                return parseSpongeFormat(root);
            } else if (isClassic) {
                System.out.println("[DEBUG_LOG] Parsing as Classic .schematic format");
                return parseClassicFormat(root);
            } else {
                System.out.println("[DEBUG_LOG] Could not determine schematic format (no Palette/BlockData or Blocks/Data found)");
                return null;
            }
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] Error parsing schematic NBT: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static SchematicData parseSpongeFormat(Object root) throws Exception {
        // Read size
        short width = ((Number) invokeNbtGet(root, "Width", short.class, (short)0)).shortValue();
        short height = ((Number) invokeNbtGet(root, "Height", short.class, (short)0)).shortValue();
        short length = ((Number) invokeNbtGet(root, "Length", short.class, (short)0)).shortValue();
        System.out.println("[DEBUG_LOG] Schematic dimensions: " + width + "x" + height + "x" + length);
        if (width <= 0 || height <= 0 || length <= 0) {
            System.out.println("[DEBUG_LOG] Invalid dimensions (<= 0)");
            return null;
        }

        // Palette map
        Object paletteTag = invokeNbtGetTag(root, "Palette");
        Map<Integer, Block> palette = new HashMap<>();
        if (paletteTag != null) {
            // Iterate keys of palette compound
            for (String key : getNbtKeys(paletteTag)) {
                int id = getIntFromCompound(paletteTag, key, -1);
                if (id >= 0) {
                    String blockName = key;
                    int bracket = blockName.indexOf('[');
                    if (bracket > 0) blockName = blockName.substring(0, bracket);
                    
                    // Handle possible minecraft: prefix
                    if (!blockName.contains(":")) blockName = "minecraft:" + blockName;

                    Identifier idf = Identifier.tryParse(blockName);
                    if (idf != null) {
                        Block block = Registries.BLOCK.get(idf);
                        palette.put(id, block);
                    }
                }
            }
        } else {
            System.out.println("[DEBUG_LOG] No Palette found in Sponge schematic!");
            return null;
        }

        // BlockData: may be a byte array or int array (varint stream in newer specs); try common cases
        int total = width * height * length;
        int[] indices = null;
        
        // WorldEdit often uses 'BlockData' (VarInt encoded byte array)
        Object bytes = invokeNbtGetByteArray(root, "BlockData");
        if (bytes instanceof byte[] barr) {
            System.out.println("[DEBUG_LOG] Found BlockData as byte array, length: " + barr.length);
            if (barr.length == total) {
                indices = new int[total];
                for (int i = 0; i < total; i++) indices[i] = barr[i] & 0xFF;
            } else {
                // Sponge V2/V3 often uses VarInt encoding for BlockData
                indices = decodeVarInt(barr, total);
                System.out.println("[DEBUG_LOG] Decoded VarInt BlockData, length: " + (indices != null ? indices.length : "null"));
            }
        }
        
        if (indices == null) {
            Object intArray = invokeNbtGetIntArray(root, "BlockData");
            if (intArray instanceof int[] arr) {
                System.out.println("[DEBUG_LOG] Found BlockData as int array, length: " + arr.length);
                indices = arr;
            }
        }

        if (indices == null) {
            System.out.println("[DEBUG_LOG] No BlockData found in Sponge schematic!");
            return null;
        }
        // Ensure indices is at least 'total' long
        if (indices.length < total) {
            System.out.println("[DEBUG_LOG] BlockData length (" + indices.length + ") is less than expected total (" + total + "), padding with 0");
            int[] newIndices = new int[total];
            System.arraycopy(indices, 0, newIndices, 0, indices.length);
            indices = newIndices;
        }

        return new SchematicData(width, height, length, indices, palette);
    }

    private static SchematicData parseClassicFormat(Object root) throws Exception {
        // Classic MCEdit .schematic
        short width = ((Number) invokeNbtGet(root, "Width", short.class, (short)0)).shortValue();
        short height = ((Number) invokeNbtGet(root, "Height", short.class, (short)0)).shortValue();
        short length = ((Number) invokeNbtGet(root, "Length", short.class, (short)0)).shortValue();
        System.out.println("[DEBUG_LOG] Classic schematic dimensions: " + width + "x" + height + "x" + length);
        if (width <= 0 || height <= 0 || length <= 0) return null;

        Object blocksBytes = invokeNbtGetByteArray(root, "Blocks");
        Object dataBytes = invokeNbtGetByteArray(root, "Data");
        Object addBlocksBytes = invokeNbtGetByteArray(root, "AddBlocks");

        if (!(blocksBytes instanceof byte[] blocks)) {
            System.out.println("[DEBUG_LOG] Classic schematic missing 'Blocks' byte array");
            return null;
        }

        int total = width * height * length;
        int[] indices = new int[total];
        Map<Integer, Block> palette = new HashMap<>();

        byte[] add = addBlocksBytes instanceof byte[] ? (byte[]) addBlocksBytes : null;
        
        // In classic format, block IDs are split into Blocks and AddBlocks (4-bit offset)
        // Data is metadata (sub-type)
        // For simplicity with Diamond Golem, we'll mostly map the base Block ID.
        // We need a numeric ID -> Block mapping. This is hard in 1.21 but many mods use legacy mappings.
        // For now, let's map common IDs or just use the most common blocks.
        
        for (int i = 0; i < Math.min(total, blocks.length); i++) {
            int blockId = blocks[i] & 0xFF;
            if (add != null) {
                int addId = (add[i / 2] >> ((i % 2 == 0) ? 4 : 0)) & 0x0F;
                blockId |= (addId << 8);
            }
            indices[i] = blockId;
            if (!palette.containsKey(blockId)) {
                palette.put(blockId, mapLegacyIdToBlock(blockId));
            }
        }

        return new SchematicData(width, height, length, indices, palette);
    }

    private static Block mapLegacyIdToBlock(int id) {
        // Very basic legacy mapping for common building blocks
        switch (id) {
            case 0: return net.minecraft.block.Blocks.AIR;
            case 1: return net.minecraft.block.Blocks.STONE;
            case 2: return net.minecraft.block.Blocks.GRASS_BLOCK;
            case 3: return net.minecraft.block.Blocks.DIRT;
            case 4: return net.minecraft.block.Blocks.COBBLESTONE;
            case 5: return net.minecraft.block.Blocks.OAK_PLANKS;
            case 6: return net.minecraft.block.Blocks.OAK_SAPLING;
            case 7: return net.minecraft.block.Blocks.BEDROCK;
            case 12: return net.minecraft.block.Blocks.SAND;
            case 13: return net.minecraft.block.Blocks.GRAVEL;
            case 14: return net.minecraft.block.Blocks.GOLD_ORE;
            case 15: return net.minecraft.block.Blocks.IRON_ORE;
            case 16: return net.minecraft.block.Blocks.COAL_ORE;
            case 17: return net.minecraft.block.Blocks.OAK_LOG;
            case 18: return net.minecraft.block.Blocks.OAK_LEAVES;
            case 20: return net.minecraft.block.Blocks.GLASS;
            case 24: return net.minecraft.block.Blocks.SANDSTONE;
            case 35: return net.minecraft.block.Blocks.WHITE_WOOL;
            case 41: return net.minecraft.block.Blocks.GOLD_BLOCK;
            case 42: return net.minecraft.block.Blocks.IRON_BLOCK;
            case 43: return net.minecraft.block.Blocks.STONE_SLAB;
            case 44: return net.minecraft.block.Blocks.STONE_SLAB;
            case 45: return net.minecraft.block.Blocks.BRICKS;
            case 48: return net.minecraft.block.Blocks.MOSSY_COBBLESTONE;
            case 49: return net.minecraft.block.Blocks.OBSIDIAN;
            case 56: return net.minecraft.block.Blocks.DIAMOND_ORE;
            case 57: return net.minecraft.block.Blocks.DIAMOND_BLOCK;
            case 87: return net.minecraft.block.Blocks.NETHERRACK;
            case 88: return net.minecraft.block.Blocks.SOUL_SAND;
            case 89: return net.minecraft.block.Blocks.GLOWSTONE;
            case 98: return net.minecraft.block.Blocks.STONE_BRICKS;
            case 121: return net.minecraft.block.Blocks.END_STONE;
            case 155: return net.minecraft.block.Blocks.QUARTZ_BLOCK;
            default: return net.minecraft.block.Blocks.STONE; // Fallback
        }
    }

    private static int[] decodeVarInt(byte[] data, int expectedTotal) {
        int[] result = new int[expectedTotal];
        int index = 0;
        int i = 0;
        try {
            while (i < data.length && index < expectedTotal) {
                int value = 0;
                int varintLength = 0;
                while (i < data.length) {
                    byte b = data[i++];
                    value |= (b & 127) << (varintLength++ * 7);
                    if ((b & 128) != 128) break;
                }
                result[index++] = value;
            }
        } catch (Exception ignored) {}
        return result;
    }

    private static Object tryReadNbt(Path file) {
        try {
            Class<?> nbtIo = Class.forName("net.minecraft.nbt.NbtIo");
            // In 1.21 it might be readCompressed(InputStream, NbtSizeTracker) or similar
            // Try common methods
            
            // Try uncompressed first just in case
            try {
                return nbtIo.getMethod("read", Path.class).invoke(null, file);
            } catch (Throwable ignored) {}

            try {
                // Method 1: readCompressed(Path, NbtSizeTracker) - common in 1.20.5+
                return nbtIo.getMethod("readCompressed", Path.class, net.minecraft.nbt.NbtSizeTracker.class).invoke(null, file, net.minecraft.nbt.NbtSizeTracker.ofUnlimitedBytes());
            } catch (Throwable e) {
                // System.out.println("[DEBUG_LOG] NbtIo.readCompressed(Path, NbtSizeTracker) failed: " + e.getMessage());
            }

            try {
                // Method 2: readCompressed(Path)
                return nbtIo.getMethod("readCompressed", Path.class).invoke(null, file);
            } catch (Throwable e) {
                // System.out.println("[DEBUG_LOG] NbtIo.readCompressed(Path) failed: " + e.getMessage());
            }

            try {
                // Method 3: readCompressed(InputStream, NbtSizeTracker)
                try (InputStream is = Files.newInputStream(file)) {
                    return nbtIo.getMethod("readCompressed", InputStream.class, net.minecraft.nbt.NbtSizeTracker.class).invoke(null, is, net.minecraft.nbt.NbtSizeTracker.ofUnlimitedBytes());
                }
            } catch (Throwable e) {
                // System.out.println("[DEBUG_LOG] NbtIo.readCompressed(InputStream, NbtSizeTracker) failed: " + e.getMessage());
            }

            try {
                // Method 4: readCompressed(InputStream)
                try (InputStream is = Files.newInputStream(file)) {
                    return nbtIo.getMethod("readCompressed", InputStream.class).invoke(null, is);
                }
            } catch (Throwable e) {
                // System.out.println("[DEBUG_LOG] NbtIo.readCompressed(InputStream) failed: " + e.getMessage());
            }
        } catch (Throwable e) {
            System.out.println("[DEBUG_LOG] General failure in tryReadNbt: " + e.getMessage());
        }
        return null;
    }

    private static Object invokeNbtGetTag(Object compound, String key) throws Exception {
        Class<?> cls = compound.getClass();
        try {
            return cls.getMethod("get", String.class).invoke(compound, key);
        } catch (Throwable e) {
            // Some versions use getCompound for compound children if they know it's a compound
            try {
                return cls.getMethod("getCompound", String.class).invoke(compound, key);
            } catch (Throwable ex) {
                return null;
            }
        }
    }

    private static Number invokeNbtGet(Object compound, String key, Class<?> numberCls, Number def) throws Exception {
        Class<?> cls = compound.getClass();
        try {
            if (numberCls == short.class) {
                return (Short) cls.getMethod("getShort", String.class).invoke(compound, key);
            } else if (numberCls == int.class) {
                return (Integer) cls.getMethod("getInt", String.class).invoke(compound, key);
            } else if (numberCls == byte.class) {
                return (Byte) cls.getMethod("getByte", String.class).invoke(compound, key);
            }
        } catch (Throwable ignored) {}

        Object tag = invokeNbtGetTag(compound, key);
        if (tag == null) return def;
        try {
            if (numberCls == short.class) {
                return (Short) tag.getClass().getMethod("shortValue").invoke(tag);
            } else if (numberCls == int.class) {
                return (Integer) tag.getClass().getMethod("intValue").invoke(tag);
            } else if (numberCls == byte.class) {
                return (Byte) tag.getClass().getMethod("byteValue").invoke(tag);
            }
        } catch (Throwable ignored) {}
        
        // Fallback try toString parse
        try {
            String s = tag.toString();
            if (s.endsWith("s") || s.endsWith("b") || s.endsWith("L")) {
                s = s.substring(0, s.length() - 1);
            }
            return Integer.parseInt(s);
        } catch (Throwable ignored) {}
        return def;
    }

    private static int getIntFromCompound(Object compound, String key, int def) throws Exception {
        try {
            return (Integer) compound.getClass().getMethod("getInt", String.class).invoke(compound, key);
        } catch (Throwable e) {
            try {
                Object tag = invokeNbtGetTag(compound, key);
                if (tag != null) {
                    return (Integer) tag.getClass().getMethod("intValue").invoke(tag);
                }
            } catch (Throwable ignored) {}
        }
        return def;
    }

    private static java.util.Set<String> getNbtKeys(Object compound) throws Exception {
        try {
            return (java.util.Set<String>) compound.getClass().getMethod("getKeys").invoke(compound);
        } catch (Throwable ignored) {}
        try {
            return (java.util.Set<String>) compound.getClass().getMethod("getKeys", new Class<?>[]{}).invoke(compound);
        } catch (Throwable ignored) {}
        return java.util.Collections.emptySet();
    }

    private static Object invokeNbtGetIntArray(Object compound, String key) {
        try {
            return compound.getClass().getMethod("getIntArray", String.class).invoke(compound, key);
        } catch (Throwable e) {
            try {
                Object tag = invokeNbtGetTag(compound, key);
                if (tag != null) {
                    return tag.getClass().getMethod("getIntArray").invoke(tag);
                }
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private static Object invokeNbtGetByteArray(Object compound, String key) {
        try {
            return compound.getClass().getMethod("getByteArray", String.class).invoke(compound, key);
        } catch (Throwable e) {
            // If getByteArray fails, try get(key) and then check if it's a ByteArrayTag
            try {
                Object tag = invokeNbtGetTag(compound, key);
                if (tag != null) {
                    // net.minecraft.nbt.NbtByteArray.getByteArray()
                    return tag.getClass().getMethod("getByteArray").invoke(tag);
                }
            } catch (Throwable ignored) {}
        }
        return null;
    }
}
