package me.illgilp.worldeditglobalizerbukkit.clipboard;

import com.google.common.base.Preconditions;
import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BaseBlock;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

public class WEGSpongeSchematicWriter implements ClipboardWriter {
    private static final int MAX_SIZE = 65535;
    private final NBTOutputStream outputStream;

    public WEGSpongeSchematicWriter(NBTOutputStream outputStream) {
        Preconditions.checkNotNull(outputStream);
        this.outputStream = outputStream;
    }

    public void write(Clipboard clipboard) throws IOException {
        this.outputStream.writeNamedTag("Schematic", new CompoundTag(this.write1(clipboard)));
    }

    private Map<String, Tag> write1(Clipboard clipboard) throws IOException {
        Region region = clipboard.getRegion();
        Vector origin = clipboard.getOrigin();
        Vector min = region.getMinimumPoint();
        Vector offset = min.subtract(origin);
        int width = region.getWidth();
        int height = region.getHeight();
        int length = region.getLength();
        if (width > 65535) {
            throw new IllegalArgumentException("Width of region too large for a .schematic");
        } else if (height > 65535) {
            throw new IllegalArgumentException("Height of region too large for a .schematic");
        } else if (length > 65535) {
            throw new IllegalArgumentException("Length of region too large for a .schematic");
        } else {
            Map<String, Tag> schematic = new HashMap();
            schematic.put("Version", new IntTag(1));
            Map<String, Tag> metadata = new HashMap();
            metadata.put("WEOffsetX", new IntTag(offset.getBlockX()));
            metadata.put("WEOffsetY", new IntTag(offset.getBlockY()));
            metadata.put("WEOffsetZ", new IntTag(offset.getBlockZ()));
            schematic.put("Metadata", new CompoundTag(metadata));
            schematic.put("Width", new ShortTag((short)width));
            schematic.put("Height", new ShortTag((short)height));
            schematic.put("Length", new ShortTag((short)length));
            schematic.put("Offset", new IntArrayTag(new int[]{min.getBlockX(), min.getBlockY(), min.getBlockZ()}));
            int paletteMax = 0;
            Map<String, Integer> palette = new HashMap();
            List<CompoundTag> tileEntities = new ArrayList();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(width * height * length);

            for(int y = 0; y < height; ++y) {
                int y0 = min.getBlockY() + y;

                for(int z = 0; z < length; ++z) {
                    int z0 = min.getBlockZ() + z;

                    for(int x = 0; x < width; ++x) {
                        int x0 = min.getBlockX() + x;
                        BlockVector point = new BlockVector(x0, y0, z0);
                        BaseBlock block = clipboard.getFullBlock(point);
                        if (block.getNbtData() != null) {
                            Map<String, Tag> values = new HashMap();
                            Iterator var24 = block.getNbtData().getValue().entrySet().iterator();

                            while(var24.hasNext()) {
                                Entry<String, Tag> entry = (Entry)var24.next();
                                values.put(entry.getKey(), entry.getValue());
                            }

                            values.remove("id");
                            values.remove("x");
                            values.remove("y");
                            values.remove("z");
                            values.put("Id", new StringTag(block.getNbtId()));
                            values.put("Pos", new IntArrayTag(new int[]{x, y, z}));
                            tileEntities.add(new CompoundTag(values));
                        }

                        String blockKey = block.toImmutableState().getAsString();
                        int blockId;
                        if (palette.containsKey(blockKey)) {
                            blockId = (Integer)palette.get(blockKey);
                        } else {
                            blockId = paletteMax;
                            palette.put(blockKey, paletteMax);
                            ++paletteMax;
                        }

                        while((blockId & -128) != 0) {
                            buffer.write(blockId & 127 | 128);
                            blockId >>>= 7;
                        }

                        buffer.write(blockId);
                    }
                }
            }

            schematic.put("PaletteMax", new IntTag(paletteMax));
            Map<String, Tag> paletteTag = new HashMap();
            palette.forEach((key, value) -> {
                Tag var10000 = (Tag)paletteTag.put(key, new IntTag(value));
            });
            schematic.put("Palette", new CompoundTag(paletteTag));
            schematic.put("BlockData", new ByteArrayTag(buffer.toByteArray()));
            schematic.put("TileEntities", new ListTag(CompoundTag.class, tileEntities));
            return schematic;
        }
    }

    public void close() throws IOException {
        this.outputStream.close();
    }
}
