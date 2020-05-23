package me.illgilp.worldeditglobalizerbungee.manager;

import com.google.common.base.Charsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.player.OfflinePlayer;
import me.illgilp.worldeditglobalizerbungee.player.Player;
import me.illgilp.worldeditglobalizerbungee.player.WEGOfflinePlayer;
import me.illgilp.worldeditglobalizerbungee.player.WEGOnlinePlayer;
import me.illgilp.worldeditglobalizerbungee.storage.model.UserCacheModel;
import me.illgilp.worldeditglobalizerbungee.storage.model.UserCacheModelBuilder;
import me.illgilp.worldeditglobalizerbungee.storage.table.UserCacheTable;
import me.illgilp.worldeditglobalizerbungee.util.UUIDFetcher;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PlayerManager {

    private static PlayerManager instance;

    public static PlayerManager getInstance() {
        return instance == null ? instance = new PlayerManager() : instance;
    }

    private Map<UUID, Player> onlinePlayers = new ConcurrentHashMap<>();

    private Map<UUID, OfflinePlayer> offlinePlayersUUID = new ConcurrentHashMap<>();
    private Map<String, OfflinePlayer> offlinePlayersName = new ConcurrentHashMap<>();
    private Map<UUID, Long> lastUse = new ConcurrentHashMap<>();

    public PlayerManager() {
        BungeeCord.getInstance().getScheduler().schedule(WorldEditGlobalizerBungee.getInstance(),
            () -> {
                for (Map.Entry<UUID, Long> uuidLongEntry : new HashMap<>(lastUse).entrySet()) {
                    if (System.currentTimeMillis() - uuidLongEntry.getValue() >= 1000*60*30) {
                        OfflinePlayer p = offlinePlayersUUID.remove(uuidLongEntry.getKey());
                        offlinePlayersName.remove(p.getName().toLowerCase());
                        lastUse.remove(uuidLongEntry.getKey());
                    }
                }

                for (Map.Entry<UUID, Player> uuidPlayerEntry : new HashMap<>(onlinePlayers).entrySet()) {
                    if (!uuidPlayerEntry.getValue().isOnline()) {
                        onlinePlayers.remove(uuidPlayerEntry.getKey());
                    }
                }

            }, 1000, 1000, TimeUnit.SECONDS
        );
    }

    public Player getPlayer(UUID uuid) {
        if (onlinePlayers.containsKey(uuid)) {
            Player p = onlinePlayers.get(uuid);
            ProxiedPlayer pp = BungeeCord.getInstance().getPlayer(uuid);
            if (pp != null) {
                if (p.getProxiedPlayer().equals(pp)) {
                    return p;
                }
            }
        }

        ProxiedPlayer pp = BungeeCord.getInstance().getPlayer(uuid);
        if (pp == null) {
            return null;
        }

        Player player = new WEGOnlinePlayer(pp);
        onlinePlayers.put(player.getUniqueId(), player);
        return player;
    }

    public Player getPlayer(String name) {
        ProxiedPlayer pp = BungeeCord.getInstance().getPlayer(name);
        if (pp == null) {
            return null;
        }
        if (onlinePlayers.containsKey(pp.getUniqueId())) {
            Player p = onlinePlayers.get(pp.getUniqueId());
            if (p.getProxiedPlayer().equals(pp)) {
                return p;
            }
        }

        Player player = new WEGOnlinePlayer(pp);
        onlinePlayers.put(player.getUniqueId(), player);
        return player;
    }

    public OfflinePlayer getOfflinePlayer(UUID uuid) {
        if (BungeeCord.getInstance().getPlayer(uuid) != null) {
            return getPlayer(uuid);
        }
        if (offlinePlayersUUID.containsKey(uuid)) {
            lastUse.put(uuid, System.currentTimeMillis());
            return offlinePlayersUUID.get(uuid);
        }
        UserCacheTable table = WorldEditGlobalizerBungee.getInstance().getDatabase().getTable(UserCacheTable.class);
        UserCacheModel userCacheModel = table.getExact(new UserCacheModelBuilder().setUUID(uuid).createUserCacheModel());
        if (userCacheModel == null) {
            return null;
        }

        OfflinePlayer offlinePlayer = new WEGOfflinePlayer(userCacheModel.getUUID(), userCacheModel.getDisplayName());
        this.offlinePlayersUUID.put(offlinePlayer.getUniqueId(), offlinePlayer);
        this.offlinePlayersName.put(offlinePlayer.getName().toLowerCase(), offlinePlayer);
        this.lastUse.put(offlinePlayer.getUniqueId(), System.currentTimeMillis());

        return offlinePlayer;
    }

    public OfflinePlayer getOfflinePlayer(String name) {
        if (BungeeCord.getInstance().getPlayer(name) != null) {
            return getPlayer(name);
        }

        if (offlinePlayersName.containsKey(name.toLowerCase())) {
            OfflinePlayer p = offlinePlayersName.get(name.toLowerCase());
            lastUse.put(p.getUniqueId(), System.currentTimeMillis());
            return p;
        }
        UserCacheTable table = WorldEditGlobalizerBungee.getInstance().getDatabase().getTable(UserCacheTable.class);
        UserCacheModel userCacheModel = table.getExact(new UserCacheModelBuilder().setName(name.toLowerCase()).createUserCacheModel());
        if (userCacheModel == null) {
            UUIDFetcher.PlayerData data = UUIDFetcher.getPlayerData(name);
            if (data != null) {
                userCacheModel = new UserCacheModel(data.getUUID(), data.getName().toLowerCase(), data.getName());

                this.saveOfflinePlayer(new WEGOfflinePlayer(data.getUUID(), data.getName()));
            } else {
                OfflinePlayer offlinePlayer = new WEGOfflinePlayer(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8)), name, false);
                this.offlinePlayersUUID.put(offlinePlayer.getUniqueId(), offlinePlayer);
                this.offlinePlayersName.put(offlinePlayer.getName().toLowerCase(), offlinePlayer);
                this.lastUse.put(offlinePlayer.getUniqueId(), System.currentTimeMillis());

                return offlinePlayer;
            }
        }

        OfflinePlayer offlinePlayer = new WEGOfflinePlayer(userCacheModel.getUUID(), userCacheModel.getDisplayName());
        this.offlinePlayersUUID.put(offlinePlayer.getUniqueId(), offlinePlayer);
        this.offlinePlayersName.put(offlinePlayer.getName().toLowerCase(), offlinePlayer);
        this.lastUse.put(offlinePlayer.getUniqueId(), System.currentTimeMillis());

        return offlinePlayer;
    }


    public OfflinePlayer getSavedOfflinePlayer(UUID uuid) {
        UserCacheTable table = WorldEditGlobalizerBungee.getInstance().getDatabase().getTable(UserCacheTable.class);
        UserCacheModel userCacheModel = table.getExact(new UserCacheModelBuilder().setUUID(uuid).createUserCacheModel());
        if (userCacheModel == null) {
            return null;
        }

        return new WEGOfflinePlayer(userCacheModel.getUUID(), userCacheModel.getDisplayName());
    }

    public OfflinePlayer getSavedOfflinePlayer(String name) {
        UserCacheTable table = WorldEditGlobalizerBungee.getInstance().getDatabase().getTable(UserCacheTable.class);
        UserCacheModel userCacheModel = table.getExact(new UserCacheModelBuilder().setName(name.toLowerCase()).createUserCacheModel());
        if (userCacheModel == null) {
            return null;
        }

        return new WEGOfflinePlayer(userCacheModel.getUUID(), userCacheModel.getDisplayName());
    }

    public void saveOfflinePlayer(OfflinePlayer offlinePlayer) {
        UserCacheTable table = WorldEditGlobalizerBungee.getInstance().getDatabase().getTable(UserCacheTable.class);
        UserCacheModel userCacheModel = table.getExact(new UserCacheModelBuilder().setUUID(offlinePlayer.getUniqueId()).createUserCacheModel());
        if (userCacheModel == null) {
            userCacheModel = new UserCacheModel(offlinePlayer.getUniqueId(), offlinePlayer.getName().toLowerCase(), offlinePlayer.getName());
        }

        userCacheModel.setName(offlinePlayer.getName().toLowerCase());
        userCacheModel.setDisplayName(offlinePlayer.getName());

        userCacheModel.save();
    }

    public List<OfflinePlayer> getOfflinePlayers(String startWith) {
        UserCacheTable table = WorldEditGlobalizerBungee.getInstance().getDatabase().getTable(UserCacheTable.class);
        List<OfflinePlayer> offlinePlayers = new ArrayList<>();
        List<UserCacheModel> models = new ArrayList<>();

        if (startWith.length() == 0) {
            models = table.getAll();
        } else {
            try {
                models = table.getDao().queryBuilder().where().raw(" name LIKE '" + startWith.toLowerCase() + "%'").query();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        for (UserCacheModel model : models) {
            OfflinePlayer player = getPlayer(model.getUUID());
            if (player == null) {
                player = new WEGOfflinePlayer(model.getUUID(), model.getDisplayName());
            }
            offlinePlayers.add(player);
        }

        return offlinePlayers;

    }

    public List<String> getOfflinePlayersNames(String startWith) {
        UserCacheTable table = WorldEditGlobalizerBungee.getInstance().getDatabase().getTable(UserCacheTable.class);
        HashSet<String> offlinePlayers = new HashSet<>();
        List<UserCacheModel> models = new ArrayList<>();

        if (startWith.length() == 0) {
            models = table.getAll();
        } else {
            try {
                models = table.getDao().queryBuilder().where().raw(" name LIKE '" + startWith.toLowerCase() + "%'").query();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        for (UserCacheModel model : models) {
            offlinePlayers.add(model.getDisplayName());
        }

        return new ArrayList<>(offlinePlayers);

    }
}
