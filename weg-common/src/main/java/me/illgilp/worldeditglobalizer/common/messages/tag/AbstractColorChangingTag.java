package me.illgilp.worldeditglobalizer.common.messages.tag;

import java.util.Collections;
import java.util.PrimitiveIterator;
import java.util.stream.Stream;
import net.kyori.adventure.internal.Internals;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.internal.parser.node.TagNode;
import net.kyori.adventure.text.minimessage.internal.parser.node.ValueNode;
import net.kyori.adventure.text.minimessage.tag.Inserting;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import net.kyori.adventure.text.minimessage.tree.Node;
import net.kyori.examination.Examinable;
import net.kyori.examination.ExaminableProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Got from Kyori's Adventure API
 *
 * @see net.kyori.adventure.text.minimessage.tag.standard.AbstractColorChangingTag
 */
public abstract class AbstractColorChangingTag implements Modifying, Examinable {

    private boolean visited;
    private int size = 0;
    private int disableApplyingColorDepth = -1;

    protected final int size() {
        return this.size;
    }

    @Override
    public final void visit(final @NotNull Node current, final int depth) {
        if (this.visited) {
            throw new IllegalStateException("Color changing tag instances cannot be re-used, return a new one for each resolve");
        }

        if (current instanceof ValueNode) {
            final String value = ((ValueNode) current).value();
            this.size += value.codePointCount(0, value.length());
        } else if (current instanceof TagNode) {
            final TagNode tag = (TagNode) current;
            if (tag.tag() instanceof Inserting) {
                // ComponentTransformation.apply() returns the value of the component placeholder
                ComponentFlattener.textOnly().flatten(((Inserting) tag.tag()).value(), s -> this.size += s.codePointCount(0, s.length()));
            }
        }
    }

    @Override
    public final void postVisit() {
        // init
        this.visited = true;
        this.init();
    }

    @Override
    public final Component apply(final @NotNull Component current, final int depth) {
        if ((this.disableApplyingColorDepth != -1 && depth > this.disableApplyingColorDepth) || current.style().color() != null) {
            if (this.disableApplyingColorDepth == -1 || depth < this.disableApplyingColorDepth) {
                this.disableApplyingColorDepth = depth;
            }
            // This component has its own color applied, which overrides ours
            // We still want to keep track of where we are though if this is text
            if (current instanceof TextComponent) {
                final String content = ((TextComponent) current).content();
                final int len = content.codePointCount(0, content.length());
                for (int i = 0; i < len; i++) {
                    // increment our color index
                    this.advanceColor();
                }
            }
            return current.children(Collections.emptyList());
        }

        this.disableApplyingColorDepth = -1;
        if (current instanceof TextComponent && ((TextComponent) current).content().length() > 0) {
            final TextComponent textComponent = (TextComponent) current;
            final String content = textComponent.content();

            final TextComponent.Builder parent = Component.text();

            // apply
            final int[] holder = new int[1];
            for (final PrimitiveIterator.OfInt it = content.codePoints().iterator(); it.hasNext(); ) {
                holder[0] = it.nextInt();
                final Component comp = Component.text(new String(holder, 0, 1), this.color());
                this.advanceColor();
                parent.append(comp);
            }

            return parent.build();
        }

        return Component.empty().mergeStyle(current);
    }

    // The lifecycle

    protected abstract void init();

    /**
     * Advance the active color.
     */
    protected abstract void advanceColor();

    /**
     * Get the current color, without side-effects.
     *
     * @return the current color
     * @since 4.10.0
     */
    protected abstract TextColor color();

    // misc

    @Override
    public abstract @NotNull Stream<? extends ExaminableProperty> examinableProperties();

    @Override
    public final @NotNull String toString() {
        return Internals.toString(this);
    }

    @Override
    public abstract boolean equals(final @Nullable Object other);

    @Override
    public abstract int hashCode();
}

