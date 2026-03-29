import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {

    public static void main(String[] args) {

        String host = "localhost";
        int port = 5000;

        try {
            Socket socket = new Socket(host, port);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true);

            Scanner scanner = new Scanner(System.in);

            // Поток для получения сообщений
            new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        System.out.println(response);
                    }
                } catch (IOException e) {
                    System.out.println("Отключен от сервера");
                }
            }).start();

            // Отправка сообщений
            while (true) {
                String message = scanner.nextLine();
                out.println(message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}