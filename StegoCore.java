import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class StegoCore {

    // Скрытие данных
    public static void hideData(String containerPath, String secretPath, String outPath, String password) throws Exception {
        // 1. Читаем и шифруем секретный файл
        byte[] secretBytes = Files.readAllBytes(new File(secretPath).toPath());
        byte[] encryptedBytes = CryptoUtils.encrypt(secretBytes, password);

        // 2. Формируем битовый поток: [Длина сообщения (32 бита)] + [Само сообщение]
        StringBuilder bitString = new StringBuilder();
        
        // Добавляем длину зашифрованных данных (4 байта = 32 бита)
        int length = encryptedBytes.length;
        for (int i = 31; i >= 0; i--) {
            bitString.append((length >> i) & 1);
        }
        
        // Добавляем сами данные
        for (byte b : encryptedBytes) {
            for (int i = 7; i >= 0; i--) {
                bitString.append((b >> i) & 1);
            }
        }

        // 3. Читаем контейнер
        List<String> lines = Files.readAllLines(new File(containerPath).toPath(), StandardCharsets.US_ASCII);

        // 4. Оценка вместимости
        if (lines.size() < bitString.length()) {
            throw new Exception("Ошибка вместимости! \n" +
                    "Нужно строк: " + bitString.length() + "\n" +
                    "Есть строк: " + lines.size() + "\n" +
                    "Контейнер слишком мал.");
        }

        // 5. Встраиваем данные (Space = 0, Tab = 1)
        try (PrintWriter writer = new PrintWriter(outPath, "US-ASCII")) {
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                
                // Если еще есть биты для записи
                if (i < bitString.length()) {
                    char bit = bitString.charAt(i);
                    if (bit == '0') {
                        line += " ";  // Бит 0 -> Пробел
                    } else {
                        line += "\t"; // Бит 1 -> Табуляция
                    }
                }
                writer.println(line);
            }
        }
    }

    // Извлечение данных
    public static void revealData(String containerPath, String outPath, String password) throws Exception {
        List<String> lines = Files.readAllLines(new File(containerPath).toPath(), StandardCharsets.US_ASCII);
        
        // 1. Сначала читаем первые 32 строки, чтобы узнать длину сообщения
        if (lines.size() < 32) throw new Exception("Файл слишком короткий, нет даже заголовка.");

        int length = 0;
        for (int i = 0; i < 32; i++) {
            char lastChar = getLastChar(lines.get(i));
            int bit = (lastChar == '\t') ? 1 : 0; // Таб = 1, Пробел (или ничего) = 0
            length = (length << 1) | bit;
        }

        // Защита от мусора
        if (length <= 0 || length > lines.size() / 8) { 
             // Это просто проверка на здравый смысл, можно убрать
        }

        // 2. Читаем само сообщение
        int bitsNeeded = length * 8;
        if (lines.size() < 32 + bitsNeeded) throw new Exception("Файл поврежден или не содержит полных данных.");

        byte[] encryptedData = new byte[length];
        
        for (int i = 0; i < length; i++) {
            int currentByte = 0;
            for (int bit = 0; bit < 8; bit++) {
                int lineIndex = 32 + (i * 8) + bit;
                char lastChar = getLastChar(lines.get(lineIndex));
                int val = (lastChar == '\t') ? 1 : 0;
                currentByte = (currentByte << 1) | val;
            }
            encryptedData[i] = (byte) currentByte;
        }

        // 3. Расшифровываем и сохраняем
        byte[] decryptedData = CryptoUtils.decrypt(encryptedData, password);
        Files.write(new File(outPath).toPath(), decryptedData);
    }

    private static char getLastChar(String line) {
        if (line.isEmpty()) return ' '; // Если пустая строка, считаем 0
        return line.charAt(line.length() - 1);
    }
}
