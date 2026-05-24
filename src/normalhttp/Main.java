package normalhttp;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || !args[0].equals("generate")) {
            System.err.println("Usage: normalhttp generate --examples <dir> --out <dir>");
            System.exit(1);
        }

        String examplesDir = "examples";
        String outDir      = "generated/src";

        for (int i = 1; i < args.length - 1; i++) {
            if (args[i].equals("--examples")) examplesDir = args[++i];
            else if (args[i].equals("--out"))  outDir      = args[++i];
        }

        Path exPath = Paths.get(examplesDir);
        Path outPath = Paths.get(outDir);

        if (!Files.isDirectory(exPath)) {
            System.err.println("Examples directory not found: " + exPath);
            System.exit(1);
        }
        Files.createDirectories(outPath);

        List<HttpExample> examples = new ArrayList<>();
        try (var stream = Files.walk(exPath)) {
            stream.filter(p -> p.toString().endsWith(".http"))
                  .sorted()
                  .forEach(p -> {
                      try { examples.addAll(HttpExampleParser.parseFile(p)); }
                      catch (IOException e) { throw new RuntimeException(e); }
                  });
        }

        if (examples.isEmpty()) {
            System.err.println("No .http files found in " + exPath);
            System.exit(1);
        }

        System.out.println("Parsed " + examples.size() + " example(s).");

        Path serverFile = outPath.resolve("GeneratedServer.java");
        Path clientFile = outPath.resolve("GeneratedClient.java");

        Files.writeString(serverFile, ServerGenerator.generate(examples));
        Files.writeString(clientFile, ClientGenerator.generate(examples));

        System.out.println("Wrote " + serverFile);
        System.out.println("Wrote " + clientFile);
    }
}
