package com.Bieling.CrapChat.api;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESEncrypt
{
    private final static byte[] key = "M02cnQ51Ji97vwT4".getBytes();

    public static byte[] decrypt(byte abyte0[]) throws Exception
    {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(2, new SecretKeySpec(key, "AES"));
        return cipher.doFinal(abyte0);
    }

    public static byte[] encrypt(byte abyte0[]) throws Exception
    {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(1, new SecretKeySpec(key, "AES"));
        return cipher.doFinal(abyte0);
    }
}