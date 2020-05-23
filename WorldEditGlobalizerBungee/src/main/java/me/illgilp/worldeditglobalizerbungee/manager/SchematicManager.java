package me.illgilp.worldeditglobalizerbungee.manager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;
import me.illgilp.jnbt.ByteArrayTag;
import me.illgilp.jnbt.CompoundTag;
import me.illgilp.jnbt.NBTInputStream;
import me.illgilp.jnbt.NamedTag;
import me.illgilp.jnbt.StringTag;
import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.clipboard.Clipboard;
import org.apache.commons.io.IOUtils;

public class SchematicManager {

    private static SchematicManager instance;
    private File schematicsFolder;

    public SchematicManager(File schematicsFolder) {
        instance = this;

        this.schematicsFolder = schematicsFolder;
    }

    public static SchematicManager getInstance() {
        if (instance == null) {
            instance = new SchematicManager(new File(WorldEditGlobalizerBungee.getInstance().getDataFolder(), "schematics"));
        }
        return instance;
    }

    public File getSchematicsFolder() {
        return schematicsFolder;
    }

    private void checkFolder() {
        if (!schematicsFolder.exists()) schematicsFolder.mkdirs();
    }

    public void saveSchematic(String name, Clipboard clipboard) {
        checkFolder();
        try {
            File schematicFile = new File(schematicsFolder, name + ".schematic");
            if (!schematicFile.exists()) schematicFile.createNewFile();
            InputStream in = new ByteArrayInputStream(clipboard.getData());

            OutputStream outStream = new GZIPOutputStream(new FileOutputStream(schematicFile));

            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            in.close();
            outStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isValidSchematic(InputStream inputStream) {
        try {
            NamedTag rootNamedTag = new NBTInputStream(new GZIPInputStream(inputStream)).readNamedTag();
            if (rootNamedTag.getName().endsWith("Schematic") && rootNamedTag.getTag() instanceof CompoundTag) {
                CompoundTag rootTag = (CompoundTag) rootNamedTag.getTag();
                if (rootTag.containsKey("Blocks") && rootTag.getTag("Blocks") instanceof ByteArrayTag &&
                        rootTag.containsKey("Materials") && rootTag.getTag("Materials") instanceof StringTag) {
                    if (rootTag.getString("Materials").equals("Alpha")) {
                        inputStream.close();
                        return true;
                    }
                } else if (rootTag.containsKey("Version")) {
                    inputStream.close();
                    return true;
                }
            }
        } catch (Exception e) {
            if (e instanceof ZipException) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                }
                return false;
            }
            e.printStackTrace();
        }
        try {
            inputStream.close();
        } catch (IOException e) {
        }
        return false;
    }

    public List<String> getSchematics() {
        checkFolder();
        List<String> list = new ArrayList<>();
        for (File file : schematicsFolder.listFiles()) {
            if (file.getName().endsWith(".schematic")) {
                list.add(file.getName().replace(".schematic", ""));
            }
        }
        return list;
    }

    public byte[] loadSchematic(String name) {
        checkFolder();
        File schematicFile = new File(schematicsFolder, name + ".schematic");
        if (!schematicFile.exists()) return new byte[0];
        try (FileInputStream fisForValidation = new FileInputStream(schematicFile)) {
            if (isValidSchematic(fisForValidation)) {
                try (GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(schematicFile))) {
                    byte[] data = IOUtils.toByteArray(gzipInputStream);
                    gzipInputStream.close();
                    return data;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public File getSchematicFile(String name) {
        return new File(schematicsFolder, name + ".schematic");
    }

    public Clipboard loadSchematicInto(String name, UUID player) {
        checkFolder();
        if (player == null) return null;
        byte[] data = loadSchematic(name);
        if (data.length == 0) {
            return null;
        }
        Clipboard clipboard = new Clipboard(player, data, Arrays.hashCode(data), "SCHEMATIC");
        return clipboard;
    }

}
