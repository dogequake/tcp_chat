import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {

    Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String nickname;
    private static final String ADMIN_PASSWORD = "12345";
    private boolean isAdmin = false;

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
            if (nickname.equals("admin")) {

                out.println("Введите пароль администратора:");
                String password = in.readLine();

                if (!password.equals(ADMIN_PASSWORD)) {
                    out.println("Неверный пароль администратора");
                    socket.close();
                    return;
                }

                isAdmin = true;
                out.println("[SYSTEM] Вы вошли как администратор");
            }
            if (ChatServer.nicknameExists(nickname)) {
                out.println("Ник уже занят");
                socket.close();
                return;
            }
            out.println("Последние сообщения:");

            for (String msg : ChatServer.messageHistory) {
                out.println(msg);
            }

            ChatServer.clients.add(this);
            ChatServer.log(nickname + " присоединился к чату");
            ChatServer.broadcast("[SYSTEM] " + nickname + " присоединился к чату", this);

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
                    out.println("/time - время сервера");
                    out.println("/kick <ник> - отключить пользователя (admin)");
                    out.println("/serverinfo - информация о сервере");
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

                // /time
                if (message.equals("/time")) {
                    out.println("Время сервера: " +
                            java.time.LocalDateTime.now().withNano(0));
                    continue;
                }

                // /kick
                if (message.startsWith("/kick ")) {

                    if (!isAdmin) {
                        out.println("Недостаточно прав");
                        continue;
                    }

                    String targetName = message.split(" ", 2)[1];

                    ClientHandler target = ChatServer.findClientByName(targetName);

                    if (target != null) {
                        target.sendMessage("[SYSTEM] Вы были отключены администратором");
                        target.socket.close();

                        ChatServer.broadcast("[ADMIN] " + targetName + " был отключен", this);
                    } else {
                        out.println("Пользователь не найден");
                    }

                    continue;
                }

                // /serverinfo
                if (message.equals("/serverinfo")) {
                    out.println("TCP Chat Server v1.0");
                    out.println("Пользователей онлайн: " + ChatServer.clients.size());
                    continue;
                }

                // обычное сообщение
                String prefix = isAdmin ? "[ADMIN] " : "";

                String fullMessage = "[" + java.time.LocalTime.now().withNano(0) + "] "
                        + prefix + nickname + ": " + message;
                System.out.println(fullMessage);

                ChatServer.broadcast(fullMessage, this);
                ChatServer.log(fullMessage);

                ChatServer.messageHistory.add(fullMessage);

                if (ChatServer.messageHistory.size() > 10) {
                    ChatServer.messageHistory.remove(0);
                }

            }

        } catch (Exception e) {
            System.out.println("Пользователь отключился: " + nickname);
        } finally {
            if (nickname != null) {
                ChatServer.removeClient(this);
                ChatServer.log(nickname + " вышел из чата");
                ChatServer.broadcast("[SYSTEM] " + nickname + " вышел из чата", this);
            }
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