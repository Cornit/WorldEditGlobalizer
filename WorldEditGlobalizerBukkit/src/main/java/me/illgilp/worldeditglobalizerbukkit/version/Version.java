package me.illgilp.worldeditglobalizerbukkit.version;

public class Version implements Comparable<Version>{

    private int major;
    private int minor;

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
        if (this.major < o.major) return -1;
        if (this.major > o.major) return 1;
        return Integer.compare(this.minor, o.minor);
    }
}
