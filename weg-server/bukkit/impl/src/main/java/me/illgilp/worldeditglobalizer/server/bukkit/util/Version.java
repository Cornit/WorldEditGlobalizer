package me.illgilp.worldeditglobalizer.server.bukkit.util;

import java.util.Optional;

public class Version implements Comparable<Version> {

    public static Version fromString(String s) {
        final String[] parts = s.split("\\.");
        return new Version(
            Integer.parseInt(parts[0]),
            Optional.ofNullable(parts.length >= 2 ? parts[1] : null)
                .map(Integer::parseInt)
                .orElse(0)
        );
    }

    private final int major;
    private final int minor;

    public Version(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Version version = (Version) o;

        if (major != version.major) {
            return false;
        }
        return minor == version.minor;
    }

    @Override
    public int hashCode() {
        int result = major;
        result = 31 * result + minor;
        return result;
    }

    @Override
    public String toString() {
        return this.major + "." + this.minor;
    }

    @Override
    public int compareTo(Version o) {
        if (this.major < o.major) {
            return -1;
        }
        if (this.major > o.major) {
            return 1;
        }
        return Integer.compare(this.minor, o.minor);
    }
}
