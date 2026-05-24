import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;

public class GeneratedClient {

    static final HttpClient HTTP = HttpClient.newHttpClient();
    static int failures = 0;

    public static void main(String[] args) throws Exception {
        String base = args.length > 0 ? args[0].replaceAll("/+$", "") : "http://localhost:8080";
        check_create_item(base);
        check_get_user(base);
        check_get_user_not_found(base);
        if (failures == 0) System.out.println("All checks passed.");
        else { System.err.println(failures + " check(s) failed."); System.exit(1); }
    }

    static void check_create_item(String base) throws Exception {
        var req = HttpRequest.newBuilder()
            .uri(URI.create(base + "/items"))
            .method("POST", BodyPublishers.ofString("{\"name\": \"widget\", \"price\": 9.99}"))
            .header("Content-Type", "application/json")
            .build();
        var res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        assertStatus("create_item", 201, res.statusCode());
        assertBody("create_item", "{\"id\": 1, \"name\": \"widget\", \"price\": 9.99}", res.body());
    }

    static void check_get_user(String base) throws Exception {
        var req = HttpRequest.newBuilder()
            .uri(URI.create(base + "/users/42"))
            .method("GET", BodyPublishers.noBody())
            .header("Accept", "application/json")
            .build();
        var res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        assertStatus("get_user", 200, res.statusCode());
        assertBody("get_user", "{\"id\": 42, \"name\": \"Alice\"}", res.body());
    }

    static void check_get_user_not_found(String base) throws Exception {
        var req = HttpRequest.newBuilder()
            .uri(URI.create(base + "/users/999"))
            .method("GET", BodyPublishers.noBody())
            .build();
        var res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        assertStatus("get_user_not_found", 404, res.statusCode());
        assertBody("get_user_not_found", "{\"error\": \"user not found\"}", res.body());
    }

    static void assertStatus(String name, int expected, int actual) {
        if (expected != actual) {
            System.err.println("FAIL " + name + ": expected status " + expected + " got " + actual);
            failures++;
        } else System.out.println("PASS " + name + " status " + actual);
    }

    static void assertBody(String name, String expected, String actual) {
        if (!expected.trim().equals(actual.trim())) {
            System.err.println("FAIL " + name + ": body mismatch");
            System.err.println("  expected: " + expected);
            System.err.println("  actual  : " + actual);
            failures++;
        } else System.out.println("PASS " + name + " body");
    }
}
