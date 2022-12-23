package me.illgilp.worldeditglobalizer.proxy.core.command;

import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.CLIPBOARD_EMPTY;
import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.COMMAND_ARG_PAGE;
import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.COMMAND_ARG_SCHEMATIC;
import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.COMMAND_WEG_SCHEMATIC_DELETE_DESCRIPTION;
import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.COMMAND_WEG_SCHEMATIC_DELETE_SUCCESS;
import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.COMMAND_WEG_SCHEMATIC_ERROR_NOT_FOUND;
import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.COMMAND_WEG_SCHEMATIC_LIST_DESCRIPTION;
import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.COMMAND_WEG_SCHEMATIC_LIST_MESSAGEBOX_LOAD_TOOLTIP;
import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.COMMAND_WEG_SCHEMATIC_LIST_MESSAGEBOX_SCHEMATIC;
import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.COMMAND_WEG_SCHEMATIC_LIST_MESSAGEBOX_TITLE;
import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.COMMAND_WEG_SCHEMATIC_LOAD_DESCRIPTION;
import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.COMMAND_WEG_SCHEMATIC_LOAD_SUCCESS;
import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.COMMAND_WEG_SCHEMATIC_SAVE_DESCRIPTION;
import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.COMMAND_WEG_SCHEMATIC_SAVE_ERROR_ALREADY_EXISTS;
import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.COMMAND_WEG_SCHEMATIC_SAVE_SUCCESS;
import static me.illgilp.worldeditglobalizer.common.permission.Permission.COMMAND_SCHEMATIC_DELETE;
import static me.illgilp.worldeditglobalizer.common.permission.Permission.COMMAND_SCHEMATIC_LIST;
import static me.illgilp.worldeditglobalizer.common.permission.Permission.COMMAND_SCHEMATIC_LOAD;
import static me.illgilp.worldeditglobalizer.common.permission.Permission.COMMAND_SCHEMATIC_SAVE;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.common.messages.box.PaginatedBox;
import me.illgilp.worldeditglobalizer.proxy.core.api.clipboard.WegClipboard;
import me.illgilp.worldeditglobalizer.proxy.core.api.command.intake.annotation.SavedSchematicArg;
import me.illgilp.worldeditglobalizer.proxy.core.api.command.intake.annotation.Source;
import me.illgilp.worldeditglobalizer.proxy.core.api.player.WegPlayer;
import me.illgilp.worldeditglobalizer.proxy.core.api.schematic.WegSchematic;
import me.illgilp.worldeditglobalizer.proxy.core.api.server.ServerNotUsableException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.Command;
import me.illgilp.worldeditglobalizer.proxy.core.intake.Require;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.annotation.Switch;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.annotation.Translated;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class SchematicCommands {

    @Command(
        aliases = { "list" },
        max = 0,
        desc = COMMAND_WEG_SCHEMATIC_LIST_DESCRIPTION
    )
    @Require(
        value = COMMAND_SCHEMATIC_LIST
    )
    public void list(
        @Source WegPlayer source,
        @Translated(COMMAND_ARG_PAGE) @Switch('p') Integer page
    ) throws IOException {
        List<MessageHelper.Builder> rows = new ArrayList<>();
        for (WegSchematic schematic : source.getSchematicContainer().getSchematics()
            .stream()
            .sorted((s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName()))
            .collect(Collectors.toList())
        ) {
            MessageHelper.Builder builder = MessageHelper.builder();
            builder.lazyComponent(lazyBuilder ->
                Component.text("  [L]", NamedTextColor.GREEN)
                    .hoverEvent(HoverEvent.showText(MessageHelper.builder()
                        .locale(lazyBuilder.getLocale())
                        .translation(COMMAND_WEG_SCHEMATIC_LIST_MESSAGEBOX_LOAD_TOOLTIP)
                        .build()))
                    .clickEvent(
                        ClickEvent.runCommand(String.format("/weg schematic load %s", schematic.getName()))
                    ));
            builder.component(Component.space());
            builder.lazyComponent(lazyBuilder ->
                MessageHelper.builder().translation(COMMAND_WEG_SCHEMATIC_LIST_MESSAGEBOX_SCHEMATIC)
                    .tagResolver(Placeholder.unparsed("schematic_name", schematic.getName()))
                    .build());
            rows.add(builder);
        }
        new PaginatedBox(
            MessageHelper.builder()
                .translation(COMMAND_WEG_SCHEMATIC_LIST_MESSAGEBOX_TITLE),
            rows,
            Optional.ofNullable(page).orElse(0),
            "/weg schematic list -p %s"
        )
            .render(MessageHelper.builder())
            .sendMessageTo(source);
    }

    @Command(
        aliases = { "save" },
        max = 1,
        desc = COMMAND_WEG_SCHEMATIC_SAVE_DESCRIPTION
    )
    @Require(
        value = COMMAND_SCHEMATIC_SAVE
    )
    public void save(
        @Source WegPlayer source,
        @Translated(COMMAND_ARG_SCHEMATIC) String name
    ) throws IOException {
        Optional<WegClipboard> clipboard = source.getClipboardContainer().getClipboard();
        if (!clipboard.isPresent()) {
            MessageHelper.builder()
                .translation(CLIPBOARD_EMPTY)
                .sendMessageTo(source);
            return;
        }
        try {
            source.getSchematicContainer().createSchematic(name, clipboard.get());
        } catch (FileAlreadyExistsException e) {
            MessageHelper.builder()
                .translation(COMMAND_WEG_SCHEMATIC_SAVE_ERROR_ALREADY_EXISTS)
                .tagResolver(Placeholder.unparsed("schematic_name", name))
                .sendMessageTo(source);
            return;
        }
        MessageHelper.builder()
            .translation(COMMAND_WEG_SCHEMATIC_SAVE_SUCCESS)
            .tagResolver(Placeholder.unparsed("schematic_name", name))
            .sendMessageTo(source);
    }

    @Command(
        aliases = { "load" },
        max = 1,
        desc = COMMAND_WEG_SCHEMATIC_LOAD_DESCRIPTION
    )
    @Require(
        value = COMMAND_SCHEMATIC_LOAD
    )
    public void load(
        @Source WegPlayer source,
        @Translated(COMMAND_ARG_SCHEMATIC) @SavedSchematicArg String name
    ) throws IOException {
        Optional<WegSchematic> schematic = source.getSchematicContainer().getSchematic(name);
        if (!schematic.isPresent()) {
            MessageHelper.builder()
                .translation(COMMAND_WEG_SCHEMATIC_ERROR_NOT_FOUND)
                .tagResolver(Placeholder.unparsed("schematic_name", name))
                .sendMessageTo(source);
            return;
        }
        source.getClipboardContainer().setClipboard(schematic.get().asClipboard());
        MessageHelper.builder()
            .translation(COMMAND_WEG_SCHEMATIC_LOAD_SUCCESS)
            .tagResolver(Placeholder.unparsed("schematic_name", name))
            .sendMessageTo(source);
        try {
            source.downloadClipboard();
        } catch (ServerNotUsableException ignored) {
        }
    }

    @Command(
        aliases = { "delete" },
        max = 1,
        desc = COMMAND_WEG_SCHEMATIC_DELETE_DESCRIPTION
    )
    @Require(
        value = COMMAND_SCHEMATIC_DELETE
    )
    public void delete(
        @Source WegPlayer source,
        @Translated(COMMAND_ARG_SCHEMATIC) @SavedSchematicArg String name
    ) throws IOException {
        Optional<WegSchematic> schematic = source.getSchematicContainer().getSchematic(name);
        if (!schematic.isPresent()) {
            MessageHelper.builder()
                .translation(COMMAND_WEG_SCHEMATIC_ERROR_NOT_FOUND)
                .tagResolver(Placeholder.unparsed("schematic_name", name))
                .sendMessageTo(source);
            return;
        }
        source.getSchematicContainer().deleteSchematic(schematic.get().getName());
        MessageHelper.builder()
            .translation(COMMAND_WEG_SCHEMATIC_DELETE_SUCCESS)
            .tagResolver(Placeholder.unparsed("schematic_name", name))
            .sendMessageTo(source);
    }
}
