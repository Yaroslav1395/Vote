package Server;

import javax.crypto.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


public class Encryption {
    public static SecretKey key = makeKey();
    /**
     * Метод необходим для генерации случайного ключа
     * @return - возвращает ключ
     */
    public static SecretKey makeKey(){
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод шифрует строку, преобразуя ее в массив байт, после чего массив шифрованных байт преобразуется
     * в строку с помощью класса Base64
     * @param forEncryption - строка для шифрования
     * @param key - ключ для шифрования
     * @return - вернет шифрованную строку
     */
    public static String encrypt(String forEncryption, SecretKey key){
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte [] encodedBytes = cipher.doFinal(forEncryption.getBytes());
            return Base64.getEncoder().encodeToString(encodedBytes);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * С помощью класса Base64 преобразуем строку в массив шифрованных байт и декодируем
     * используя ключ, после чего возвращаем стоку
     * @param forDecryption - строка для дешифрования
     * @param key - ключ для дешифрования
     * @return - вернет дешифрованную строку
     */
    public static String decrypt(String forDecryption, SecretKey key){
        if(forDecryption == null) return "";
        byte[] encodedBytes = Base64.getDecoder().decode(forDecryption);
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decoded = cipher.doFinal(encodedBytes);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (NoSuchPaddingException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException |
                 IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }
}
