package com.example.shell;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {
    private static final String KEY_ALGORITHM = "AES";
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    public static final byte[] ZERO_IV = {
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    };

    private static byte[] localKey = null;
    public static void init(String key) {
        localKey = key.getBytes();
    }

    public static byte[] encrypt(byte[] content) {
        return encrypt(content, localKey, ZERO_IV);
    }

    public static byte[] decrypt(byte[] content) {
        return decrypt(content, localKey, ZERO_IV);
    }

    public static byte[] encrypt(byte[] content, byte[] key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Exception ex) {
            System.out.println(AESUtil.class.getName() + "->" + ex.getMessage());
        }
        return null;
    }

    public static byte[] decrypt(byte[] content, byte[] key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Exception ex) {
            System.out.println(AESUtil.class.getName() + "->" + ex.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        String content = "hello";
        String key = "1122334455667788";
        System.out.println("content:" + content);
        byte[] s1 = AESUtil.encrypt(content.getBytes(), key.getBytes(), ZERO_IV);
        byte[] s2 = AESUtil.decrypt(s1, key.getBytes(), ZERO_IV);
        for(int i = 0; i<s2.length; i++) {
            System.out.println(s2[i]);
        }
    }
}
