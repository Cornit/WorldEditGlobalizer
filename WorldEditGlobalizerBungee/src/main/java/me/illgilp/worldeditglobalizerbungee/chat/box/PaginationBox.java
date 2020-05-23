package me.illgilp.worldeditglobalizerbungee.chat.box;

import java.util.ArrayList;
import java.util.List;
import me.illgilp.worldeditglobalizerbungee.chat.chatevent.ChatEventBuilder;
import me.illgilp.worldeditglobalizerbungee.manager.MessageManager;
import me.illgilp.worldeditglobalizerbungee.util.ComponentUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public abstract class PaginationBox extends MessageBox {

    private List<MessageBoxEntry> entries;
    private int maxEntriesPerPage = 18;
    private int page;
    private String pageCommand;

    public PaginationBox(TextComponent title, ChatColor borderColor, String pageCommand, List<MessageBoxEntry> entries) {
        super(title, borderColor);
        this.pageCommand = pageCommand;
        this.entries = entries;
    }

    @Override
    public List<MessageBoxEntry> getEntries() {
        int entriesOffset = this.page * maxEntriesPerPage;

        if (entriesOffset >= entries.size()) {
            return new ArrayList<>();
        }
        List<MessageBoxEntry> copy = new ArrayList<>();

        for (int i = entriesOffset; i < Math.min(entriesOffset + maxEntriesPerPage, entries.size()); i++) {
            copy.add(entries.get(i));
        }

        return copy;
    }

    public TextComponent create(int page) {
        this.page = page;
        int pages = new Double(Math.ceil(((double) entries.size()) / ((double) maxEntriesPerPage))).intValue();
        if (pages <= 0) pages = 1;
        TextComponent result = super.create();
        TextComponent pageComp = ComponentUtils.of("");
        TextComponent pageNumberComp = ComponentUtils.of(MessageManager.getRawMessageOrEmpty("chat.box.page.number", (page+1), pages));
        if (page > 0) {
            pageComp.addExtra(
                new ChatEventBuilder()
                .setText("§6<<< ")
                .setHover(MessageManager.getRawMessageOrEmpty("chat.box.page.previous.tooltip"))
                .setCommand(String.format(this.pageCommand, page))
                .create().toComponent()
            );
        }

        pageComp.addExtra(pageNumberComp);
        if (page < (pages - 1)) {
            pageComp.addExtra(
                new ChatEventBuilder()
                    .setText(" §6>>>")
                    .setHover(MessageManager.getRawMessageOrEmpty("chat.box.page.next.tooltip"))
                    .setCommand(String.format(this.pageCommand, page+2))
                    .create().toComponent()
            );
        }
        result.addExtra(ComponentUtils.newLine());
        result.addExtra(super.centerAndBorder(pageComp));
        return result;
    }

}
