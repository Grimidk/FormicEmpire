import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapCoordsCodegen {

    public static void main(String[] args) throws IOException {
        System.out.println("[Codegen] Reading upgrade_positions.json...");
        
        String os = System.getProperty("os.name", "").toLowerCase();
        String home = System.getProperty("user.home", ".");
        Path savesDir;
        if (os.contains("mac")) {
            savesDir = Path.of(home, "Library/Application Support", "GrimIDK", "FormicEmpire", "saves");
        } else if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            savesDir = Path.of(appData != null ? appData : home + "/AppData/Roaming", "GrimIDK", "FormicEmpire", "saves");
        } else {
            savesDir = Path.of(home, ".local/share", "GrimIDK", "FormicEmpire", "saves");
        }
        
        Path positionsFile = savesDir.resolve("upgrade_positions.json");
        if (!Files.exists(positionsFile)) {
            System.err.println("Error: upgrade_positions.json not found at " + positionsFile + ". Make sure you saved your map layout in the game first!");
            System.exit(1);
        }

        Map<String, int[]> coords = new HashMap<>();
        String json = Files.readString(positionsFile, StandardCharsets.UTF_8);
        
        Pattern p = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\{\\s*\"defaultX\"\\s*:\\s*[-]?\\d+\\s*,\\s*\"defaultY\"\\s*:\\s*[-]?\\d+\\s*,\\s*\"setX\"\\s*:\\s*([-]?\\d+)\\s*,\\s*\"setY\"\\s*:\\s*([-]?\\d+)\\s*\\}");
        Matcher m = p.matcher(json);
        while (m.find()) {
            String name = m.group(1);
            int gx = Integer.parseInt(m.group(2));
            int gy = Integer.parseInt(m.group(3));
            coords.put(name, new int[]{gx, gy});
        }
        
        System.out.println("[Codegen] Found " + coords.size() + " coordinate entries.");
        System.out.println("[Codegen] Patching GameUnlocks.java...");

        Path javaFile = Path.of("src/main/java/com/grimidk/formicempire/classes/infrasctructure/registries/GameUnlocks.java");
        List<String> javaLines = Files.readAllLines(javaFile, StandardCharsets.UTF_8);

        Pattern pattern = Pattern.compile("public static final Upgrade ([A-Z0-9_]+)\\s*=\\s*new Upgrade\\(([^;]+)\\);");
        Pattern keyPattern = Pattern.compile("\"([A-Z0-9_]+)\"");

        int modified = 0;
        for (int i = 0; i < javaLines.size(); i++) {
            String line = javaLines.get(i);
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String varName = matcher.group(1);
                String argsStr = matcher.group(2);

                Matcher keyMatcher = keyPattern.matcher(argsStr);
                if (keyMatcher.find()) {
                    String nameKey = keyMatcher.group(1);
                    if (coords.containsKey(nameKey)) {
                        int[] point = coords.get(nameKey);
                        int lastComma1 = argsStr.lastIndexOf(',');
                        if (lastComma1 != -1) {
                            int lastComma2 = argsStr.lastIndexOf(',', lastComma1 - 1);
                            if (lastComma2 != -1) {
                                String prefix = argsStr.substring(0, lastComma2);
                                String newArgs = prefix + ", " + point[0] + ", " + point[1];
                                String newLine = line.replace(argsStr, newArgs);
                                javaLines.set(i, newLine);
                                modified++;
                            }
                        }
                    }
                }
            }
        }

        Files.write(javaFile, javaLines, StandardCharsets.UTF_8);
        System.out.println("[Codegen] Successfully updated " + modified + " upgrades in GameUnlocks.java!");
        System.out.println("[Codegen] You can now recompile the game.");
    }
}
