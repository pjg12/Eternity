package websocket;

public class TestServer {

    public static void main(String[] args) {

        Roll20WebSocketServer server =
            new Roll20WebSocketServer(8080);

        server.start();

        System.out.println(
            "Listening on ws://localhost:8080");
    }
}