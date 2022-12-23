package me.illgilp.worldeditglobalizer.common.messages.box;

import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.MESSAGEBOX_NAVIGATION_TOOLTIP;

import java.util.List;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class PaginatedBox extends MessageBox {

    private final List<MessageHelper.Builder> entries;
    private final int page;
    private final int totalPages;
    private final String navigateCommand;

    public PaginatedBox(MessageHelper.Builder title, List<MessageHelper.Builder> entries, int page, String navigateCommand) {
        super(title);
        this.entries = entries;
        this.navigateCommand = navigateCommand;
        if (this.entries.size() > 9) {
            this.totalPages = Double.valueOf(Math.ceil(((double) entries.size()) / 8.0)).intValue();
        } else {
            this.totalPages = 1;
        }
        if (page < 0) {
            this.page = 0;
        } else if (page >= this.totalPages) {
            this.page = this.totalPages - 1;
        } else {
            this.page = page;
        }
    }

    @Override
    public MessageHelper.Builder render(MessageHelper.Builder builder) {
        super.render(builder);
        builder.component(
            this.entries.stream()
                .skip(this.page * 8L)
                .limit(this.totalPages == 1 ? 9 : 8)
                .collect(MessageHelper.Builder.toBuilder(Component.newline()))
        );
        if (this.totalPages > 1) {
            MessageHelper.Builder bottomMessage = MessageHelper.builder();
            bottomMessage.component(Component.space());
            if (this.page > 0) {
                bottomMessage.lazyComponent(lazyBuilder -> Component.text("<<<", NamedTextColor.DARK_PURPLE)
                    .hoverEvent(HoverEvent.showText(MessageHelper.builder()
                        .locale(lazyBuilder.getLocale())
                        .translation(MESSAGEBOX_NAVIGATION_TOOLTIP)
                        .build()
                    ))
                    .clickEvent(ClickEvent.runCommand(String.format(navigateCommand, page - 1)))
                );
                bottomMessage.component(Component.space());
            }
            bottomMessage.component(MessageHelper.builder()
                .translation(TranslationKey.MESSAGEBOX_NAVIGATION_PAGE)
                .tagResolver(Placeholder.unparsed("current_page", String.valueOf(this.page + 1)))
                .tagResolver(Placeholder.unparsed("total_pages", String.valueOf(this.totalPages)))
            );
            if ((this.page + 1) < this.totalPages) {
                bottomMessage.component(Component.space());
                bottomMessage.lazyComponent(lazyBuilder -> Component.text(">>>", NamedTextColor.DARK_PURPLE)
                    .hoverEvent(HoverEvent.showText(MessageHelper.builder()
                        .locale(lazyBuilder.getLocale())
                        .translation(MESSAGEBOX_NAVIGATION_TOOLTIP)
                        .build()
                    ))
                    .clickEvent(ClickEvent.runCommand(String.format(navigateCommand, page + 1)))
                );
            }
            bottomMessage.component(Component.space());
            builder.component(Component.newline());
            builder.component(centerMessage(46, bottomMessage));
        }
        return builder;
    }
}
