package me.illgilp.worldeditglobalizer.proxy.core.gen;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.common.permission.Permission;
import me.illgilp.worldeditglobalizer.proxy.core.command.SimpleCommandManager;
import me.illgilp.worldeditglobalizer.proxy.core.gen.util.MarkdownUtil;
import me.illgilp.worldeditglobalizer.proxy.core.intake.Parameter;
import net.steppschuh.markdowngenerator.link.Link;
import net.steppschuh.markdowngenerator.list.UnorderedList;
import net.steppschuh.markdowngenerator.rule.HorizontalRule;
import net.steppschuh.markdowngenerator.table.Table;
import net.steppschuh.markdowngenerator.text.code.Code;
import net.steppschuh.markdowngenerator.text.emphasis.BoldText;
import net.steppschuh.markdowngenerator.text.heading.Heading;

public class CommandDocumentationGenerator {

    public static void main(String[] args) throws IOException, IllegalAccessException, InstantiationException {
        MessageHelper.loadMessages();
        StringBuilder sb = new StringBuilder()
            .append(new Heading("Commands & Permissions", 1)).append("\n");
        sb.append(new Heading("Table of Contents", 2)).append("\n").append("\n");
        sb.append(new UnorderedList<>(Stream.of("Commands", "Permissions")
            .map(n -> new Link(n, "#" + MarkdownUtil.getIdFromHeadingText(n)))
            .collect(Collectors.toList()))).append("\n").append("\n");
        sb.append(new Heading("Commands", 2)).append("\n").append("\n");
        Map<Permission, List<SimpleCommandManager.CommandInfo>> permissionCommandInfoMap = new HashMap<>();

        StringBuilder commandsStringBuilder = new StringBuilder();

        SimpleCommandManager simpleCommandManager = new SimpleCommandManager();

        Table.Builder tableBuilder = new Table.Builder()
            .withAlignments(Table.ALIGN_LEFT, Table.ALIGN_LEFT, Table.ALIGN_LEFT)
            .addRow("Command", "Description", "Permissions");

        simpleCommandManager.getCommands()
            .stream()
            .filter(info -> Objects.nonNull(info.getDescription().getShortDescription()))
            .sorted((o1, o2) -> o1.getFullUsage().buildPlain().compareToIgnoreCase(o2.getFullUsage().buildPlain()))
            .forEach(info -> {
                tableBuilder.addRow(
                    new Link("`" + info.getFullUsage().buildPlain() + "`", "#" + MarkdownUtil.getIdFromHeadingText(info.getFullUsage().buildPlain())),
                    MessageHelper.builder()
                        .translation(info.getDescription().getShortDescription())
                        .buildPlain(),
                    info.getDescription().getPermissions().stream()
                        .map(Permission::getPermission)
                        .map(Code::new)
                        .map(Code::toString)
                        .collect(Collectors.joining(", "))
                );
                info.getDescription().getPermissions()
                    .forEach(permission ->
                        permissionCommandInfoMap.computeIfAbsent(permission, permission1 -> new ArrayList<>())
                            .add(info));
                info.getDescription().getParameters().stream()
                    .flatMap(param -> param.getPermissions().stream())
                    .forEach(permission ->
                        permissionCommandInfoMap.computeIfAbsent(permission, permission1 -> new ArrayList<>())
                            .add(info));
                commandsStringBuilder.append("\n");
                commandsStringBuilder.append(new HorizontalRule()).append("\n").append("\n");
                commandsStringBuilder.append(new Heading(info.getFullUsage().buildPlain(), 3)).append("\n").append("\n");
                commandsStringBuilder.append(new BoldText("Command:"))
                    .append(" ")
                    .append(new Code(info.getFullUsage().buildPlain()))
                    .append("\n")
                    .append("<br/>")
                    .append("\n");
                commandsStringBuilder.append(new BoldText("Description:"))
                    .append(" ")
                    .append(new Code(MessageHelper.builder()
                        .translation(info.getDescription().getShortDescription())
                        .buildPlain()))
                    .append("\n")
                    .append("<br/>")
                    .append("\n");
                List<String> perms = info.getDescription().getPermissions().stream()
                    .map(Permission::getPermission)
                    .map(Code::new)
                    .map(Code::toString)
                    .collect(Collectors.toList());
                commandsStringBuilder.append(new BoldText("Permissions:"));
                if (perms.size() > 0) {
                    commandsStringBuilder.append("\n").append("\n")
                        .append(new UnorderedList<>(perms))
                        .append("\n")
                        .append("  <br/>");
                } else {
                    commandsStringBuilder.append(" none")
                        .append("\n")
                        .append("<br/>");
                }
                commandsStringBuilder
                    .append("\n")
                    .append("\n");
                commandsStringBuilder.append(new BoldText("Arguments:"));
                if (info.getDescription().getParameters().size() > 0) {
                    commandsStringBuilder.append("\n\n");
                    Table.Builder argsTableBuilder = new Table.Builder()
                        .withAlignments(Table.ALIGN_CENTER, Table.ALIGN_CENTER, Table.ALIGN_CENTER, Table.ALIGN_CENTER, Table.ALIGN_CENTER, Table.ALIGN_CENTER, Table.ALIGN_CENTER)
                        .addRow("Position", "Argument", "Description", "Optional?", "Boolean Flag?", "Value Flag?", "Default value", "Permissions");
                    int i = 0;
                    for (Parameter parameter : info.getDescription().getParameters()) {
                        argsTableBuilder.addRow(
                            ++i,
                            parameter.getName().buildPlain(),
                            parameter.getDescription().map(MessageHelper.Builder::buildPlain).orElse("-"),
                            parameter.getOptionType().isOptional() ? "yes" : "no",
                            parameter.getOptionType().getFlag() != null && !parameter.getOptionType().isValueFlag() ? "yes" : "no",
                            parameter.getOptionType().isValueFlag() ? "yes" : "no",
                            String.join(" ", parameter.getDefaultValue()),
                            parameter.getPermissions().stream()
                                .map(Permission::getPermission)
                                .map(Code::new)
                                .map(Code::toString)
                                .collect(Collectors.joining(", "))
                        );
                    }
                    commandsStringBuilder.append(argsTableBuilder.build()).append("\n");
                } else {
                    commandsStringBuilder.append(" ").append("none").append("\n");
                }
            });
        sb.append(tableBuilder.build()).append("\n");

        sb.append(commandsStringBuilder).append("\n");

        sb.append(new Heading("Permissions", 2)).append("\n").append("\n");

        Table.Builder permTableBuilder = new Table.Builder()
            .withAlignments(Table.ALIGN_LEFT, Table.ALIGN_LEFT, Table.ALIGN_LEFT)
            .addRow("Permission", "Description", "Commands");


        for (Permission permission : Arrays.stream(Permission.values()).sorted().collect(Collectors.toList())) {
            permTableBuilder.addRow(
                new Code(permission.getPermission()),
                permission.getDescription(),
                permissionCommandInfoMap.getOrDefault(permission, new ArrayList<>())
                    .stream()
                    .map(cmd -> new Link(new Code(cmd.getFullUsage().buildPlain()).toString(), "#" + MarkdownUtil.getIdFromHeadingText(cmd.getFullUsage().buildPlain())))
                    .map(Objects::toString)
                    .collect(Collectors.joining(", "))
            );
        }
        sb.append(permTableBuilder.build()).append("\n");
        File docsFolder = new File("docs");
        if (!docsFolder.exists()) {
            docsFolder.mkdirs();
        }
        File file = new File(docsFolder, "Commands & Permissions.md");
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            for (String s : Arrays.stream(sb.toString().split("\r\n")).flatMap(s -> Arrays.stream(s.split("\n"))).collect(Collectors.toList())) {
                writer.println(s);
            }
            writer.flush();
        }
    }

}
