package com.wuyz.simpleencrypt;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.SecureRandom;

/**
 * Created by wuyz on 9/14/2016.
 *
 */
public class Main {
    private static final byte[] KEY = "eFDe&9(feFDe&9(f".getBytes();

    public static void main(String[] args) {
        if (args == null || args.length != 3 ||
                (!args[0].equals("e") && !args[0].equals("d"))) {
            System.err.println("Error! usage: java SimpleEncrypt e[encrypt]|d[decrypt] src dest");
            return;
        }

        if (args[1].equals(args[2])) {
            System.err.println("src cannot be same as dest");
            return;
        }
        
        File file = new File(args[1]);
        if (!file.exists() || !file.isFile()) {
            System.err.println("file not exist");
            return;
        }

        boolean encrypt = args[0].equals("e");
        Cipher cipher = getCipher(KEY, "AES", encrypt);
        if (cipher == null)
            return;

        try (FileInputStream inputStream = new FileInputStream(args[1]);
             FileOutputStream outputStream = new FileOutputStream(args[2])) {
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
