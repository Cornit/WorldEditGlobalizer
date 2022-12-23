package me.illgilp.worldeditglobalizer.proxy.core.api.clipboard;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import me.illgilp.worldeditglobalizer.proxy.core.api.WegProxy;

public class WegClipboardContainer {

    private final File clipboardFile;

    public WegClipboardContainer(UUID playerUniqueId) {
        this.clipboardFile = new File(
            WegProxy.getInstance().getDataFolder(),
            "clipboards/" + playerUniqueId + ".clipboard"
        );
    }

    public boolean hasClipboard() {
        return clipboardFile.exists();
    }

    public boolean clear() {
        if (!hasClipboard()) {
            return false;
        }
        return this.clipboardFile.delete();
    }

    public Optional<WegClipboard> getClipboard() {
        if (!hasClipboard()) {
            return Optional.empty();
        }
        return Optional.of(new WegClipboardImpl());
    }

    public void setClipboard(int hash, byte[] data) throws IOException {
        if (clipboardFile.getParentFile() != null) {
            if (!clipboardFile.getParentFile().exists()) {
                if (!clipboardFile.getParentFile().mkdirs()) {
                    WegProxy.getInstance().getLogger().severe("Could not create directory: " + clipboardFile.getParentFile().getAbsolutePath());
                    return;
                }
            }
        }
        try (OutputStream out = Files.newOutputStream(this.clipboardFile.toPath())) {
            final ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt(hash);
            buffer.flip();
            out.write(buffer.array());
            out.write(data);
            out.flush();
        }
    }

    public void setClipboard(WegClipboard clipboard) throws IOException {
        this.setClipboard(clipboard.getHash(), clipboard.getData());
    }

    @Getter
    private final class WegClipboardImpl implements WegClipboard {

        private final Instant uploadDate;
        private final long size;
        private final File file;

        public WegClipboardImpl() {
            this.file = WegClipboardContainer.this.clipboardFile;
            this.uploadDate = Instant.ofEpochMilli(this.file.lastModified());
            this.size = this.file.length();
        }

        public int getHash() throws IOException {
            try (DataInputStream in = new DataInputStream(Files.newInputStream(this.file.toPath()))) {
                final byte[] data = new byte[4];
                in.readFully(data);
                final ByteBuffer buffer = ByteBuffer.wrap(data);
                return buffer.getInt();
            }
        }

        @Override
        public byte[] getData() throws IOException {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); InputStream in = Files.newInputStream(this.file.toPath())) {
                final byte[] buffer = new byte[8192];
                int read = 0;
                while ((read = in.read(buffer, 0, buffer.length)) != -1) {
                    baos.write(buffer, 0, read);
                }
                final byte[] data = baos.toByteArray();
                final byte[] copy = new byte[data.length - 4];
                System.arraycopy(data, 4, copy, 0, copy.length);
                return copy;
            }
        }
    }
}
