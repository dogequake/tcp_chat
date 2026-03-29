import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServer {

    public static List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        int port = 5000;

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Сервер запущен: " + port);

            while (true) {
                Socket clientSocket = server.accept();
                System.out.println("Новый пользователь подключился");

                ClientHandler client = new ClientHandler(clientSocket);
                clients.add(client);
                client.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
    }
}