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

            ChatServer.log(nickname + " присоединился к чату");
            ChatServer.broadcast(nickname + " присоединился к чату", this);

            String message;

            while ((message = in.readLine()) != null) {

                // /quit
                if (message.equals("/quit")) {
                    break;
                }

                // /users
                if (message.equals("/users")) {
                    out.println(ChatServer.getUsers());
                    continue;
                }

                // /help
                if (message.equals("/help")) {
                    out.println("Команды:");
                    out.println("/users - список пользователей");
                    out.println("/msg <ник> <текст> - личное сообщение");
                    out.println("/quit - выход");
                    continue;
                }

                // /msg
                if (message.startsWith("/msg ")) {
                    String[] parts = message.split(" ", 3);

                    if (parts.length >= 3) {
                        String targetName = parts[1];
                        String privateMessage = parts[2];

                        ClientHandler target = ChatServer.findClientByName(targetName);

                        if (target != null) {
                            target.sendMessage("[ЛИЧНО] " + nickname + ": " + privateMessage);
                        } else {
                            out.println("Пользователь не найден");
                        }
                    }
                    continue;
                }

                // обычное сообщение
                String fullMessage = "[" + java.time.LocalTime.now().withNano(0) + "] "
                        + nickname + ": " + message;

                System.out.println(fullMessage);

                ChatServer.broadcast(fullMessage, this);
                ChatServer.log(fullMessage);
            }

        } catch (Exception e) {
            System.out.println("Пользователь отключился: " + nickname);
        } finally {
            ChatServer.removeClient(this);
            ChatServer.log(nickname + " вышел из чата");
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    public String getNickname() {
        return nickname;
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}