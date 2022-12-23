package me.illgilp.worldeditglobalizer.server.bukkit.worldedit.adapter.v1_8_fawe_22_3_9.schematic;


import com.google.common.base.Preconditions;
import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NamedTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class WegSchematicReader implements ClipboardReader {
    private static final Logger log = Logger.getLogger(com.sk89q.worldedit.extent.clipboard.io.SchematicReader.class.getCanonicalName());
    private final NBTInputStream inputStream;

    public static boolean isFormat(InputStream inputStream) {
        try (NBTInputStream str = new NBTInputStream(inputStream)) {
            NamedTag rootTag = str.readNamedTag();
            if (!rootTag.getName().equals("Schematic")) {
                return false;
            }
            CompoundTag schematicTag = (CompoundTag) rootTag.getTag();
            // Check
            Map<String, Tag> schematic = schematicTag.getValue();
            if (!schematic.containsKey("Materials")) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public WegSchematicReader(NBTInputStream inputStream) {
        Preconditions.checkNotNull(inputStream);
        this.inputStream = inputStream;
    }

    public Clipboard read(WorldData data) throws IOException {
        NamedTag rootTag = this.inputStream.readNamedTag();
        if (!rootTag.getName().equals("Schematic")) {
            throw new IOException("Tag 'Schematic' does not exist or is not first");
        } else {
            CompoundTag schematicTag = (CompoundTag) rootTag.getTag();
            Map<String, Tag> schematic = schematicTag.getValue();
            if (!schematic.containsKey("Blocks")) {
                throw new IOException("Schematic file is missing a 'Blocks' tag");
            } else {
                String materials = requireTag(schematic, "Materials", StringTag.class).getValue();
                if (!materials.equals("Alpha")) {
                    throw new IOException("Schematic file is not an Alpha schematic");
                } else {
                    short width = requireTag(schematic, "Width", ShortTag.class).getValue();
                    short height = requireTag(schematic, "Height", ShortTag.class).getValue();
                    short length = requireTag(schematic, "Length", ShortTag.class).getValue();

                    int index;
                    Vector origin;
                    CuboidRegion region;
                    try {
                        int originX = requireTag(schematic, "WEOriginX", IntTag.class).getValue();
                        int originY = requireTag(schematic, "WEOriginY", IntTag.class).getValue();
                        int originZ = requireTag(schematic, "WEOriginZ", IntTag.class).getValue();
                        Vector min = new Vector(originX, originY, originZ);
                        index = requireTag(schematic, "WEOffsetX", IntTag.class).getValue();
                        int offsetY = requireTag(schematic, "WEOffsetY", IntTag.class).getValue();
                        int offsetZ = requireTag(schematic, "WEOffsetZ", IntTag.class).getValue();
                        Vector offset = new Vector(index, offsetY, offsetZ);
                        origin = min.subtract(offset);
                        region = new CuboidRegion(min, min.add(width, height, length).subtract(Vector.ONE));
                    } catch (IOException var26) {
                        origin = new Vector(0, 0, 0);
                        region = new CuboidRegion(origin, origin.add(width, height, length).subtract(Vector.ONE));
                    }

                    byte[] blockId = requireTag(schematic, "Blocks", ByteArrayTag.class).getValue();
                    byte[] blockData = requireTag(schematic, "Data", ByteArrayTag.class).getValue();
                    byte[] addId = new byte[0];
                    short[] blocks = new short[blockId.length];
                    if (schematic.containsKey("AddBlocks")) {
                        addId = requireTag(schematic, "AddBlocks", ByteArrayTag.class).getValue();
                    }

                    for (index = 0; index < blockId.length; ++index) {
                        if (index >> 1 >= addId.length) {
                            blocks[index] = (short) (blockId[index] & 255);
                        } else if ((index & 1) == 0) {
                            blocks[index] = (short) (((addId[index >> 1] & 15) << 8) + (blockId[index] & 255));
                        } else {
                            blocks[index] = (short) (((addId[index >> 1] & 240) << 4) + (blockId[index] & 255));
                        }
                    }

                    List<Tag> tileEntities = requireTag(schematic, "TileEntities", ListTag.class).getValue();
                    Map<BlockVector, Map<String, Tag>> tileEntitiesMap = new HashMap<>();
                    Iterator<Tag> var35 = tileEntities.iterator();

                    while (true) {
                        int y;
                        int z;
                        Tag tag;
                        do {
                            if (!var35.hasNext()) {
                                BlockArrayClipboard clipboard = new WegBlockArrayClipboard(region);
                                clipboard.setOrigin(origin);
                                int failedBlockSets = 0;

                                for (int x = 0; x < width; ++x) {
                                    for (y = 0; y < height; ++y) {
                                        for (z = 0; z < length; ++z) {
                                            index = y * width * length + z * width + x;
                                            BlockVector pt = new BlockVector(x, y, z);
                                            BaseBlock block = new BaseBlock(blocks[index], blockData[index]);
                                            if (tileEntitiesMap.containsKey(pt)) {
                                                block.setNbtData(new CompoundTag(tileEntitiesMap.get(pt)));
                                            }

                                            try {
                                                clipboard.setBlock(region.getMinimumPoint().add(pt), block);
                                            } catch (WorldEditException var28) {
                                                switch (failedBlockSets) {
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
                                    List<Tag> entityTags = requireTag(schematic, "Entities", ListTag.class).getValue();

                                    for (Tag entityTag : entityTags) {
                                        tag = entityTag;
                                        if (tag instanceof CompoundTag) {
                                            CompoundTag compound = (CompoundTag) tag;
                                            String id = compound.getString("id");
                                            Location location = NBTConversions.toLocation(clipboard, compound.getListTag("Pos"), compound.getListTag("Rotation"));
                                            if (!id.isEmpty()) {
                                                BaseEntity state = new BaseEntity(id, compound);
                                                clipboard.createEntity(location, state);
                                            }
                                        }
                                    }
                                } catch (IOException ignored) {
                                }

                                return clipboard;
                            }

                            tag = var35.next();
                        } while (!(tag instanceof CompoundTag));

                        CompoundTag t = (CompoundTag) tag;
                        y = 0;
                        z = 0;
                        index = 0;
                        Map<String, Tag> values = new HashMap<>();

                        Entry<String, Tag> entry;
                        for (Iterator<Entry<String, Tag>> var24 = t.getValue().entrySet().iterator(); var24.hasNext(); values.put(entry.getKey() + "", entry.getValue())) {
                            entry = var24.next();
                            if (entry.getKey().equals("x")) {
                                if (entry.getValue() instanceof IntTag) {
                                    y = ((IntTag) entry.getValue()).getValue();
                                }
                            } else if (entry.getKey().equals("y")) {
                                if (entry.getValue() instanceof IntTag) {
                                    z = ((IntTag) entry.getValue()).getValue();
                                }
                            } else if (entry.getKey().equals("z") && entry.getValue() instanceof IntTag) {
                                index = ((IntTag) entry.getValue()).getValue();
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
            Tag tag = items.get(key);
            if (!expected.isInstance(tag)) {
                throw new IOException(key + " tag is not of tag type " + expected.getName());
            } else {
                return expected.cast(tag);
            }
        }
    }

    @Nullable
    private static <T extends Tag> T getTag(CompoundTag tag, Class<T> expected, String key) {
        Map<String, Tag> items = tag.getValue();
        if (!items.containsKey(key)) {
            return null;
        } else {
            Tag test = items.get(key);
            return !expected.isInstance(test) ? null : expected.cast(test);
        }
    }
}

