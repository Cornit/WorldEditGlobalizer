package me.illgilp.worldeditglobalizerbukkit.clipboard;

import com.google.common.collect.Maps;
import com.sk89q.jnbt.*;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.NBTSchematicReader;
import com.sk89q.worldedit.extent.clipboard.io.legacycompat.NBTCompatibilityHandler;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.entity.EntityTypes;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Reads schematic files using the Sponge Schematic Specification.
 */
public class WEGSpongeSchematicReader extends NBTSchematicReader {

    private static final List<NBTCompatibilityHandler> COMPATIBILITY_HANDLERS = new ArrayList<>();
    private static final Logger log = Logger.getLogger(WEGSpongeSchematicReader.class.getCanonicalName());

    static {
        // If NBT Compat handlers are needed - add them here.
    }

    private final NBTInputStream inputStream;

    /**
     * Create a new instance.
     *
     * @param inputStream the input stream to read from
     */
    public WEGSpongeSchematicReader(NBTInputStream inputStream) {
        checkNotNull(inputStream);
        this.inputStream = inputStream;
    }

    public static boolean isFormat(InputStream inputStream) {
        try (NBTInputStream str = new NBTInputStream(inputStream)) {
            NamedTag rootTag = str.readNamedTag();
            if (!rootTag.getName().equals("Schematic")) {
                return false;
            }
            CompoundTag schematicTag = (CompoundTag) rootTag.getTag();

            // Check
            Map<String, Tag> schematic = schematicTag.getValue();
            if (!schematic.containsKey("Version")) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    @Override
    public Clipboard read() throws IOException {
        NamedTag rootTag = inputStream.readNamedTag();
        if (!rootTag.getName().equals("Schematic")) {
            throw new IOException("Tag 'Schematic' does not exist or is not first");
        }
        CompoundTag schematicTag = (CompoundTag) rootTag.getTag();

        // Check
        Map<String, Tag> schematic = schematicTag.getValue();
        int version = requireTag(schematic, "Version", IntTag.class).getValue();
        switch (version) {
            case 1:
                return readVersion1(schematic);
            default:
                throw new IOException("This schematic version is currently not supported");
        }
    }

    private Clipboard readVersion1(Map<String, Tag> schematic) throws IOException {
        BlockVector3 origin;
        Region region;

        Map<String, Tag> metadata = requireTag(schematic, "Metadata", CompoundTag.class).getValue();

        int width = requireTag(schematic, "Width", ShortTag.class).getValue();
        int height = requireTag(schematic, "Height", ShortTag.class).getValue();
        int length = requireTag(schematic, "Length", ShortTag.class).getValue();

        int[] offsetParts = requireTag(schematic, "Offset", IntArrayTag.class).getValue();
        if (offsetParts.length != 3) {
            throw new IOException("Invalid offset specified in schematic.");
        }

        BlockVector3 min = BlockVector3.at(offsetParts[0], offsetParts[1], offsetParts[2]);

        if (metadata.containsKey("WEOffsetX")) {
            // We appear to have WorldEdit Metadata
            int offsetX = requireTag(metadata, "WEOffsetX", IntTag.class).getValue();
            int offsetY = requireTag(metadata, "WEOffsetY", IntTag.class).getValue();
            int offsetZ = requireTag(metadata, "WEOffsetZ", IntTag.class).getValue();
            BlockVector3 offset = BlockVector3.at(offsetX, offsetY, offsetZ);
            origin = min.subtract(offset);
            region = new CuboidRegion(min, min.add(width, height, length).subtract(BlockVector3.ONE));
        } else {
            origin = min;
            region = new CuboidRegion(origin, origin.add(width, height, length).subtract(BlockVector3.ONE));
        }

        int paletteMax = requireTag(schematic, "PaletteMax", IntTag.class).getValue();
        Map<String, Tag> paletteObject = requireTag(schematic, "Palette", CompoundTag.class).getValue();
        if (paletteObject.size() != paletteMax) {
            throw new IOException("Differing given palette size to actual size");
        }

        Map<Integer, BlockState> palette = new HashMap<>();

        ParserContext parserContext = new ParserContext();
        parserContext.setRestricted(false);
        parserContext.setTryLegacy(false);
        parserContext.setPreferringWildcard(false);

        for (String palettePart : paletteObject.keySet()) {
            int id = requireTag(paletteObject, palettePart, IntTag.class).getValue();
            BlockState state;
            try {
                state = WorldEdit.getInstance().getBlockFactory().parseFromInput(palettePart, parserContext).toImmutableState();
            } catch (InputParseException e) {
                throw new IOException("Invalid BlockState in schematic: " + palettePart + ". Are you missing a mod of using a schematic made in a newer version of Minecraft?");
            }
            palette.put(id, state);
        }

        byte[] blocks = requireTag(schematic, "BlockData", ByteArrayTag.class).getValue();

        Map<BlockVector3, Map<String, Tag>> tileEntitiesMap = new HashMap<>();
        try {
            List<Map<String, Tag>> tileEntityTags = requireTag(schematic, "TileEntities", ListTag.class).getValue().stream()
                    .map(tag -> (CompoundTag) tag)
                    .map(CompoundTag::getValue)
                    .collect(Collectors.toList());

            for (Map<String, Tag> tileEntity : tileEntityTags) {
                int[] pos = requireTag(tileEntity, "Pos", IntArrayTag.class).getValue();
                tileEntitiesMap.put(BlockVector3.at(pos[0], pos[1], pos[2]), tileEntity);
            }
        } catch (Exception e) {
            throw new IOException("Failed to load Tile Entities: " + e.getMessage());
        }

        WEGBlockArrayClipboard clipboard = new WEGBlockArrayClipboard(region);
        clipboard.setOrigin(origin);

        int index = 0;
        int i = 0;
        int value = 0;
        int varintLength = 0;
        while (i < blocks.length) {
            value = 0;
            varintLength = 0;

            while (true) {
                value |= (blocks[i] & 127) << (varintLength++ * 7);
                if (varintLength > 5) {
                    throw new RuntimeException("VarInt too big (probably corrupted data)");
                }
                if ((blocks[i] & 128) != 128) {
                    i++;
                    break;
                }
                i++;
            }
            // index = (y * length + z) * width + x
            int y = index / (width * length);
            int z = (index % (width * length)) / width;
            int x = (index % (width * length)) % width;
            BlockState state = palette.get(value);
            BlockVector3 pt = BlockVector3.at(x, y, z);
            try {
                if (tileEntitiesMap.containsKey(pt)) {
                    Map<String, Tag> values = Maps.newHashMap(tileEntitiesMap.get(pt));
                    for (NBTCompatibilityHandler handler : COMPATIBILITY_HANDLERS) {
                        if (handler.isAffectedBlock(state)) {
                            handler.updateNBT(state, values);
                        }
                    }
                    values.put("x", new IntTag(pt.getBlockX()));
                    values.put("y", new IntTag(pt.getBlockY()));
                    values.put("z", new IntTag(pt.getBlockZ()));
                    values.put("id", values.get("Id"));
                    values.remove("Id");
                    values.remove("Pos");
                    clipboard.setBlock(clipboard.getMinimumPoint().add(pt), state.toBaseBlock(new CompoundTag(values)));
                } else {
                    clipboard.setBlock(clipboard.getMinimumPoint().add(pt), state);
                }
            } catch (WorldEditException e) {
                throw new IOException("Failed to load a block in the schematic");
            }

            index++;
        }
        if (schematic.containsKey("WorldEditGlobalizer")) {
            CompoundTag weg = (CompoundTag) schematic.get("WorldEditGlobalizer");
            if (weg != null) {
                if (weg.containsKey("Entities")) {
                    List<CompoundTag> entities = weg.getList("Entities", CompoundTag.class);
                    for (CompoundTag ent : entities) {
                        if (ent.containsKey("WEGTypeId")) {
                            String id = ent.getString("WEGTypeId");
                            if (EntityTypes.get(id.toLowerCase()) != null) {
                                EntityType entityType = EntityTypes.get(id.toLowerCase());
                                Map<String, Tag> entM = new HashMap<>(ent.getValue());
                                entM.remove("WEGTypeId");
                                if (ent.containsKey("Pos")) {
                                    List<DoubleTag> pos = ent.getList("Pos", DoubleTag.class);
                                    double entX = pos.get(0).getValue();
                                    double entY = pos.get(1).getValue();
                                    double entZ = pos.get(2).getValue();
                                    if (ent.containsKey("WorldUUIDLeast") && ent.containsKey("WorldUUIDMost")) {
                                        UUID uuid = new UUID(ent.getLong("WorldUUIDMost"), ent.getLong("WorldUUIDLeast"));
                                        World world = Bukkit.getWorld(uuid) != null ? Bukkit.getWorld(uuid) : Bukkit.getWorlds().get(0);
                                        clipboard.createEntity(new Location(new BukkitWorld(world), entX, entY, entZ), new BaseEntity(entityType, new CompoundTag(entM)));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return clipboard;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
