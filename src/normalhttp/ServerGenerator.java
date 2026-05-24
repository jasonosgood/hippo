package normalhttp;

import java.util.*;

public class ServerGenerator {

    public static String generate(List<HttpExample> examples) {
        StringBuilder sb = new StringBuilder();
        sb.append("import com.sun.net.httpserver.HttpServer;\n");
        sb.append("import com.sun.net.httpserver.HttpExchange;\n");
        sb.append("import java.io.*;\n");
        sb.append("import java.net.InetSocketAddress;\n\n");
        sb.append("public class GeneratedServer {\n\n");
        sb.append("    public static void main(String[] args) throws Exception {\n");
        sb.append("        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;\n");
        sb.append("        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);\n");

        // Group by path so each path gets one context
        Map<String, List<HttpExample>> byPath = new LinkedHashMap<>();
        for (HttpExample ex : examples)
            byPath.computeIfAbsent(ex.request.path, k -> new ArrayList<>()).add(ex);

        for (String path : byPath.keySet()) {
            sb.append("        server.createContext(\"").append(path)
              .append("\", exchange -> handle_").append(sanitize(path))
              .append("(exchange));\n");
        }

        sb.append("        server.start();\n");
        sb.append("        System.out.println(\"Server listening on port \" + port);\n");
        sb.append("    }\n\n");

        // One handler method per path
        for (Map.Entry<String, List<HttpExample>> entry : byPath.entrySet()) {
            String path = entry.getKey();
            List<HttpExample> group = entry.getValue();
            sb.append("    static void handle_").append(sanitize(path))
              .append("(HttpExchange x) throws java.io.IOException {\n");
            sb.append("        String method = x.getRequestMethod().toUpperCase();\n");

            for (int i = 0; i < group.size(); i++) {
                HttpExample ex = group.get(i);
                HttpResponse res = ex.response;
                sb.append(i == 0 ? "        if" : " else if")
                  .append(" (method.equals(\"").append(ex.request.method.toUpperCase()).append("\")) {\n");

                for (Map.Entry<String, String> h : res.headers.entrySet())
                    sb.append("            x.getResponseHeaders().set(\"")
                      .append(h.getKey()).append("\", \"").append(h.getValue()).append("\");\n");

                String body = res.body;
                sb.append("            byte[] body = \"").append(escape(body)).append("\".getBytes();\n");
                sb.append("            x.sendResponseHeaders(").append(res.status).append(", body.length);\n");
                sb.append("            try (OutputStream os = x.getResponseBody()) { os.write(body); }\n");
                sb.append("        }");
            }

            sb.append(" else {\n");
            sb.append("            x.sendResponseHeaders(405, -1);\n");
            sb.append("        }\n");
            sb.append("    }\n\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    private static String sanitize(String path) {
        return path.replaceAll("[^a-zA-Z0-9]", "_").replaceAll("^_+", "");
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
