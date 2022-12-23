package me.illgilp.worldeditglobalizer.common;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataInput;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataOutput;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class WegVersion {

    public static final int BYTES_SIZE = 4 * 4;

    private final int major;
    private final int minor;
    private final int patch;
    private final Type type;

    public WegVersion(PacketDataInput in) throws IOException {
        this.major = in.readInt();
        this.minor = in.readInt();
        this.patch = in.readInt();
        this.type = Type.fromId(in.readInt());
    }

    public WegVersion(String asString) {
        Pattern pattern = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)(-(([Bb]eta)|([Aa]lpha)))?$");
        Matcher matcher = pattern.matcher(asString);
        if (matcher.find()) {
            if (matcher.groupCount() == 4 || matcher.groupCount() == 7) {
                this.major = Integer.parseInt(matcher.group(1));
                this.minor = Integer.parseInt(matcher.group(2));
                this.patch = Integer.parseInt(matcher.group(3));
                this.type = Arrays.stream(Type.values())
                    .filter(t -> matcher.groupCount() == 7)
                    .filter(t -> matcher.group(5).equalsIgnoreCase(t.display))
                    .findFirst()
                    .orElse(Type.RELEASE);
                return;
            }
        }
        throw new IllegalArgumentException("version '" + asString + "' does not match required pattern");
    }

    public void write(PacketDataOutput out) throws IOException {
        out.writeInt(this.major);
        out.writeInt(this.minor);
        out.writeInt(this.patch);
        out.writeInt(this.type.id);
    }

    public enum Type {

        ALPHA(1, "alpha"),
        BETA(2, "beta"),
        RELEASE(3, null),

        ;

        private final int id;
        private final String display;

        Type(int id, String display) {
            this.id = id;
            this.display = display;
        }

        private static Type fromId(int id) {
            return Arrays.stream(values()).filter(type -> type.id == id).findFirst().orElse(RELEASE);
        }

        private Optional<String> getDisplay() {
            return Optional.ofNullable(display);
        }
    }

}
