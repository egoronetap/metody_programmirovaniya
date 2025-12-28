import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Стеганография в ASCII (Курсовая работа) ===");
        System.out.println("Поддерживаемые форматы: .txt, .html, .java, .c");
        System.out.println("Метод: Modification of Trailing Whitespace");
        System.out.println("Шифрование: TEA (Tiny Encryption Algorithm)");
        System.out.println("---------------------------------------------");

        while (true) {
            System.out.println("\nВыберите действие:");
            System.out.println("1. Скрыть файл в контейнер");
            System.out.println("2. Извлечь файл из контейнера");
            System.out.println("3. Выход");
            System.out.print("> ");

            String choice = scanner.nextLine();

            try {
                if (choice.equals("1")) {
                    System.out.print("Введите путь к файлу-КОНТЕЙНЕРУ (напр. book.txt): ");
                    String container = scanner.nextLine();
                    
                    System.out.print("Введите путь к СЕКРЕТНОМУ файлу (напр. secret.txt): ");
                    String secret = scanner.nextLine();
                    
                    System.out.print("Введите имя ВЫХОДНОГО файла (напр. stego.txt): ");
                    String out = scanner.nextLine();
                    
                    System.out.print("Введите ПАРОЛЬ для шифрования: ");
                    String pass = scanner.nextLine();

                    System.out.println("Анализ...");
                    StegoCore.hideData(container, secret, out, pass);
                    System.out.println("[УСПЕХ] Данные скрыты в файле " + out);

                } else if (choice.equals("2")) {
                    System.out.print("Введите путь к файлу со скрытыми данными: ");
                    String container = scanner.nextLine();
                    
                    System.out.print("Куда сохранить извлеченный файл: ");
                    String out = scanner.nextLine();
                    
                    System.out.print("Введите ПАРОЛЬ: ");
                    String pass = scanner.nextLine();

                    StegoCore.revealData(container, out, pass);
                    System.out.println("[УСПЕХ] Файл извлечен и сохранен в " + out);

                } else if (choice.equals("3")) {
                    System.out.println("Выход...");
                    break;
                } else {
                    System.out.println("Неверный ввод.");
                }
            } catch (Exception e) {
                System.out.println("[ОШИБКА] " + e.getMessage());
                // e.printStackTrace(); // Раскомментировать для отладки
            }
        }
        scanner.close();
    }
}
