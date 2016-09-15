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
        if (args == null || args.length != 2 ||
                (!args[1].equals("e") && !args[1].equals("d"))) {
            System.err.println("Error! usage: java SimpleEncrypt file e[encrypt]|d[decrypt]");
            return;
        }

        File file = new File(args[0]);
        if (!file.exists() || !file.isFile()) {
            System.err.println("file not exist");
            return;
        }

        boolean encrypt = args[1].equals("e");
        if (encrypt) {
            String content = encrypt(file);
            if (content == null || content.isEmpty())
                return;
            try (FileWriter writer = new FileWriter(file)){
                writer.write(content);
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            byte[] buffer = decrypt(file);
            if (buffer == null || buffer.length == 0)
                return;
            try (FileWriter writer = new FileWriter(file)){
                writer.write(new String(buffer));
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("done!");
    }

    private static Cipher getCipher(byte[] key, String algorithm, boolean isEncrypt) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, algorithm);
            Cipher cipher = Cipher.getInstance("AES");
            SecureRandom random = new SecureRandom();
            cipher.init(isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, keySpec, random);
            return cipher;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String encrypt(File file) {
        if (file == null || !file.exists() || !file.isFile())
            return null;
        Cipher cipher = getCipher(KEY, "AES", true);
        if (cipher == null)
            return null;

        try (FileInputStream inputStream = new FileInputStream(file)){
            byte[] buffer = new byte[1024];
            int n;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(buffer.length);
            while ((n = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, n);
            }
            inputStream.close();
            outputStream.close();
            buffer = outputStream.toByteArray();
            buffer = cipher.doFinal(buffer);
            if (buffer != null && buffer.length > 0) {
                return new BASE64Encoder().encode(buffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] decrypt(File file) {
        if (file == null || !file.exists() || !file.isFile())
            return null;
        Cipher cipher = getCipher(KEY, "AES", false);
        if (cipher == null)
            return null;

        try (FileInputStream inputStream = new FileInputStream(file)){
            byte[] buffer = new BASE64Decoder().decodeBuffer(inputStream);
            inputStream.close();
            if (buffer == null || buffer.length == 0)
                return null;

            return cipher.doFinal(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
