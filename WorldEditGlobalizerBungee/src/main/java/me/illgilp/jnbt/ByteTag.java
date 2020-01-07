package me.illgilp.jnbt;

/**
 * The {@code TAG_Byte} tag.
 */
public final class ByteTag extends Tag {

    private final byte value;

    /**
     * Creates the tag with an empty name.
     *
     * @param value the value of the tag
     */
    public ByteTag(byte value) {
        super();
        this.value = value;
    }

    @Override
    public Byte getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "TAG_Byte(" + value + ")";
    }

}
