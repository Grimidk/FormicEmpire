import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TranslationSheetCodegen {

    private static final String PACKAGE =
            "com.grimidk.formicempire.classes.infrasctructure.i18n.translations";

    private static final Lang[] LANGS = {
            new Lang("english", "English", "en", "EnglishTranslation"),
            new Lang("spanish", "Spanish", "es", "SpanishTranslation"),
            new Lang("portuguese", "Portuguese", "pt", "PortugueseTranslation"),
            new Lang("french", "French", "fr", "FrenchTranslation"),
    };

    public static void main(String[] args) throws Exception {
        if (args.length < 2 || args.length > 3) {
            System.err.println(
                    "Usage: java TranslationSheetCodegen.java <csvPath> <generatedSourcesRoot> [codegenSourcePath]");
            System.exit(1);
        }
        Path csvPath = Path.of(args[0]).toAbsolutePath().normalize();
        Path outRoot = Path.of(args[1]).toAbsolutePath().normalize();
        Path packageDir = outRoot.resolve(PACKAGE.replace('.', '/'));
        Path codegenSource = args.length == 3
                ? Path.of(args[2]).toAbsolutePath().normalize()
                : null;

        if (!Files.isRegularFile(csvPath)) {
            throw new IllegalArgumentException("CSV not found: " + csvPath);
        }

        List<Path> outputs = new ArrayList<>();
        for (Lang lang : LANGS) {
            outputs.add(packageDir.resolve(lang.className + ".java"));
        }

        if (isUpToDate(csvPath, codegenSource, outputs)) {
            System.out.println("Translation sources up to date — skipping generation");
            return;
        }

        List<String[]> rows = readCsv(Files.readString(csvPath, StandardCharsets.UTF_8));
        if (rows.isEmpty()) {
            throw new IllegalStateException("Empty translation sheet: " + csvPath);
        }

        String[] header = rows.get(0);
        Map<String, Integer> col = new LinkedHashMap<>();
        for (int i = 0; i < header.length; i++) {
            col.put(header[i].trim().toLowerCase(), i);
        }
        requireColumn(col, "tag");
        for (Lang lang : LANGS) {
            requireColumn(col, lang.csvColumn);
        }

        List<String[]> data = rows.subList(1, rows.size());
        Set<String> seen = new HashSet<>();
        List<Row> parsed = new ArrayList<>(data.size());
        for (int i = 0; i < data.size(); i++) {
            String[] cells = data.get(i);
            if (isBlankRow(cells)) {
                continue;
            }
            String tag = cell(cells, col.get("tag")).trim();
            if (tag.isEmpty()) {
                throw new IllegalStateException("Blank tag at CSV data row " + (i + 2));
            }
            if (!seen.add(tag)) {
                throw new IllegalStateException("Duplicate tag in CSV: " + tag);
            }
            String[] values = new String[LANGS.length];
            for (int li = 0; li < LANGS.length; li++) {
                values[li] = cell(cells, col.get(LANGS[li].csvColumn));
            }
            parsed.add(new Row(tag, values));
        }

        Files.createDirectories(packageDir);
        for (int li = 0; li < LANGS.length; li++) {
            Lang lang = LANGS[li];
            String source = renderJava(lang, parsed, li);
            Path out = packageDir.resolve(lang.className + ".java");
            writeIfChanged(out, source);
            System.out.println("Wrote " + out + " (" + parsed.size() + " keys)");
        }
    }

    private static boolean isUpToDate(Path csv, Path codegen, List<Path> outputs) throws IOException {
        for (Path out : outputs) {
            if (!Files.isRegularFile(out)) {
                return false;
            }
        }
        FileTime newestOut = null;
        for (Path out : outputs) {
            FileTime t = Files.getLastModifiedTime(out);
            if (newestOut == null || t.compareTo(newestOut) > 0) {
                newestOut = t;
            }
        }
        if (Files.getLastModifiedTime(csv).compareTo(newestOut) > 0) {
            return false;
        }
        if (codegen != null && Files.isRegularFile(codegen)
                && Files.getLastModifiedTime(codegen).compareTo(newestOut) > 0) {
            return false;
        }
        return true;
    }

    private static void writeIfChanged(Path out, String source) throws IOException {
        byte[] bytes = source.getBytes(StandardCharsets.UTF_8);
        if (Files.isRegularFile(out) && Arrays.equals(Files.readAllBytes(out), bytes)) {
            return;
        }
        Files.write(out, bytes);
    }

    private static String renderJava(Lang lang, List<Row> rows, int langIndex) {
        StringBuilder sb = new StringBuilder(rows.size() * 80);
        sb.append("// Generated from texts/translation_sheet.csv — do not edit. Edit the CSV and recompile.\n");
        sb.append("package ").append(PACKAGE).append(";\n\n");
        sb.append("import java.util.HashMap;\n");
        sb.append("import java.util.Map;\n\n");
        sb.append("public class ").append(lang.className).append(" implements Translation {\n");
        sb.append("    @Override\n");
        sb.append("    public String getLanguageName() {\n");
        sb.append("        return \"").append(lang.displayName).append("\";\n");
        sb.append("    }\n\n");
        sb.append("    @Override\n");
        sb.append("    public Map<String, String> getStrings() {\n");
        sb.append("        Map<String, String> ").append(lang.mapVar).append(" = new HashMap<>();\n");
        for (Row row : rows) {
            sb.append("        ").append(lang.mapVar).append(".put(\"")
                    .append(escapeJava(row.tag)).append("\", \"")
                    .append(escapeJava(row.values[langIndex])).append("\");\n");
        }
        sb.append("        return ").append(lang.mapVar).append(";\n");
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    private static String escapeJava(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> out.append("\\\\");
                case '"' -> out.append("\\\"");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }

    private static void requireColumn(Map<String, Integer> col, String name) {
        if (!col.containsKey(name)) {
            throw new IllegalStateException("CSV missing column: " + name);
        }
    }

    private static String cell(String[] cells, int index) {
        if (index < 0 || index >= cells.length || cells[index] == null) {
            return "";
        }
        return cells[index];
    }

    private static boolean isBlankRow(String[] cells) {
        if (cells == null || cells.length == 0) {
            return true;
        }
        for (String c : cells) {
            if (c != null && !c.isBlank()) {
                return false;
            }
        }
        return true;
    }

    static List<String[]> readCsv(String text) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (Reader reader = new StringReader(text);
             BufferedReader br = new BufferedReader(reader)) {
            List<String> fields = new ArrayList<>();
            StringBuilder field = new StringBuilder();
            boolean inQuotes = false;
            int ch;
            while ((ch = br.read()) != -1) {
                char c = (char) ch;
                if (inQuotes) {
                    if (c == '"') {
                        br.mark(1);
                        int next = br.read();
                        if (next == '"') {
                            field.append('"');
                        } else {
                            inQuotes = false;
                            if (next != -1) {
                                br.reset();
                            }
                        }
                    } else {
                        field.append(c);
                    }
                } else if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(field.toString());
                    field.setLength(0);
                } else if (c == '\n') {
                    fields.add(field.toString());
                    field.setLength(0);
                    rows.add(fields.toArray(String[]::new));
                    fields = new ArrayList<>();
                } else if (c != '\r') {
                    field.append(c);
                }
            }
            if (inQuotes) {
                throw new IOException("Unterminated quoted field in CSV");
            }
            if (field.length() > 0 || !fields.isEmpty()) {
                fields.add(field.toString());
                rows.add(fields.toArray(String[]::new));
            }
        }
        return rows;
    }

    private record Lang(String csvColumn, String displayName, String mapVar, String className) {}

    private record Row(String tag, String[] values) {}
}
