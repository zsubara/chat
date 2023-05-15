import server.ChatServer;

import java.io.IOException;

import static java.lang.System.exit;

public class Main {

    public static void main(String[] args) {

        ChatServer server = new ChatServer("localhost", 8080);

        server.run();
        waitUntilKeypressed();
        server.terminate();
        exit(0);
    }

    private static void waitUntilKeypressed() {
        try {
            System.in.read();
            while (System.in.available() > 0) {
                System.in.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
