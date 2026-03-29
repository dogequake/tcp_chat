import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String nickname;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Запрос ника
            out.println("Введите никнейм:");
            nickname = in.readLine();

            System.out.println(nickname + " присоединился к чату");
            ChatServer.broadcast(nickname + " присоединился к чату", this);

            String message;

            while ((message = in.readLine()) != null) {
                String fullMessage = nickname + ": " + message;
                System.out.println(fullMessage);

                ChatServer.broadcast(fullMessage, this);
            }

        } catch (Exception e) {
            System.out.println("Пользователь отключился: " + nickname);
        } finally {
            ChatServer.removeClient(this);
            ChatServer.broadcast(nickname + " вышел из чата", this);
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}