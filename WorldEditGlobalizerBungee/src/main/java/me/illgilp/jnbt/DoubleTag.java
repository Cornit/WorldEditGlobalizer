package me.illgilp.jnbt;

/**
 * The {@code TAG_Double} tag.
 */
public final class DoubleTag extends Tag {

    private final double value;

    /**
     * Creates the tag with an empty name.
     *
     * @param value the value of the tag
     */
    public DoubleTag(double value) {
        super();
        this.value = value;
    }

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "TAG_Double(" + value + ")";
    }

}
