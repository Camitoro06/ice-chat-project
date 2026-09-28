module ChatApp {
    // Estructura que representa un mensaje del chat.
    struct ChatMessage {
        long id;
        string sender;
        string text;
        string timestamp;
    };

    // Secuencias tipadas compartidas por cliente y servidor.
    sequence<ChatMessage> MessageSeq;
    sequence<string> UserSeq;

    // Excepción de negocio para validaciones del servicio.
    exception ChatException {
        string reason;
    };

    // Contrato remoto del chat.
    interface ChatRoom {
        void login(string nickname) throws ChatException;
        void postMessage(string nickname, string message) throws ChatException;
        idempotent MessageSeq getPendingMessages(string nickname, long lastMessageId);
        idempotent UserSeq getOnlineUsers();
        void logout(string nickname);
    };
};
