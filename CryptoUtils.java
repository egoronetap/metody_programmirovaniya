import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CryptoUtils {

    // Ключевые константы TEA
    private static final int DELTA = 0x9E3779B9;
    private static final int ROUNDS = 32;

    /**
     * Основной метод шифрования данных
     */
    public static byte[] encrypt(byte[] data, String password) {
        int[] key = generateKey(password);
        byte[] paddedData = padData(data); // Дополняем данные до кратного 8 байт
        byte[] result = new byte[paddedData.length];

        // Шифруем блоками по 8 байт (2 int)
        for (int i = 0; i < paddedData.length; i += 8) {
            int v0 = bytesToInt(paddedData, i);
            int v1 = bytesToInt(paddedData, i + 4);
            
            int sum = 0;
            for (int j = 0; j < ROUNDS; j++) {
                sum += DELTA;
                v0 += ((v1 << 4) + key[0]) ^ (v1 + sum) ^ ((v1 >>> 5) + key[1]);
                v1 += ((v0 << 4) + key[2]) ^ (v0 + sum) ^ ((v0 >>> 5) + key[3]);
            }
            
            intToBytes(v0, result, i);
            intToBytes(v1, result, i + 4);
        }
        return result;
    }

    /**
     * Основной метод расшифрования
     */
    public static byte[] decrypt(byte[] data, String password) {
        int[] key = generateKey(password);
        byte[] result = new byte[data.length];

        for (int i = 0; i < data.length; i += 8) {
            int v0 = bytesToInt(data, i);
            int v1 = bytesToInt(data, i + 4);
            
            int sum = DELTA * ROUNDS;
            for (int j = 0; j < ROUNDS; j++) {
                v1 -= ((v0 << 4) + key[2]) ^ (v0 + sum) ^ ((v0 >>> 5) + key[3]);
                v0 -= ((v1 << 4) + key[0]) ^ (v1 + sum) ^ ((v1 >>> 5) + key[1]);
                sum -= DELTA;
            }
            
            intToBytes(v0, result, i);
            intToBytes(v1, result, i + 4);
        }
        return unpadData(result); // Убираем дополнение
    }

    // --- Вспомогательные методы ---

    // Генерация 128-битного ключа из пароля (простое хеширование)
    private static int[] generateKey(String password) {
        byte[] passBytes = password.getBytes(StandardCharsets.US_ASCII);
        int[] key = new int[4];
        for (int i = 0; i < passBytes.length; i++) {
            key[i % 4] = (key[i % 4] << 8) ^ (passBytes[i] & 0xFF);
        }
        return key;
    }

    // Преобразование 4 байт в int
    private static int bytesToInt(byte[] b, int offset) {
        return ((b[offset] & 0xFF) << 24) |
               ((b[offset + 1] & 0xFF) << 16) |
               ((b[offset + 2] & 0xFF) << 8) |
               (b[offset + 3] & 0xFF);
    }

    // Запись int в массив байт
    private static void intToBytes(int val, byte[] b, int offset) {
        b[offset] = (byte) (val >>> 24);
        b[offset + 1] = (byte) (val >>> 16);
        b[offset + 2] = (byte) (val >>> 8);
        b[offset + 3] = (byte) val;
    }

    // PKCS#7 Padding (дополнение данных)
    private static byte[] padData(byte[] data) {
        int paddingLength = 8 - (data.length % 8);
        byte[] padded = Arrays.copyOf(data, data.length + paddingLength);
        for (int i = data.length; i < padded.length; i++) {
            padded[i] = (byte) paddingLength;
        }
        return padded;
    }

    private static byte[] unpadData(byte[] data) {
        int paddingLength = data[data.length - 1] & 0xFF;
        if (paddingLength > 8 || paddingLength <= 0) return data; // Ошибка или нет паддинга
        return Arrays.copyOf(data, data.length - paddingLength);
    }
}
