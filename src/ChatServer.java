import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServer {

    public static List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    public static List<String> messageHistory = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        int port = 5000;

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Сервер запущен: " + port);

            while (true) {
                Socket clientSocket = server.accept();
                System.out.println("Новый пользователь подключился");

                ClientHandler client = new ClientHandler(clientSocket);
                client.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public static ClientHandler findClientByName(String name) {
        for (ClientHandler client : clients) {
            if (client.getNickname().equals(name)) {
                return client;
            }
        }
        return null;
    }

    public static String getUsers() {
        StringBuilder users = new StringBuilder("Онлайн: ");
        for (ClientHandler client : clients) {
            users.append(client.getNickname()).append(" ");
        }
        return users.toString();
    }

    public static void log(String message) {
        try (FileWriter fw = new FileWriter("chat.log", true)) {
            fw.write(message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean nicknameExists(String nickname) {
        for (ClientHandler client : clients) {
            if (client.getNickname() != null &&
                    client.getNickname().equalsIgnoreCase(nickname)) {
                return true;
            }
        }
        return false;
    }
}