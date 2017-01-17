package com.wuyz.simpleencrypt;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.SecureRandom;

/**
 * Created by wuyz on 9/14/2016.
 * main
 */
public class Main {

    public static void main(String[] args) {
        if (args == null || args.length != 4) {
            System.err.println("argument num error! usage: java SimpleEncrypt key e[encrypt]|d[decrypt] src dest");
            return;
        }

        final String key = args[0];
        final String type = args[1];
        final String src = args[2];
        final String dest = args[3];

        if (!type.equals("e") && !type.equals("d")) {
            System.err.println("type error! usage: java SimpleEncrypt key e[encrypt]|d[decrypt] src dest");
            return;
        }

        if (src.equals(dest)) {
            System.err.println("src cannot be same as dest");
            return;
        }

        File file = new File(src);
        if (!file.exists() || !file.isFile()) {
            System.err.println("file not exist");
            return;
        }

        boolean encrypt = type.equals("e");
        Cipher cipher = getCipher(key.getBytes(), "AES", encrypt);
        if (cipher == null)
            return;

        try (FileInputStream inputStream = new FileInputStream(src);
             FileOutputStream outputStream = new FileOutputStream(dest)) {
            crypt(inputStream, outputStream, cipher);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("done!");
    }

    private static Cipher getCipher(byte[] key, String algorithm, boolean isEncrypt) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, algorithm);
            Cipher cipher = Cipher.getInstance(algorithm);
            SecureRandom random = new SecureRandom();
            cipher.init(isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, keySpec, random);
            return cipher;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void crypt(InputStream inputStream, OutputStream outputStream, Cipher cipher) throws Exception {
        int blockSize = cipher.getBlockSize();
        int outSize = cipher.getOutputSize(blockSize);
//        System.out.println("blockSize " + blockSize + ", outSize " + outSize);
        byte[] inBytes = new byte[blockSize];
        byte[] outBytes = new byte[outSize];
        while (true) {
            int n = inputStream.read(inBytes);
            if (n == blockSize) {
                int m = cipher.update(inBytes, 0, n, outBytes);
//                System.out.println("update " + m);
                outputStream.write(outBytes, 0, m);
                continue;
            }
            if (n > 0) {
                outBytes = cipher.doFinal(inBytes, 0, n);
            } else {
                outBytes = cipher.doFinal();
            }
//            System.out.println("doFinal " + outBytes.length);
            outputStream.write(outBytes);
            break;
        }
    }
}
