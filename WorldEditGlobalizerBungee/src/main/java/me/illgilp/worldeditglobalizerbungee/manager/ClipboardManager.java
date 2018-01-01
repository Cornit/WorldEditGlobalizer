package me.illgilp.worldeditglobalizerbungee.manager;

import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.clipboard.Clipboard;
import me.illgilp.worldeditglobalizerbungee.util.PacketDataSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class ClipboardManager {

    private static ClipboardManager instance;
    private File folder;

    public ClipboardManager(File folder) {
        this.folder = folder;
        folder.mkdirs();
    }

    public static ClipboardManager getInstance() {
        if(instance == null){
            instance = new ClipboardManager(new File(WorldEditGlobalizerBungee.getInstance().getDataFolder(),"clipboards"));
        }
        return instance;
    }

    private void checkFolder(){
        folder.mkdirs();
    }

    public void saveClipboard(Clipboard clipboard){
        checkFolder();
        try {
            File cb = new File(folder, clipboard.getOwner().toString() + ".clipboard");
            if (!cb.exists()) cb.createNewFile();
            FileOutputStream out = new FileOutputStream(cb);
            PacketDataSerializer serializer = new PacketDataSerializer();
            serializer.writeInt(clipboard.getHash());
            serializer.writeString(clipboard.getFromServer());
            serializer.writeArray(clipboard.getData());
            out.write(serializer.toByteArray());
            out.flush();
            out.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public boolean hasClipboard(UUID uuid){
        checkFolder();
        File cb = new File(folder, uuid.toString() + ".clipboard");
        return cb.exists();
    }

    public Clipboard getClipboard(UUID uuid){
        if(!hasClipboard(uuid))return null;
        File cb = new File(folder, uuid.toString() + ".clipboard");
        try {
            PacketDataSerializer serializer = new PacketDataSerializer(Files.readAllBytes(cb.toPath()));
            int hash = serializer.readInt();
            String fromServer = serializer.readString();
            Clipboard clipboard = new Clipboard(uuid, serializer.readArray(),hash,fromServer);
            return clipboard;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void removeClipboard(UUID uuid){
        if(!hasClipboard(uuid))return;
        File cb = new File(folder, uuid.toString() + ".clipboard");
        cb.delete();
    }

    public void removeAll(){
        checkFolder();
        for(File file : folder.listFiles()){
            file.delete();
        }
    }

    public File getClipboardFile(UUID uuid){
        if(!hasClipboard(uuid))return null;
        return new File(folder, uuid.toString() + ".clipboard");
    }
}
