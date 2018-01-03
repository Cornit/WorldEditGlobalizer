

package me.illgilp.jnbt;

/**
 * The {@code TAG_Float} tag.
 */
public final class FloatTag extends Tag {

    private final float value;

    /**
     * Creates the tag with an empty name.
     *
     * @param value the value of the tag
     */
    public FloatTag(float value) {
        super();
        this.value = value;
    }

    @Override
    public Float getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "TAG_Float(" + value + ")";
    }

}
