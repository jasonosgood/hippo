package normalhttp;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class HttpExampleParser {

    public static List<HttpExample> parseFile(Path file) throws IOException {
        String name = file.getFileName().toString().replaceAll("\\.http$", "");
        String content = Files.readString(file);
        List<HttpExample> examples = new ArrayList<>();

        for (String block : content.split("\n---\n")) {
            examples.add(parseBlock(name, block.trim(), file.toString()));
        }
        return examples;
    }

    private static HttpExample parseBlock(String name, String block, String src) {
        String[] sections = block.split("(?m)^### RESPONSE");
        if (sections.length != 2)
            throw new IllegalArgumentException(src + ": expected '### REQUEST' and '### RESPONSE' sections");

        String reqSection = sections[0].replaceFirst("(?m)^### REQUEST\\s*", "").trim();
        String resSection = sections[1].trim();

        HttpExample ex = new HttpExample();
        ex.sourceName = name;
        ex.request    = parseRequest(reqSection, src);
        ex.response   = parseResponse(resSection, src);
        return ex;
    }

    private static HttpRequest parseRequest(String text, String src) {
        HttpRequest req = new HttpRequest();
        String[] parts = splitHeadBody(text);
        String[] lines = parts[0].split("\n");

        // Request line
        String[] rl = lines[0].trim().split(" ");
        if (rl.length < 2)
            throw new IllegalArgumentException(src + ": malformed request line: " + lines[0]);
        req.method  = rl[0];
        req.path    = rl[1];
        if (rl.length > 2) req.version = rl[2];

        // Headers
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) break;
            int colon = line.indexOf(':');
            if (colon < 0) throw new IllegalArgumentException(src + ": malformed header: " + line);
            req.headers.put(line.substring(0, colon).trim(), line.substring(colon + 1).trim());
        }

        req.body = parts[1];
        return req;
    }

    private static HttpResponse parseResponse(String text, String src) {
        HttpResponse res = new HttpResponse();
        String[] parts = splitHeadBody(text);
        String[] lines = parts[0].split("\n");

        // Status line
        String[] sl = lines[0].trim().split(" ", 3);
        if (sl.length < 2)
            throw new IllegalArgumentException(src + ": malformed status line: " + lines[0]);
        try { res.status = Integer.parseInt(sl[1]); }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException(src + ": non-numeric status code: " + sl[1]);
        }
        res.statusText = sl.length > 2 ? sl[2] : "";

        // Headers
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) break;
            int colon = line.indexOf(':');
            if (colon < 0) throw new IllegalArgumentException(src + ": malformed header: " + line);
            res.headers.put(line.substring(0, colon).trim(), line.substring(colon + 1).trim());
        }

        res.body = parts[1];
        return res;
    }

    /** Splits raw HTTP message into [head, body] on first blank line. */
    private static String[] splitHeadBody(String text) {
        int blank = text.indexOf("\n\n");
        if (blank < 0) return new String[]{ text.trim(), "" };
        return new String[]{ text.substring(0, blank).trim(), text.substring(blank + 2).trim() };
    }
}
