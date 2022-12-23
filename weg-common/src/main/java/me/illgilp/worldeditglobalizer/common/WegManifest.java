package me.illgilp.worldeditglobalizer.common;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class WegManifest {

    private static final String WEG_VERSION_KEY = "WorldEditGlobalizer-Version";
    private static final String BUILD_TAG_KEY = "Build-Tag";
    private final WegVersion version;
    private final String buildTag;

    public static WegManifest load() {
        final Class<WegManifest> clazz = WegManifest.class;
        final String className = clazz.getSimpleName() + ".class";
        final String classPath = clazz.getResource(className).toString();
        if (!classPath.startsWith("jar")) {
            return null;
        }

        try {
            final URL url = new URL(classPath);
            final JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
            final Manifest manifest = jarConnection.getManifest();
            return new WegManifest(
                new WegVersion(readAttribute(manifest.getMainAttributes(), WEG_VERSION_KEY, () -> "(unknown)")),
                readAttribute(manifest.getMainAttributes(), BUILD_TAG_KEY, () -> "(unknown)")
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String readAttribute(Attributes attributes, String name, Supplier<String> defaultValue) {
        return Optional.ofNullable(attributes.getValue(name)).orElseGet(defaultValue);
    }
}
