import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MusicTracksCodegen {
    private static final String MAIN_MENU_ID = "MainMenu";
    private static final Pattern EXISTING_TRACK = Pattern.compile(
            "public\\s+static\\s+final\\s+MusicTrack\\s+(\\w+)\\s*=\\s*track\\(\\s*\"([^\"]+)\"\\s*,\\s*\"((?:\\\\.|[^\"\\\\])*)\"\\s*,\\s*\"((?:\\\\.|[^\"\\\\])*)\"\\s*\\)\\s*;"
    );

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java MusicTracksCodegen.java <musicRoot> <musicTracksJava>");
            System.exit(1);
        }
        Path musicRoot = Path.of(args[0]).toAbsolutePath().normalize();
        Path output = Path.of(args[1]).toAbsolutePath().normalize();
        Path baseDir = musicRoot.resolve("base");

        Map<String, ExistingMeta> existing = parseExisting(output);
        List<TrackSpec> tracks = collect(baseDir, existing);
        ensureMainMenu(tracks, existing);
        tracks.sort(Comparator.comparing((TrackSpec t) -> t.id, String.CASE_INSENSITIVE_ORDER));

        String generated = render(tracks);
        if (Files.isRegularFile(output)) {
            String current = Files.readString(output, StandardCharsets.UTF_8);
            if (current.equals(generated)) {
                System.out.println("MusicTracks up to date — " + tracks.size() + " track(s)");
                return;
            }
        }
        Files.createDirectories(output.getParent());
        Files.writeString(output, generated, StandardCharsets.UTF_8);
        System.out.println("MusicTracks synced — " + tracks.size() + " track(s) -> " + output);
    }

    private static void ensureMainMenu(List<TrackSpec> tracks, Map<String, ExistingMeta> existing) {
        for (TrackSpec track : tracks) {
            if (MAIN_MENU_ID.equals(track.id)) {
                return;
            }
        }
        ExistingMeta meta = existing.get(MAIN_MENU_ID);
        String displayName = meta != null ? meta.displayName : "Main Menu";
        String author = meta != null ? meta.author : "Unknown";
        String fieldName = meta != null ? meta.fieldName : "MAIN_MENU";
        tracks.add(new TrackSpec(fieldName, MAIN_MENU_ID, displayName, author));
    }

    private static List<TrackSpec> collect(Path dir, Map<String, ExistingMeta> existing) throws IOException {
        List<TrackSpec> out = new ArrayList<>();
        if (!Files.isDirectory(dir)) {
            Files.createDirectories(dir);
            return out;
        }
        Set<String> seen = new HashSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.mp3")) {
            for (Path path : stream) {
                if (!Files.isRegularFile(path)) {
                    continue;
                }
                String fileName = path.getFileName().toString();
                String id = stripExtension(fileName);
                if (id.isEmpty() || !seen.add(id)) {
                    continue;
                }
                ExistingMeta meta = existing.get(id);
                String displayName = meta != null ? meta.displayName : displayNameFromId(id);
                String author = meta != null ? meta.author : "Unknown";
                String fieldName = meta != null ? meta.fieldName : fieldNameFor(id);
                out.add(new TrackSpec(fieldName, id, displayName, author));
            }
        }
        return out;
    }

    private static Map<String, ExistingMeta> parseExisting(Path output) throws IOException {
        Map<String, ExistingMeta> map = new HashMap<>();
        if (!Files.isRegularFile(output)) {
            return map;
        }
        String source = Files.readString(output, StandardCharsets.UTF_8);
        Matcher matcher = EXISTING_TRACK.matcher(source);
        while (matcher.find()) {
            String fieldName = matcher.group(1);
            String id = unescapeJava(matcher.group(2));
            String displayName = unescapeJava(matcher.group(3));
            String author = unescapeJava(matcher.group(4));
            map.put(id, new ExistingMeta(fieldName, displayName, author));
        }
        return map;
    }

    private static String render(List<TrackSpec> tracks) {
        StringBuilder sb = new StringBuilder();
        sb.append("package com.grimidk.formicempire.classes.infrasctructure.registries;\n\n");
        sb.append("import com.grimidk.formicempire.classes.infrasctructure.audio.MusicTrack;\n\n");
        sb.append("import java.util.ArrayList;\n");
        sb.append("import java.util.Collections;\n");
        sb.append("import java.util.List;\n\n");
        sb.append("public final class MusicTracks {\n");
        sb.append("    private static final List<MusicTrack> TRACKS = new ArrayList<>();\n\n");
        for (TrackSpec track : tracks) {
            sb.append("    public static final MusicTrack ").append(track.fieldName)
                    .append(" = track(")
                    .append(quote(track.id)).append(", ")
                    .append(quote(track.displayName)).append(", ")
                    .append(quote(track.author)).append(");\n");
        }
        sb.append("\n");
        sb.append("    private MusicTracks() {}\n\n");
        sb.append("    public static List<MusicTrack> getTracks() {\n");
        sb.append("        return Collections.unmodifiableList(TRACKS);\n");
        sb.append("    }\n\n");
        sb.append("    public static List<MusicTrack> getBaseTracks() {\n");
        sb.append("        return getTracks();\n");
        sb.append("    }\n\n");
        sb.append("    public static List<MusicTrack> getPlayableBaseTracks() {\n");
        sb.append("        return filterPlayable(TRACKS, false);\n");
        sb.append("    }\n\n");
        sb.append("    public static List<MusicTrack> getPlayableMenuTracks() {\n");
        sb.append("        List<MusicTrack> out = new ArrayList<>();\n");
        sb.append("        MusicTrack mainMenu = getById(MusicTrack.MAIN_MENU_ID);\n");
        sb.append("        if (mainMenu != null && mainMenu.isResourcePresent()) {\n");
        sb.append("            out.add(mainMenu);\n");
        sb.append("        }\n");
        sb.append("        return Collections.unmodifiableList(out);\n");
        sb.append("    }\n\n");
        sb.append("    public static List<MusicTrack> getPlayableSessionTracks() {\n");
        sb.append("        return filterPlayable(TRACKS, true);\n");
        sb.append("    }\n\n");
        sb.append("    public static MusicTrack getById(String id) {\n");
        sb.append("        if (id == null || id.isBlank()) {\n");
        sb.append("            return null;\n");
        sb.append("        }\n");
        sb.append("        for (MusicTrack track : TRACKS) {\n");
        sb.append("            if (track.getId().equals(id)) {\n");
        sb.append("                return track;\n");
        sb.append("            }\n");
        sb.append("        }\n");
        sb.append("        return null;\n");
        sb.append("    }\n\n");
        sb.append("    private static List<MusicTrack> filterPlayable(List<MusicTrack> source, boolean excludeMainMenu) {\n");
        sb.append("        List<MusicTrack> out = new ArrayList<>();\n");
        sb.append("        for (MusicTrack track : source) {\n");
        sb.append("            if (excludeMainMenu && track.isMainMenuTrack()) {\n");
        sb.append("                continue;\n");
        sb.append("            }\n");
        sb.append("            if (!track.isResourcePresent()) {\n");
        sb.append("                continue;\n");
        sb.append("            }\n");
        sb.append("            out.add(track);\n");
        sb.append("        }\n");
        sb.append("        return Collections.unmodifiableList(out);\n");
        sb.append("    }\n\n");
        sb.append("    private static MusicTrack track(String id, String displayName, String author) {\n");
        sb.append("        return register(new MusicTrack(id, displayName, author));\n");
        sb.append("    }\n\n");
        sb.append("    private static MusicTrack register(MusicTrack track) {\n");
        sb.append("        for (MusicTrack existing : TRACKS) {\n");
        sb.append("            if (existing.getId().equals(track.getId())) {\n");
        sb.append("                throw new IllegalStateException(\"Duplicate music track: \" + track.getId());\n");
        sb.append("            }\n");
        sb.append("        }\n");
        sb.append("        TRACKS.add(track);\n");
        sb.append("        return track;\n");
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    static String fieldNameFor(String id) {
        if (MAIN_MENU_ID.equals(id)) {
            return "MAIN_MENU";
        }
        return toConstant(id);
    }

    static String toConstant(String id) {
        String spaced = id.replace('-', '_').replace(' ', '_');
        spaced = spaced.replaceAll("(?<=[a-z0-9])(?=[A-Z])", "_");
        spaced = spaced.replaceAll("_+", "_");
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < spaced.length(); i++) {
            char c = spaced.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_') {
                out.append(Character.toUpperCase(c));
            } else {
                out.append('_');
            }
        }
        String value = out.toString().replaceAll("_+", "_");
        if (value.startsWith("_")) {
            value = value.substring(1);
        }
        if (value.endsWith("_")) {
            value = value.substring(0, value.length() - 1);
        }
        if (value.isEmpty()) {
            value = "TRACK";
        }
        if (Character.isDigit(value.charAt(0))) {
            value = "TRACK_" + value;
        }
        return value;
    }

    static String displayNameFromId(String id) {
        String trimmed = id.trim().replace('_', ' ').replace('-', ' ');
        String spaced = trimmed.replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ");
        spaced = spaced.replaceAll("(?<=[A-Za-z])(?=\\d)", " ");
        spaced = spaced.replaceAll("(?<=\\d)(?=[A-Za-z])", " ");
        spaced = spaced.replaceAll("\\s+", " ").trim();
        if (spaced.isEmpty()) {
            return id;
        }
        String[] words = spaced.split(" ");
        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                out.append(word.substring(1));
            }
        }
        return out.toString();
    }

    private static String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot <= 0) {
            return fileName;
        }
        return fileName.substring(0, dot);
    }

    private static String quote(String value) {
        return "\"" + escapeJava(value) + "\"";
    }

    private static String escapeJava(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String unescapeJava(String value) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()) {
                char next = value.charAt(++i);
                switch (next) {
                    case 'n' -> out.append('\n');
                    case 'r' -> out.append('\r');
                    case 't' -> out.append('\t');
                    case '"' -> out.append('"');
                    case '\\' -> out.append('\\');
                    default -> out.append(next);
                }
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static final class TrackSpec {
        final String fieldName;
        final String id;
        final String displayName;
        final String author;

        TrackSpec(String fieldName, String id, String displayName, String author) {
            this.fieldName = Objects.requireNonNull(fieldName);
            this.id = Objects.requireNonNull(id);
            this.displayName = Objects.requireNonNull(displayName);
            this.author = Objects.requireNonNull(author);
        }
    }

    private static final class ExistingMeta {
        final String fieldName;
        final String displayName;
        final String author;

        ExistingMeta(String fieldName, String displayName, String author) {
            this.fieldName = fieldName;
            this.displayName = displayName;
            this.author = author;
        }
    }
}
