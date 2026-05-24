import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;

public class GeneratedServer {

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/items", exchange -> handle_items(exchange));
        server.createContext("/users/42", exchange -> handle_users_42(exchange));
        server.createContext("/users/999", exchange -> handle_users_999(exchange));
        server.start();
        System.out.println("Server listening on port " + port);
    }

    static void handle_items(HttpExchange x) throws java.io.IOException {
        String method = x.getRequestMethod().toUpperCase();
        if (method.equals("POST")) {
            x.getResponseHeaders().set("Content-Type", "application/json");
            byte[] body = "{\"id\": 1, \"name\": \"widget\", \"price\": 9.99}".getBytes();
            x.sendResponseHeaders(201, body.length);
            try (OutputStream os = x.getResponseBody()) { os.write(body); }
        } else {
            x.sendResponseHeaders(405, -1);
        }
    }

    static void handle_users_42(HttpExchange x) throws java.io.IOException {
        String method = x.getRequestMethod().toUpperCase();
        if (method.equals("GET")) {
            x.getResponseHeaders().set("Content-Type", "application/json");
            byte[] body = "{\"id\": 42, \"name\": \"Alice\"}".getBytes();
            x.sendResponseHeaders(200, body.length);
            try (OutputStream os = x.getResponseBody()) { os.write(body); }
        } else {
            x.sendResponseHeaders(405, -1);
        }
    }

    static void handle_users_999(HttpExchange x) throws java.io.IOException {
        String method = x.getRequestMethod().toUpperCase();
        if (method.equals("GET")) {
            x.getResponseHeaders().set("Content-Type", "application/json");
            byte[] body = "{\"error\": \"user not found\"}".getBytes();
            x.sendResponseHeaders(404, body.length);
            try (OutputStream os = x.getResponseBody()) { os.write(body); }
        } else {
            x.sendResponseHeaders(405, -1);
        }
    }

}
