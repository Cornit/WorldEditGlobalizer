package me.illgilp.worldeditglobalizer.proxy.velocity;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.player.PlayerSettings;
import com.velocitypowered.api.proxy.player.ResourcePackInfo;
import com.velocitypowered.api.proxy.player.TabList;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.api.util.ModInfo;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import me.illgilp.worldeditglobalizer.proxy.velocity.util.AdventureVelocityAdapter;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AdventureVelocityAdapterTests {

    private static final Component TEST_COMPONENT =
        Component.text().content("c")
            .color(NamedTextColor.GOLD)
            .append(Component.text("o", NamedTextColor.DARK_AQUA))
            .append(Component.text("l", NamedTextColor.LIGHT_PURPLE))
            .append(Component.text("o", NamedTextColor.DARK_PURPLE))
            .append(Component.text("u", NamedTextColor.BLUE))
            .append(Component.text("r", NamedTextColor.DARK_GREEN))
            .append(Component.text("s", NamedTextColor.RED))
            .build();

    @Test
    void test_AdventureVelocityAdapter_sendMessage() {
        Identity identity = Identity.identity(UUID.randomUUID());
        MessageType messageType = MessageType.CHAT;

        Player player = new SendMessageDummyPlayer((source, message, type) -> {
            Assertions.assertEquals(identity, source);
            Assertions.assertEquals(TEST_COMPONENT, message);
            Assertions.assertEquals(messageType, type);
        });
        AdventureVelocityAdapter.sendMessage(player, identity, TEST_COMPONENT, messageType);
    }

    @Test
    void test_AdventureVelocityAdapter_sendActionBar() {
        Player player = new SendActionBarDummyPlayer(
            (message) -> Assertions.assertEquals(TEST_COMPONENT, message)
        );
        AdventureVelocityAdapter.sendActionBar(player, TEST_COMPONENT);
    }

    private interface SendMessageHandler {

        void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type);

    }

    @RequiredArgsConstructor
    public static class SendMessageDummyPlayer extends DummyPlayer {
        private final SendMessageHandler sendMessageHandler;

        @Override
        public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
            sendMessageHandler.sendMessage(source, message, type);
        }
    }

    @RequiredArgsConstructor
    public static class SendActionBarDummyPlayer extends DummyPlayer {
        private final Consumer<Component> sendActionBarHandler;

        @Override
        public void sendActionBar(@NotNull Component message) {
            sendActionBarHandler.accept(message);
        }
    }

    @RequiredArgsConstructor
    public static class DummyPlayer implements Player {

        @Override
        public String getUsername() {
            return null;
        }

        @Override
        public @Nullable Locale getEffectiveLocale() {
            return null;
        }

        @Override
        public void setEffectiveLocale(Locale locale) {

        }

        @Override
        public UUID getUniqueId() {
            return null;
        }

        @Override
        public Optional<ServerConnection> getCurrentServer() {
            return Optional.empty();
        }

        @Override
        public PlayerSettings getPlayerSettings() {
            return null;
        }

        @Override
        public Optional<ModInfo> getModInfo() {
            return Optional.empty();
        }

        @Override
        public long getPing() {
            return 0;
        }

        @Override
        public boolean isOnlineMode() {
            return false;
        }

        @Override
        public ConnectionRequestBuilder createConnectionRequest(RegisteredServer server) {
            return null;
        }

        @Override
        public List<GameProfile.Property> getGameProfileProperties() {
            return null;
        }

        @Override
        public void setGameProfileProperties(List<GameProfile.Property> properties) {

        }

        @Override
        public GameProfile getGameProfile() {
            return null;
        }

        @Override
        public void clearHeaderAndFooter() {

        }

        @Override
        public Component getPlayerListHeader() {
            return null;
        }

        @Override
        public Component getPlayerListFooter() {
            return null;
        }

        @Override
        public TabList getTabList() {
            return null;
        }

        @Override
        public void disconnect(Component reason) {

        }

        @Override
        public void spoofChatInput(String input) {

        }

        @Override
        public void sendResourcePack(String url) {

        }

        @Override
        public void sendResourcePack(String url, byte[] hash) {

        }

        @Override
        public void sendResourcePackOffer(ResourcePackInfo packInfo) {

        }

        @Override
        public @Nullable ResourcePackInfo getAppliedResourcePack() {
            return null;
        }

        @Override
        public @Nullable ResourcePackInfo getPendingResourcePack() {
            return null;
        }

        @Override
        public boolean sendPluginMessage(ChannelIdentifier identifier, byte[] data) {
            return false;
        }

        @Override
        public @Nullable String getClientBrand() {
            return null;
        }

        @Override
        public Tristate getPermissionValue(String permission) {
            return null;
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return null;
        }

        @Override
        public Optional<InetSocketAddress> getVirtualHost() {
            return Optional.empty();
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public ProtocolVersion getProtocolVersion() {
            return null;
        }

        @Override
        public @NotNull Identity identity() {
            return Identity.nil();
        }

    }
}
