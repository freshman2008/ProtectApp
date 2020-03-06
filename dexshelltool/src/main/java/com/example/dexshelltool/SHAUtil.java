package com.example.dexshelltool;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHAUtil {
    public static byte[] sha1(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input);
        return result;
    }
}
