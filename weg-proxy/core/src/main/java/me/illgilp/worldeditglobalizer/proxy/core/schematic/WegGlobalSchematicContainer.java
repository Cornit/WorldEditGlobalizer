package me.illgilp.worldeditglobalizer.proxy.core.schematic;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.illgilp.worldeditglobalizer.proxy.core.api.clipboard.WegClipboard;
import me.illgilp.worldeditglobalizer.proxy.core.api.schematic.WegSchematic;
import me.illgilp.worldeditglobalizer.proxy.core.api.schematic.WegSchematicContainer;


@RequiredArgsConstructor
public class WegGlobalSchematicContainer implements WegSchematicContainer {

    private final Supplier<File> folderSupplier;

    @Override
    public List<WegSchematic> getSchematics() throws IOException {
        return Files.walk(folderSupplier.get().toPath())
            .filter(Files::isRegularFile)
            .map(Path::toFile)
            .map(file -> new WegSchematicImpl(removeExtension(folderSupplier.get()
                .toPath()
                .relativize(file.toPath())
                .toFile()
                .getPath()
                .replace("\\", "/")
            ), file))
            .collect(Collectors.toList());
    }

    @Override
    public Optional<WegSchematic> getSchematic(String name) throws IOException {
        return getSchematics().stream()
            .filter(wegSchematic -> wegSchematic.getName().equalsIgnoreCase(name))
            .findFirst();
    }

    @Override
    public WegSchematic createSchematic(String name, WegClipboard clipboard) throws IOException {
        File target = new File(this.folderSupplier.get(), name + ".wegschem");
        if (target.exists()) {
            throw new FileAlreadyExistsException(target.getAbsolutePath());
        }
        if (target.getParentFile() != null) {
            if (!target.getParentFile().exists()) {
                if (!target.getParentFile().mkdirs()) {
                    throw new IOException("could not create directory " + target.getParentFile().getAbsolutePath());
                }
            }
        }

        Files.write(target.toPath(), clipboard.getData());

        return new WegSchematicImpl(name, target);
    }

    @Override
    public boolean deleteSchematic(String name) throws IOException {
        return getSchematics().stream()
            .filter(wegSchematic -> wegSchematic.getName().equalsIgnoreCase(name))
            .findFirst()
            .map(WegSchematic::getFile)
            .map(File::delete)
            .orElse(false);
    }


    private String removeExtension(String name) {
        int index = name.lastIndexOf('.');
        if (index >= 0) {
            return name.substring(0, index);
        }
        return name;
    }

    @RequiredArgsConstructor
    private static class WegSchematicImpl implements WegSchematic {

        private final String name;
        private final File file;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public File getFile() {
            return file;
        }

        @Override
        public WegClipboard asClipboard() throws IOException {
            return new WegSchematicClipboardImpl(
                Instant.ofEpochMilli(this.file.lastModified())
                    .atZone(ZoneId.systemDefault())
                    .toInstant(),
                Files.readAllBytes(file.toPath()));
        }
    }

    @RequiredArgsConstructor
    private static class WegSchematicClipboardImpl implements WegClipboard {

        private final Instant lastModified;
        private final byte[] data;

        @Override
        public Instant getUploadDate() {
            return this.lastModified;
        }

        @Override
        public long getSize() {
            return data.length;
        }

        @Override
        public int getHash() {
            return Arrays.hashCode(data);
        }

        @Override
        public byte[] getData() {
            return data;
        }
    }

}
