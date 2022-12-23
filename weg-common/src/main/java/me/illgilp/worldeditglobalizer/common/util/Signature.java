package me.illgilp.worldeditglobalizer.common.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Signature {


    public static byte[] sign(byte[] payload, byte[] secret) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret, "HmacSHA256");
        sha256_HMAC.init(secret_key);
        return sha256_HMAC.doFinal(payload);
    }

    public static boolean verify(byte[] payload, byte[] signature, byte[] secret) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret, "HmacSHA256");
        sha256_HMAC.init(secret_key);

        byte[] sign = sha256_HMAC.doFinal(payload);
        return Arrays.equals(sign, signature);
    }

}
