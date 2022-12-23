package me.illgilp.worldeditglobalizer.common.messages.tag;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.examination.ExaminableProperty;
import org.jetbrains.annotations.NotNull;

public class ProgressBarTag extends AbstractColorChangingTag {

    /**
     * From Kyori's Adventure API
     *
     * @see net.kyori.adventure.text.minimessage.tag.standard.ColorTagResolver
     */
    private static final Map<String, TextColor> COLOR_ALIASES = new HashMap<>();

    static {
        COLOR_ALIASES.put("dark_grey", NamedTextColor.DARK_GRAY);
        COLOR_ALIASES.put("grey", NamedTextColor.GRAY);
    }

    private final float percentage;
    private final TextColor barColor;
    private final TextColor backgroundColor;
    private int barCodePoints = 0;
    private int currentBarPoint = 0;

    public ProgressBarTag(float percentage, TextColor barColor, TextColor backgroundColor) {
        this.percentage = percentage;
        this.barColor = barColor;
        this.backgroundColor = backgroundColor;
    }

    public static Tag create(float percentage, final ArgumentQueue args, final Context ctx) {
        TextColor barColor = NamedTextColor.LIGHT_PURPLE;
        TextColor backgroundColor = NamedTextColor.WHITE;
        if (args.hasNext()) {
            barColor = resolveColor(args.pop().value(), ctx);
        }
        if (args.hasNext()) {
            backgroundColor = resolveColor(args.pop().value(), ctx);
        }

        return new ProgressBarTag(percentage, barColor, backgroundColor);
    }

    private static @NotNull TextColor resolveColor(final @NotNull String colorName, final @NotNull Context ctx) throws ParsingException {
        final TextColor color;
        if (COLOR_ALIASES.containsKey(colorName)) {
            color = COLOR_ALIASES.get(colorName);
        } else if (colorName.charAt(0) == '#') {
            color = TextColor.fromHexString(colorName);
        } else {
            color = NamedTextColor.NAMES.value(colorName);
        }

        if (color == null) {
            throw ctx.newException(String.format("Unable to parse a color from '%s'. Please use named colours or hex (#RRGGBB) colors.", colorName));
        }
        return color;
    }

    @Override
    protected void init() {
        this.barCodePoints = Math.round(this.size() * percentage);
    }

    @Override
    protected void advanceColor() {
        currentBarPoint++;
    }

    @Override
    protected TextColor color() {
        return this.currentBarPoint < this.barCodePoints ? this.barColor : this.backgroundColor;
    }

    @Override
    public @NotNull Stream<? extends ExaminableProperty> examinableProperties() {
        return Stream.of(
            ExaminableProperty.of("barColor", this.barColor),
            ExaminableProperty.of("backgroundColor", this.backgroundColor)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProgressBarTag)) {
            return false;
        }

        ProgressBarTag that = (ProgressBarTag) o;

        if (Float.compare(that.percentage, percentage) != 0) {
            return false;
        }
        if (barCodePoints != that.barCodePoints) {
            return false;
        }
        if (currentBarPoint != that.currentBarPoint) {
            return false;
        }
        if (!Objects.equals(barColor, that.barColor)) {
            return false;
        }
        return Objects.equals(backgroundColor, that.backgroundColor);
    }

    @Override
    public int hashCode() {
        int result = (percentage != +0.0f ? Float.floatToIntBits(percentage) : 0);
        result = 31 * result + barCodePoints;
        result = 31 * result + currentBarPoint;
        result = 31 * result + (barColor != null ? barColor.hashCode() : 0);
        result = 31 * result + (backgroundColor != null ? backgroundColor.hashCode() : 0);
        return result;
    }
}
