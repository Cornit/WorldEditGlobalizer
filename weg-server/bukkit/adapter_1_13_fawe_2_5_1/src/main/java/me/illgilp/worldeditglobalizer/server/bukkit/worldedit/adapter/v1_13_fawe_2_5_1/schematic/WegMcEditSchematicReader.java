package me.illgilp.worldeditglobalizer.server.bukkit.worldedit.adapter.v1_13_fawe_2_5_1.schematic;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.NBTSchematicReader;
import com.sk89q.worldedit.extent.clipboard.io.legacycompat.NBTCompatibilityHandler;
import com.sk89q.worldedit.extent.clipboard.io.legacycompat.SignCompatibilityHandler;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.entity.EntityTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import com.sk89q.worldedit.world.storage.NBTConversions;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads schematic files that are compatible with MCEdit and other editors.
 */
@SuppressWarnings( { "deprecation", "removal" })
public class WegMcEditSchematicReader extends NBTSchematicReader {

    private static final List<NBTCompatibilityHandler> COMPATIBILITY_HANDLERS = new ArrayList<>();
    private static final Logger log = Logger.getLogger(WegMcEditSchematicReader.class.getCanonicalName());

    static {
        COMPATIBILITY_HANDLERS.add(new SignCompatibilityHandler());
    }

    private final com.sk89q.jnbt.NBTInputStream inputStream;

    /**
     * Create a new instance.
     *
     * @param inputStream the input stream to read from
     */
    public WegMcEditSchematicReader(com.sk89q.jnbt.NBTInputStream inputStream) {
        checkNotNull(inputStream);
        this.inputStream = inputStream;
    }

