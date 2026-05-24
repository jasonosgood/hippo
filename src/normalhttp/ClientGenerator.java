package normalhttp;

import java.util.List;

public class ClientGenerator {

    public static String generate(List<HttpExample> examples) {
        StringBuilder sb = new StringBuilder();
        sb.append("import java.net.URI;\n");
        sb.append("import java.net.http.*;\n");
        sb.append("import java.net.http.HttpRequest.BodyPublishers;\n\n");
        sb.append("public class GeneratedClient {\n\n");
        sb.append("    static final HttpClient HTTP = HttpClient.newHttpClient();\n");
        sb.append("    static int failures = 0;\n\n");
        sb.append("    public static void main(String[] args) throws Exception {\n");
        sb.append("        String base = args.length > 0 ? args[0].replaceAll(\"/+$\", \"\") : \"http://localhost:8080\";\n");

        for (HttpExample ex : examples) {
            sb.append("        check_").append(sanitize(ex.sourceName)).append("(base);\n");
        }

        sb.append("        if (failures == 0) System.out.println(\"All checks passed.\");\n");
        sb.append("        else { System.err.println(failures + \" check(s) failed.\"); System.exit(1); }\n");
        sb.append("    }\n\n");

        for (HttpExample ex : examples) {
            String handlerName = "check_" + sanitize(ex.sourceName);
            sb.append("    static void ").append(handlerName).append("(String base) throws Exception {\n");

            // Build request
            String bodyLiteral = escape(null, ex.request.body);
            if (ex.request.body.isEmpty()) {
                sb.append("        var req = HttpRequest.newBuilder()\n");
                sb.append("            .uri(URI.create(base + \"").append(ex.request.path).append("\"))\n");
                sb.append("            .method(\"").append(ex.request.method).append("\", BodyPublishers.noBody())\n");
            } else {
                sb.append("        var req = HttpRequest.newBuilder()\n");
                sb.append("            .uri(URI.create(base + \"").append(ex.request.path).append("\"))\n");
                sb.append("            .method(\"").append(ex.request.method)
                  .append("\", BodyPublishers.ofString(\"").append(bodyLiteral).append("\"))\n");
            }
            for (var h : ex.request.headers.entrySet()) {
                if (h.getKey().equalsIgnoreCase("Host")) continue; // set by HttpClient
                sb.append("            .header(\"").append(h.getKey()).append("\", \"")
                  .append(h.getValue()).append("\")\n");
            }
            sb.append("            .build();\n");

            sb.append("        var res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());\n");

            // Assertions
            sb.append("        assertStatus(\"").append(ex.sourceName).append("\", ")
              .append(ex.response.status).append(", res.statusCode());\n");

            if (!ex.response.body.isEmpty()) {
                sb.append("        assertBody(\"").append(ex.sourceName).append("\", \"")
                  .append(bodyLiteral(ex.response.body)).append("\", res.body());\n");
            }

            sb.append("    }\n\n");
        }

        // Helper methods
        sb.append("    static void assertStatus(String name, int expected, int actual) {\n");
        sb.append("        if (expected != actual) {\n");
        sb.append("            System.err.println(\"FAIL \" + name + \": expected status \" + expected + \" got \" + actual);\n");
        sb.append("            failures++;\n");
        sb.append("        } else System.out.println(\"PASS \" + name + \" status \" + actual);\n");
        sb.append("    }\n\n");
        sb.append("    static void assertBody(String name, String expected, String actual) {\n");
        sb.append("        if (!expected.trim().equals(actual.trim())) {\n");
        sb.append("            System.err.println(\"FAIL \" + name + \": body mismatch\");\n");
        sb.append("            System.err.println(\"  expected: \" + expected);\n");
        sb.append("            System.err.println(\"  actual  : \" + actual);\n");
        sb.append("            failures++;\n");
        sb.append("        } else System.out.println(\"PASS \" + name + \" body\");\n");
        sb.append("    }\n");
        sb.append("}\n");

        return sb.toString();
    }

    private static String sanitize(String s) {
        return s.replaceAll("[^a-zA-Z0-9]", "_");
    }

    /** Escape for a Java string literal. */
    private static String escape(StringBuilder ignore, String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    private static String bodyLiteral(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
