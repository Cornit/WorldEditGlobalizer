package me.illgilp.worldeditglobalizersponge.clipboard;


import com.google.common.base.Preconditions;
import com.sk89q.jnbt.*;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.registry.WorldData;
import com.sk89q.worldedit.world.storage.NBTConversions;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WEGSchematicReader implements ClipboardReader {
    private static final Logger log = Logger.getLogger(com.sk89q.worldedit.extent.clipboard.io.SchematicReader.class.getCanonicalName());
    private final NBTInputStream inputStream;

    public WEGSchematicReader(NBTInputStream inputStream) {
        Preconditions.checkNotNull(inputStream);
        this.inputStream = inputStream;
    }

    public Clipboard read(WorldData data) throws IOException {
        NamedTag rootTag = this.inputStream.readNamedTag();
        if (!rootTag.getName().equals("Schematic")) {
            throw new IOException("Tag 'Schematic' does not exist or is not first");
        } else {
            CompoundTag schematicTag = (CompoundTag)rootTag.getTag();
            Map<String, Tag> schematic = schematicTag.getValue();
            if (!schematic.containsKey("Blocks")) {
                throw new IOException("Schematic file is missing a 'Blocks' tag");
            } else {
                String materials = ((StringTag)requireTag(schematic, "Materials", StringTag.class)).getValue();
                if (!materials.equals("Alpha")) {
                    throw new IOException("Schematic file is not an Alpha schematic");
                } else {
                    short width = ((ShortTag)requireTag(schematic, "Width", ShortTag.class)).getValue().shortValue();
                    short height = ((ShortTag)requireTag(schematic, "Height", ShortTag.class)).getValue().shortValue();
                    short length = ((ShortTag)requireTag(schematic, "Length", ShortTag.class)).getValue().shortValue();

                    int index;
                    Vector origin;
                    CuboidRegion region;
                    try {
                        int originX = ((IntTag)requireTag(schematic, "WEOriginX", IntTag.class)).getValue().intValue();
                        int originY = ((IntTag)requireTag(schematic, "WEOriginY", IntTag.class)).getValue().intValue();
                        int originZ = ((IntTag)requireTag(schematic, "WEOriginZ", IntTag.class)).getValue().intValue();
                        Vector min = new Vector(originX, originY, originZ);
                        index = ((IntTag)requireTag(schematic, "WEOffsetX", IntTag.class)).getValue().intValue();
                        int offsetY = ((IntTag)requireTag(schematic, "WEOffsetY", IntTag.class)).getValue().intValue();
                        int offsetZ = ((IntTag)requireTag(schematic, "WEOffsetZ", IntTag.class)).getValue().intValue();
                        Vector offset = new Vector(index, offsetY, offsetZ);
                        origin = min.subtract(offset);
                        region = new CuboidRegion(min, min.add(width, height, length).subtract(Vector.ONE));
                    } catch (IOException var26) {
                        origin = new Vector(0, 0, 0);
                        region = new CuboidRegion(origin, origin.add(width, height, length).subtract(Vector.ONE));
                    }

                    byte[] blockId = ((ByteArrayTag)requireTag(schematic, "Blocks", ByteArrayTag.class)).getValue();
                    byte[] blockData = ((ByteArrayTag)requireTag(schematic, "Data", ByteArrayTag.class)).getValue();
                    byte[] addId = new byte[0];
                    short[] blocks = new short[blockId.length];
                    if (schematic.containsKey("AddBlocks")) {
                        addId = ((ByteArrayTag)requireTag(schematic, "AddBlocks", ByteArrayTag.class)).getValue();
                    }

                    for(index = 0; index < blockId.length; ++index) {
                        if (index >> 1 >= addId.length) {
                            blocks[index] = (short)(blockId[index] & 255);
                        } else if ((index & 1) == 0) {
                            blocks[index] = (short)(((addId[index >> 1] & 15) << 8) + (blockId[index] & 255));
                        } else {
                            blocks[index] = (short)(((addId[index >> 1] & 240) << 4) + (blockId[index] & 255));
                        }
                    }

                    List<Tag> tileEntities = ((ListTag)requireTag(schematic, "TileEntities", ListTag.class)).getValue();
                    Map<BlockVector, Map<String, Tag>> tileEntitiesMap = new HashMap();
                    Iterator var35 = tileEntities.iterator();

                    while(true) {
                        int y;
                        int z;
                        Tag tag;
                        do {
                            if (!var35.hasNext()) {
                                BlockArrayClipboard clipboard = new WEGBlockArrayClipboard(region);
                                clipboard.setOrigin(origin);
                                int failedBlockSets = 0;

                                for(int x = 0; x < width; ++x) {
                                    for(y = 0; y < height; ++y) {
                                        for(z = 0; z < length; ++z) {
                                            index = y * width * length + z * width + x;
                                            BlockVector pt = new BlockVector(x, y, z);
                                            BaseBlock block = new BaseBlock(blocks[index], blockData[index]);
                                            if (tileEntitiesMap.containsKey(pt)) {
                                                block.setNbtData(new CompoundTag((Map)tileEntitiesMap.get(pt)));
                                            }

                                            try {
                                                clipboard.setBlock(region.getMinimumPoint().add(pt), block);
                                            } catch (WorldEditException var28) {
                                                switch(failedBlockSets) {
                                                    case 0:
                                                        log.log(Level.WARNING, "Failed to set block on a Clipboard", var28);
                                                        break;
                                                    case 1:
                                                        log.log(Level.WARNING, "Failed to set block on a Clipboard (again) -- no more messages will be logged", var28);
                                                }

                                                ++failedBlockSets;
                                            }
                                        }
                                    }
                                }

                                try {
                                    List<Tag> entityTags = ((ListTag)requireTag(schematic, "Entities", ListTag.class)).getValue();
                                    Iterator var41 = entityTags.iterator();

                                    while(var41.hasNext()) {
                                        tag = (Tag) var41.next();
                                        if (tag instanceof CompoundTag) {
                                            CompoundTag compound = (CompoundTag)tag;
                                            String id = compound.getString("id");
                                            Location location = NBTConversions.toLocation(clipboard, compound.getListTag("Pos"), compound.getListTag("Rotation"));
                                            if (!id.isEmpty()) {
                                                BaseEntity state = new BaseEntity(id, compound);
                                                clipboard.createEntity(location, state);
                                            }
                                        }
                                    }
                                } catch (IOException var27) {
                                    ;
                                }

                                return clipboard;
                            }

                            tag = (Tag)var35.next();
                        } while(!(tag instanceof CompoundTag));

                        CompoundTag t = (CompoundTag)tag;
                        y = 0;
                        z = 0;
                        index = 0;
                        Map<String, Tag> values = new HashMap();

                        Entry entry;
                        for(Iterator var24 = t.getValue().entrySet().iterator(); var24.hasNext(); values.put(entry.getKey()+"", (Tag) entry.getValue())) {
                            entry = (Entry)var24.next();
                            if (((String)entry.getKey()).equals("x")) {
                                if (entry.getValue() instanceof IntTag) {
                                    y = ((IntTag)entry.getValue()).getValue().intValue();
                                }
                            } else if (((String)entry.getKey()).equals("y")) {
                                if (entry.getValue() instanceof IntTag) {
                                    z = ((IntTag)entry.getValue()).getValue().intValue();
                                }
                            } else if (((String)entry.getKey()).equals("z") && entry.getValue() instanceof IntTag) {
                                index = ((IntTag)entry.getValue()).getValue().intValue();
                            }
                        }

                        BlockVector vec = new BlockVector(y, z, index);
                        tileEntitiesMap.put(vec, values);
                    }
                }
            }
        }
    }

    private static <T extends Tag> T requireTag(Map<String, Tag> items, String key, Class<T> expected) throws IOException {
        if (!items.containsKey(key)) {
            throw new IOException("Schematic file is missing a \"" + key + "\" tag");
        } else {
            Tag tag = (Tag)items.get(key);
            if (!expected.isInstance(tag)) {
                throw new IOException(key + " tag is not of tag type " + expected.getName());
            } else {
                return (T) expected.cast(tag);
            }
        }
    }

    @Nullable
    private static <T extends Tag> T getTag(CompoundTag tag, Class<T> expected, String key) {
        Map<String, Tag> items = tag.getValue();
        if (!items.containsKey(key)) {
            return null;
        } else {
            Tag test = (Tag)items.get(key);
            return !expected.isInstance(test) ? null : (T) expected.cast(test);
        }
    }
}

