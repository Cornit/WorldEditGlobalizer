package me.illgilp.worldeditglobalizercommon.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.digest.DigestUtils;

public class HiddenDataUtil {

    private static final String SEQUENCE_HEADER;
    private static final String SEQUENCE_FOOTER;
    private static final String SEQUENCE_SEPERATOR;

    private static final String CHARS = "0123456789abcdef";


    static {
        SEQUENCE_HEADER = "§r" + encodedToColors(encode("WH".getBytes(StandardCharsets.UTF_8))) + "§r";
        SEQUENCE_FOOTER = "§r" + encodedToColors(encode("WF".getBytes(StandardCharsets.UTF_8))) + "§r";
        SEQUENCE_SEPERATOR = "§r" + encodedToColors(encode("HS".getBytes(StandardCharsets.UTF_8))) + "§r";
    }


    public static String encryptData(byte[] data, long[] key) {
        return quote(encodedToColors(encrypt(data, key)) + SEQUENCE_SEPERATOR + encodedToColors(encrypt(DigestUtils.sha(data), new long[]{1})));
    }

    public static String encodeData(byte[] data) {
        return quote(encodedToColors(encode(data)));
    }

    public static boolean hasData(String input) {
        if (input == null) return false;

        return input.indexOf(SEQUENCE_HEADER) > -1 && input.indexOf(SEQUENCE_FOOTER) > -1;
    }

    public static byte[] extractHiddenEncodedData(String input) throws DecoderException {
        return decode(extract(input).replace("§",""));
    }

    public static byte[] extractHiddenEncryptedData(String input, long[] keys) throws DecoderException {
        if (!input.contains(SEQUENCE_SEPERATOR)) {
            throw new DecoderException("hash not found");
        }
        String extracted = extract(input);
        byte[] data = decrypt(extracted.split(SEQUENCE_SEPERATOR)[0].replace("§",""), keys);
        byte[] hash = decrypt(extracted.split(SEQUENCE_SEPERATOR)[1].replace("§",""), new long[]{1});

        byte[] newHash = DigestUtils.sha(data);
        if (!Arrays.equals(hash, newHash)) {
            throw new DecoderException("invalid key");
        }

        return data;
    }

    /**
     * Internal stuff.
     */
    private static String quote(String input) {
        if (input == null) return null;
        return SEQUENCE_HEADER + input + SEQUENCE_FOOTER;
    }

    private static String extract(String input) {
        if (input == null) return null;

        int start = input.indexOf(SEQUENCE_HEADER);
        int end = input.indexOf(SEQUENCE_FOOTER);

        if (start < 0 || end < 0) {
            return null;
        }

        return input.substring(start + SEQUENCE_HEADER.length(), end);
    }

    private static String encodedToColors(String encoded) {
        StringBuilder b = new StringBuilder();
        for (char c : encoded.toCharArray()) {
            b.append('§').append(c);
        }
        return b.toString();
    }

    private static String encode(byte[] data) {
        StringBuilder encoded = new StringBuilder();
        for (byte dat : data) {
            int unsignedByte = (int) dat - Byte.MIN_VALUE;
            byte[] bytes = new byte[] {
                (byte) ((unsignedByte >> 4) & 0xf),
                (byte) (unsignedByte & 0xf)
            };

            for (byte aByte : bytes) {
                encoded.append(CHARS.charAt(aByte));
            }
        }
        return encoded.toString();
    }

    private static String encrypt(byte[] data, long[] key) {
        StringBuilder encoded = new StringBuilder();
        int counter = 0;
        for (byte dat : data) {
            int unsignedByte = (int) dat - Byte.MIN_VALUE;
            byte[] bytes = new byte[] {
                (byte) ((unsignedByte >> 4) & 0xf),
                (byte) (unsignedByte & 0xf)
            };

            for (byte aByte : bytes) {
                Random random = new Random(key[counter++ % key.length]);
                int in = aByte;
                int ch = in;
                int cha = (random.nextInt(255) + 1) * (random.nextBoolean() ? 1 : -1);
                ch = in + cha;
                ch = (ch + 256) % 16;
                encoded.append(CHARS.charAt(ch));

            }


        }
        
        return encoded.toString();
    }

    private static byte[] decode(String encoded) {
        byte[] decodedBytes = new byte[encoded.length()/2];

        for (int i = 0; i < encoded.toCharArray().length; i+=2) {
            char c1 = encoded.charAt(i);
            char c2 = encoded.charAt(i+1);

            byte b1 = (byte) CHARS.indexOf(c1);
            byte b2 = (byte) CHARS.indexOf(c2);

            byte val = (byte) ((((b1 & 0xf) << 4) | (b2 & 0xf)) + Byte.MIN_VALUE);
            decodedBytes[i / 2] = val;
        }

        return decodedBytes;

    }

    private static byte[] decrypt(String encoded, long[] key) {
        byte[] decodedBytes = new byte[encoded.length()/2];

        int counter = 0;
        for (int i = 0; i < encoded.toCharArray().length; i+=2) {
            char c1 = encoded.charAt(i);
            char c2 = encoded.charAt(i+1);

            byte b1 = 0;
            byte b2 = 0;

            {
                Random random = new Random(key[counter++ % key.length]);
                int pos = CHARS.indexOf(c1);
                int cha = (random.nextInt(255) + 1);
                if (random.nextBoolean()) {
                    pos = cha - pos;
                    b1 = (byte) ((16 - (pos % 16)) % 16);
                } else {
                    pos = ((-cha) + 256) - pos;
                    b1 = (byte) ((16 - (pos % 16)) % 16);
                }
            }

            {
                Random random = new Random(key[counter++ % key.length]);
                int pos = CHARS.indexOf(c2);
                int cha = (random.nextInt(255) + 1);
                if (random.nextBoolean()) {
                    pos = cha - pos;
                    b2 = (byte) ((16 - (pos % 16)) % 16);
                } else {
                    pos = ((-cha) + 256) - pos;
                    b2 = (byte) ((16 - (pos % 16)) % 16);
                }

            }

            byte val = (byte) ((((b1 & 0xf) << 4) | (b2 & 0xf)) + Byte.MIN_VALUE);
            decodedBytes[i / 2] = val;
        }

        return decodedBytes;

    }









}