    public static boolean isFormat(InputStream inputStream) {
        try (com.sk89q.jnbt.NBTInputStream str = new com.sk89q.jnbt.NBTInputStream(inputStream)) {
            com.sk89q.jnbt.NamedTag rootTag = str.readNamedTag();
            if (!rootTag.getName().equals("Schematic")) {
                return false;
            }
            CompoundTag schematicTag = (CompoundTag) rootTag.getTag();
            // Check
            Map<String, com.sk89q.jnbt.Tag> schematic = schematicTag.getValue();
            if (!schematic.containsKey("Materials")) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public Clipboard read() throws IOException {
        // Schematic tag
        com.sk89q.jnbt.NamedTag rootTag = inputStream.readNamedTag();
        if (!rootTag.getName().equals("Schematic")) {
            throw new IOException("Tag 'Schematic' does not exist or is not first");
        }
        CompoundTag schematicTag = (CompoundTag) rootTag.getTag();

        // Check
        Map<String, com.sk89q.jnbt.Tag> schematic = schematicTag.getValue();
        if (!schematic.containsKey("Blocks")) {
            throw new IOException("Schematic file is missing a 'Blocks' tag");
        }

        // Check type of Schematic
        String materials = requireTag(schematic, "Materials", StringTag.class).getValue();
        if (!materials.equals("Alpha")) {
            throw new IOException("Schematic file is not an Alpha schematic");
        }

        // ====================================================================
        // Metadata
        // ====================================================================

        BlockVector3 origin;
        Region region;

        // Get information
        short width = requireTag(schematic, "Width", ShortTag.class).getValue();
        short height = requireTag(schematic, "Height", ShortTag.class).getValue();
        short length = requireTag(schematic, "Length", ShortTag.class).getValue();

        try {
            int originX = requireTag(schematic, "WEOriginX", IntTag.class).getValue();
            int originY = requireTag(schematic, "WEOriginY", IntTag.class).getValue();
            int originZ = requireTag(schematic, "WEOriginZ", IntTag.class).getValue();
            BlockVector3 min = BlockVector3.at(originX, originY, originZ);

            int offsetX = requireTag(schematic, "WEOffsetX", IntTag.class).getValue();
            int offsetY = requireTag(schematic, "WEOffsetY", IntTag.class).getValue();
            int offsetZ = requireTag(schematic, "WEOffsetZ", IntTag.class).getValue();
            BlockVector3 offset = BlockVector3.at(offsetX, offsetY, offsetZ);

            origin = min.subtract(offset);
            region = new CuboidRegion(min, min.add(width, height, length).subtract(BlockVector3.ONE));
        } catch (IOException ignored) {
            origin = BlockVector3.at(0, 0, 0);
            region = new CuboidRegion(origin, origin.add(width, height, length).subtract(BlockVector3.ONE));
        }

        // ====================================================================
        // Blocks
        // ====================================================================

        // Get blocks
        byte[] blockId = requireTag(schematic, "Blocks", ByteArrayTag.class).getValue();
        byte[] blockData = requireTag(schematic, "Data", ByteArrayTag.class).getValue();
        byte[] addId = new byte[0];
        short[] blocks = new short[blockId.length]; // Have to later combine IDs

        // We support 4096 block IDs using the same method as vanilla Minecraft, where
        // the highest 4 bits are stored in a separate byte array.
        if (schematic.containsKey("AddBlocks")) {
            addId = requireTag(schematic, "AddBlocks", ByteArrayTag.class).getValue();
        }

        // Combine the AddBlocks data with the first 8-bit block ID
        for (int index = 0; index < blockId.length; index++) {
            if ((index >> 1) >= addId.length) { // No corresponding AddBlocks index
                blocks[index] = (short) (blockId[index] & 0xFF);
            } else {
                if ((index & 1) == 0) {
                    blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (blockId[index] & 0xFF));
                } else {
                    blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (blockId[index] & 0xFF));
                }
            }
        }

        // Need to pull out tile entities
        List<com.sk89q.jnbt.Tag> tileEntities = requireTag(schematic, "TileEntities", ListTag.class).getValue();
        Map<BlockVector3, Map<String, com.sk89q.jnbt.Tag>> tileEntitiesMap = new HashMap<>();

        for (com.sk89q.jnbt.Tag tag : tileEntities) {
            if (!(tag instanceof CompoundTag)) {
                continue;
            }
            CompoundTag t = (CompoundTag) tag;

            int x = 0;
            int y = 0;
            int z = 0;

            Map<String, com.sk89q.jnbt.Tag> values = new HashMap<>();

            for (Map.Entry<String, com.sk89q.jnbt.Tag> entry : t.getValue().entrySet()) {
                switch (entry.getKey()) {
                    case "x":
                        if (entry.getValue() instanceof IntTag) {
                            x = ((IntTag) entry.getValue()).getValue();
                        }
                        break;
                    case "y":
                        if (entry.getValue() instanceof IntTag) {
                            y = ((IntTag) entry.getValue()).getValue();
                        }
                        break;
                    case "z":
                        if (entry.getValue() instanceof IntTag) {
                            z = ((IntTag) entry.getValue()).getValue();
                        }
                        break;
                }

                values.put(entry.getKey(), entry.getValue());
            }

            int index = y * width * length + z * width + x;
            BlockState block = LegacyMapper.getInstance().getBlockFromLegacy(blocks[index], blockData[index]);
            if (block != null) {
                for (NBTCompatibilityHandler handler : COMPATIBILITY_HANDLERS) {
                    if (handler.isAffectedBlock(block)) {
                        handler.updateNBT(block, values);
                    }
                }
            }

            BlockVector3 vec = BlockVector3.at(x, y, z);
            tileEntitiesMap.put(vec, values);
        }

        BlockArrayClipboard clipboard = new WegBlockArrayClipboard(region);
        clipboard.setOrigin(origin);

        // Don't log a torrent of errors
        int failedBlockSets = 0;

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int index = y * width * length + z * width + x;
                    BlockVector3 pt = BlockVector3.at(x, y, z);
                    BlockState state = LegacyMapper.getInstance().getBlockFromLegacy(blocks[index], blockData[index]);

                    try {
                        if (state != null) {
                            if (tileEntitiesMap.containsKey(pt)) {
                                clipboard.setBlock(region.getMinimumPoint().add(pt), state.toBaseBlock(new CompoundTag(tileEntitiesMap.get(pt))));
                            } else {
                                clipboard.setBlock(region.getMinimumPoint().add(pt), state);
                            }
                        } else {
                            log.warning("Unknown block when pasting schematic: " + blocks[index] + ":" + blockData[index] + ". Please report this issue.");
                        }
                    } catch (WorldEditException e) {
                        switch (failedBlockSets) {
                            case 0:
                                log.log(Level.WARNING, "Failed to set block on a Clipboard", e);
                                break;
                            case 1:
                                log.log(Level.WARNING, "Failed to set block on a Clipboard (again) -- no more messages will be logged", e);
                                break;
                            default:
                        }

                        failedBlockSets++;
                    }
                }
            }
        }

        // ====================================================================
        // Entities
        // ====================================================================

        try {
            List<com.sk89q.jnbt.Tag> entityTags = requireTag(schematic, "Entities", ListTag.class).getValue();

            for (com.sk89q.jnbt.Tag tag : entityTags) {
                if (tag instanceof CompoundTag) {
                    CompoundTag compound = (CompoundTag) tag;
                    String id = convertEntityId(compound.getString("id"));
                    Location location = NBTConversions.toLocation(clipboard, compound.getListTag("Pos"), compound.getListTag("Rotation"));

                    if (!id.isEmpty()) {
                        EntityType entityType = EntityTypes.get(id.toLowerCase());
                        if (entityType != null) {
                            BaseEntity state = new BaseEntity(entityType, compound);
                            clipboard.createEntity(location, state);
                        } else {
                            log.warning("Unknown entity when pasting schematic: " + id.toLowerCase());
                        }
                    }
                }
            }
        } catch (IOException ignored) { // No entities? No problem
        }

        return clipboard;
    }

    private String convertEntityId(String id) {
        switch (id) {
            case "xp_orb":
                return "experience_orb";
            case "xp_bottle":
                return "experience_bottle";
            case "eye_of_ender_signal":
                return "eye_of_ender";
            case "ender_crystal":
                return "end_crystal";
            case "fireworks_rocket":
                return "firework_rocket";
            case "commandblock_minecart":
                return "command_block_minecart";
            case "snowman":
                return "snow_golem";
            case "villager_golem":
                return "iron_golem";
            case "evocation_fangs":
                return "evoker_fangs";
            case "evocation_illager":
                return "evoker";
            case "vindication_illager":
                return "vindicator";
            case "illusion_illager":
                return "illusioner";
        }
        return id;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
