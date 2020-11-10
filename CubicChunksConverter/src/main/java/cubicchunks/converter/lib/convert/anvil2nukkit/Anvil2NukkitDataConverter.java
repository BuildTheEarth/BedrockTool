package cubicchunks.converter.lib.convert.anvil2nukkit;

import cn.nukkit.block.BlockID;
import com.flowpowered.nbt.ByteArrayTag;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.ListTag;
import cubicchunks.converter.lib.convert.ChunkDataConverter;
import cubicchunks.converter.lib.convert.data.AnvilChunkData;
import cubicchunks.converter.lib.convert.data.NukkitChunkData;
import cubicchunks.converter.lib.util.NibbleArray;
import cubicchunks.converter.lib.util.Utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author DaPorkchop_
 */
public class Anvil2NukkitDataConverter implements ChunkDataConverter<AnvilChunkData, NukkitChunkData> {
    private static int id(int block, int meta) {
        return (block << 4) | meta;
    }

    private static int keepMeta(int id, int newBlock) {
        return (id & 0xF) | (newBlock << 4);
    }

    private static int keepId(int id, int newMeta) {
        return (id & ~0xF) | newMeta;
    }

    private static int fixId(int id) {
        int meta = id & 0xF;
        switch (id >> 4) {
            case 3:
                if (meta == 2) {
                    return id(BlockID.PODZOL, 0);
                }
                break;
            case 125:
                return keepMeta(id, BlockID.DOUBLE_WOODEN_SLAB);
            case 126:
                return keepMeta(id, BlockID.WOOD_SLAB);
            case 95:
                return keepMeta(id, 241);
            case 157:
                return keepMeta(id, 126);
            case 158:
                return keepMeta(id, 125);
            case 160:
                return keepMeta(id, BlockID.GLASS_PANE);
            case 166:
                return keepMeta(id, BlockID.INVISIBLE_BEDROCK);
            case 177:
                return keepMeta(id, BlockID.AIR);
            case 188:
                return id(BlockID.FENCE, 1);
            case 189:
                return id(BlockID.FENCE, 2);
            case 190:
                return id(BlockID.FENCE, 3);
            case 191:
                return id(BlockID.FENCE, 4);
            case 192:
                return id(BlockID.FENCE, 5);
            case 198:
                return keepMeta(id, BlockID.END_ROD);
            case 199:
                return keepMeta(id, BlockID.CHORUS_PLANT);
            case 202: //pillar
            case 204: //double slab
            case 205: //slab
                return id(BlockID.PURPUR_BLOCK, 0);
            case 207:
                return keepMeta(id, BlockID.BEETROOT_BLOCK);
            case 208:
                return keepMeta(id, BlockID.GRASS_PATH);
            case 210: //repeating command block
                return id(188, 0);
            case 211: //chain command block
                return id(189, 0);
            case 218:
                return keepMeta(id, BlockID.OBSERVER);
            case 235:
                return keepMeta(id, BlockID.WHITE_GLAZED_TERRACOTTA);
            case 236:
                return keepMeta(id, BlockID.ORANGE_GLAZED_TERRACOTTA);
            case 237:
                return keepMeta(id, BlockID.MAGENTA_GLAZED_TERRACOTTA);
            case 238:
                return keepMeta(id, BlockID.LIGHT_BLUE_GLAZED_TERRACOTTA);
            case 239:
                return keepMeta(id, BlockID.YELLOW_GLAZED_TERRACOTTA);
            case 240:
                return keepMeta(id, BlockID.LIME_GLAZED_TERRACOTTA);
            case 241:
                return keepMeta(id, BlockID.PINK_GLAZED_TERRACOTTA);
            case 242:
                return keepMeta(id, BlockID.GRAY_GLAZED_TERRACOTTA);
            case 243:
                return keepMeta(id, BlockID.SILVER_GLAZED_TERRACOTTA);
            case 244:
                return keepMeta(id, BlockID.CYAN_GLAZED_TERRACOTTA);
            case 245:
                return keepMeta(id, BlockID.PURPLE_GLAZED_TERRACOTTA);
            case 246:
                return keepMeta(id, BlockID.BLUE_GLAZED_TERRACOTTA);
            case 247:
                return keepMeta(id, BlockID.BROWN_GLAZED_TERRACOTTA);
            case 248:
                return keepMeta(id, BlockID.GREEN_GLAZED_TERRACOTTA);
            case 249:
                return keepMeta(id, BlockID.RED_GLAZED_TERRACOTTA);
            case 250:
                return keepMeta(id, BlockID.BLACK_GLAZED_TERRACOTTA);
            case 251:
                return keepMeta(id, BlockID.CONCRETE);
            case 252:
                return keepMeta(id, BlockID.CONCRETE_POWDER);
            case BlockID.STONE_BUTTON:
            case BlockID.WOODEN_BUTTON:
                int face = id & 0x7;

                switch (face) {
                    case 0: //down
                        face = 0;
                        break;
                    case 1: //east
                        face = 5;
                        break;
                    case 2: //west
                        face = 4;
                        break;
                    case 3: //south
                        face = 3;
                        break;
                    case 4: //north
                        face = 2;
                        break;
                    case 5: //up
                        face = 1;
                        break;
                }

                return keepMeta(id, face | (id & 0x8));
        }
        if (id >= 219 && id <= 234) { //shulker box
            return id(BlockID.SHULKER_BOX, (id >> 4) - 219);
        }
        return id;
    }

    private static int getAnvilIndex(int x, int y, int z) {
        return (y << 8) | (z << 4) | x;
    }

    private static int fixSection(CompoundTag section) {
        byte[] blocks = ((ByteArrayTag) section.getValue().get("Blocks")).getValue();
        byte[] data = ((ByteArrayTag) section.getValue().get("Data")).getValue();
        NibbleArray meta = new NibbleArray(data);

        int changed = 0;
        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) { //minimize cache misses
                    int index = getAnvilIndex(x, y, z);
                    int oldId = ((blocks[index] & 0xFF) << 4) | meta.get(index);
                    int newId = fixId(oldId);
                    if (newId != oldId) {
                        blocks[index] = (byte) (newId >> 4);
                        meta.set(index, newId & 0xF);
                        changed++;
                    }
                }
            }
        }
        return changed;
    }

    @Override
    @SuppressWarnings("unchecked")
    public NukkitChunkData convert(AnvilChunkData input) {
        try {
            CompoundTag tag = Utils.readCompressed(new ByteArrayInputStream(input.getData().array()));
            boolean dirty = ((ListTag<CompoundTag>) ((CompoundTag) tag.getValue().get("Level")).getValue().get("Sections")).getValue().stream()
                    .mapToInt(Anvil2NukkitDataConverter::fixSection)
                    .max().orElse(0) != 0;
            return new NukkitChunkData(input.getDimension(), input.getPosition(), dirty ? Utils.writeCompressedZlib(tag, true) : input.getData());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
