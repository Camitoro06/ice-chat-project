package chat.client;

import ChatApp.ChatException;
import ChatApp.ChatMessage;
import ChatApp.ChatRoomPrx;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

import java.util.Scanner;

public class ClientMain {
    private static volatile boolean running = true;
    private static volatile long lastReceivedId = 0;

    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            String proxyString = "ChatService:default -h 127.0.0.1 -p 10000";
            ObjectPrx base = communicator.stringToProxy(proxyString);
            ChatRoomPrx chatPrx = ChatRoomPrx.checkedCast(base);

            if (chatPrx == null) {
                System.err.println("[ERROR] No se pudo establecer conexión con el servicio Ice remoto.");
                return;
            }

            Scanner scanner = new Scanner(System.in);
            String nickname = "";

            System.out.println("=== BIENVENIDO AL CHAT DISTRIBUIDO ZEROC ICE ===");
            while (true) {
                System.out.print("Ingrese su nickname: ");
                nickname = scanner.nextLine().trim();
                if (nickname.isEmpty()) {
                    continue;
                }

                try {
                    chatPrx.login(nickname);
                    System.out.println(">>> Autenticado correctamente como '" + nickname + "'.");
                    System.out.println(">>> Comandos disponibles: /users (lista usuarios), /exit (salir)");
                    System.out.println("-------------------------------------------------");
                    break;
                } catch (ChatException ce) {
                    System.out.println("[RECHAZADO POR SERVIDOR] " + ce.reason + " Intente nuevamente.");
                }
            }

            final String activeUser = nickname;

            Thread listenerThread = new Thread(() -> {
                while (running) {
                    try {
                        ChatMessage[] newMessages = chatPrx.getPendingMessages(activeUser, lastReceivedId);
                        for (ChatMessage msg : newMessages) {
                            if (msg.id > lastReceivedId) {
                                lastReceivedId = msg.id;
                            }

                            // No se repite en pantalla el mensaje enviado por el propio usuario.
                            if (!msg.sender.equals(activeUser)) {
                                System.out.println("\n[" + msg.timestamp + "] <" + msg.sender + ">: " + msg.text);
                                System.out.print("> ");
                            }
                        }

                        Thread.sleep(500);
                    } catch (Exception e) {
                        if (running) {
                            System.err.println("\n[AVISO] Se perdió la comunicación con el servidor Ice ("
                                    + e.getClass().getSimpleName() + "): " + e.getMessage());
                            running = false;
                        }
                        break;
                    }
                }
            }, "ice-chat-listener");

            listenerThread.setDaemon(true);
            listenerThread.start();

            while (running) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("/exit")) {
                    running = false;
                    try {
                        chatPrx.logout(activeUser);
                    } catch (Exception ignored) {
                        // La sesión local se cierra incluso si el servidor ya no responde.
                    }
                    System.out.println(">>> Sesión cerrada satisfactoriamente. ¡Hasta pronto!");
                    break;
                } else if (input.equalsIgnoreCase("/users")) {
                    try {
                        String[] users = chatPrx.getOnlineUsers();
                        System.out.println(">>> Usuarios activos (" + users.length + "): " + String.join(", ", users));
                    } catch (Exception e) {
                        System.err.println("[ERROR] No se pudo obtener la lista de usuarios: " + e.getMessage());
                    }
                } else if (!input.isEmpty()) {
                    try {
                        chatPrx.postMessage(activeUser, input);
                    } catch (ChatException ce) {
                        System.err.println("[ERROR ENVÍO] " + ce.reason);
                    } catch (Exception e) {
                        System.err.println("[ERROR RED] " + e.getClass().getSimpleName()
                                + ": " + e.getMessage());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR FATAL] " + e.getMessage());
        }
    }
}
