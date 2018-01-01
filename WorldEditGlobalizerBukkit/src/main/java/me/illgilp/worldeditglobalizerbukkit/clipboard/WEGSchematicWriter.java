package me.illgilp.worldeditglobalizerbukkit.clipboard;


import com.google.common.base.Preconditions;
import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.DoubleTag;
import com.sk89q.jnbt.FloatTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.registry.WorldData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class WEGSchematicWriter implements ClipboardWriter {
    private static final int MAX_SIZE = 65535;
    private final NBTOutputStream outputStream;

    public WEGSchematicWriter(NBTOutputStream outputStream) {
        Preconditions.checkNotNull(outputStream);
        this.outputStream = outputStream;
    }

    public void write(Clipboard clipboard, WorldData data) throws IOException {
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
            HashMap<String, Tag> schematic = new HashMap();
            schematic.put("Width", new ShortTag((short)width));
            schematic.put("Length", new ShortTag((short)length));
            schematic.put("Height", new ShortTag((short)height));
            schematic.put("Materials", new StringTag("Alpha"));
            schematic.put("WEOriginX", new IntTag(min.getBlockX()));
            schematic.put("WEOriginY", new IntTag(min.getBlockY()));
            schematic.put("WEOriginZ", new IntTag(min.getBlockZ()));
            schematic.put("WEOffsetX", new IntTag(offset.getBlockX()));
            schematic.put("WEOffsetY", new IntTag(offset.getBlockY()));
            schematic.put("WEOffsetZ", new IntTag(offset.getBlockZ()));
            byte[] blocks = new byte[width * height * length];
            byte[] addBlocks = null;
            byte[] blockData = new byte[width * height * length];
            List<Tag> tileEntities = new ArrayList();
            Iterator var15 = region.iterator();

            while(true) {
                int x;
                int y;
                int z;
                BaseBlock block;
                CompoundTag rawTag;
                do {
                    if (!var15.hasNext()) {
                        schematic.put("Blocks", new ByteArrayTag(blocks));
                        schematic.put("Data", new ByteArrayTag(blockData));
                        schematic.put("TileEntities", new ListTag(CompoundTag.class, tileEntities));
                        if (addBlocks != null) {
                            schematic.put("AddBlocks", new ByteArrayTag(addBlocks));
                        }

                        List<Tag> entities = new ArrayList();
                        Iterator var28 = clipboard.getEntities().iterator();

                        while(var28.hasNext()) {
                            Entity entity = (Entity)var28.next();
                            BaseEntity state = entity.getState();
                            if (state != null) {
                                Map<String, Tag> values = new HashMap();
                                rawTag = state.getNbtData();
                                if (rawTag != null) {
                                    values.putAll(rawTag.getValue());
                                }

                                values.put("id", new StringTag(state.getTypeId()));
                                values.put("Pos", this.writeVector(entity.getLocation().toVector(), "Pos"));
                                values.put("Rotation", this.writeRotation(entity.getLocation(), "Rotation"));
                                CompoundTag entityTag = new CompoundTag(values);
                                entities.add(entityTag);
                            }
                        }

                        schematic.put("Entities", new ListTag(CompoundTag.class, entities));
                        CompoundTag schematicTag = new CompoundTag(schematic);
                        this.outputStream.writeNamedTag("Schematic", schematicTag);
                        return;
                    }

                    Vector point = (Vector)var15.next();
                    Vector relative = point.subtract(min);
                    x = relative.getBlockX();
                    y = relative.getBlockY();
                    z = relative.getBlockZ();
                    int index = y * width * length + z * width + x;
                    block = clipboard.getBlock(point);
                    if (block.getType() > 255) {
                        if (addBlocks == null) {
                            addBlocks = new byte[(blocks.length >> 1) + 1];
                        }

                        addBlocks[index >> 1] = (byte)((index & 1) == 0 ? addBlocks[index >> 1] & 240 | block.getType() >> 8 & 15 : addBlocks[index >> 1] & 15 | (block.getType() >> 8 & 15) << 4);
                    }

                    blocks[index] = (byte)block.getType();
                    blockData[index] = (byte)block.getData();
                    rawTag = block.getNbtData();
                } while(rawTag == null);

                Map<String, Tag> values = new HashMap();
                Iterator var25 = rawTag.getValue().entrySet().iterator();

                while(var25.hasNext()) {
                    Entry<String, Tag> entry = (Entry)var25.next();
                    values.put(entry.getKey(), entry.getValue());
                }

                values.put("id", new StringTag(block.getNbtId()));
                values.put("x", new IntTag(x));
                values.put("y", new IntTag(y));
                values.put("z", new IntTag(z));
                CompoundTag tileEntityTag = new CompoundTag(values);
                tileEntities.add(tileEntityTag);
            }
        }
    }

    private Tag writeVector(Vector vector, String name) {
        List<DoubleTag> list = new ArrayList();
        list.add(new DoubleTag(vector.getX()));
        list.add(new DoubleTag(vector.getY()));
        list.add(new DoubleTag(vector.getZ()));
        return new ListTag(DoubleTag.class, list);
    }

    private Tag writeRotation(Location location, String name) {
        List<FloatTag> list = new ArrayList();
        list.add(new FloatTag(location.getYaw()));
        list.add(new FloatTag(location.getPitch()));
        return new ListTag(FloatTag.class, list);
    }

    public void close() throws IOException {
        this.outputStream.close();
    }
}
