package me.illgilp.mavenrepolib.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class IOUtils {

    public static String readString(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String hashFile(File file) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(Files.readAllBytes(file.toPath()));
            byte[] dec = messageDigest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : dec) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString().toLowerCase();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
