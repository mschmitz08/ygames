import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public final class LdictTool {
    private LdictTool() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            printUsage();
            System.exit(1);
        }

        String command = args[0];
        if ("export".equalsIgnoreCase(command)) {
            exportLdict(args[1], args[2]);
            return;
        }
        if ("build".equalsIgnoreCase(command)) {
            buildLdict(args[1], args[2]);
            return;
        }

        printUsage();
        System.exit(1);
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java LdictTool export <input.ldict> <output.tsv>");
        System.out.println("  java LdictTool build <input.tsv> <output.ldict>");
    }

    private static void exportLdict(String inputPath, String outputPath) throws IOException {
        TreeMap<Integer, String> entries = new TreeMap<Integer, String>();
        DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(inputPath)));
        try {
            while (true) {
                try {
                    short key = input.readShort();
                    String value = input.readUTF();
                    if (key >= 0)
                        entries.put(Integer.valueOf(key & 0xffff), value);
                }
                catch (EOFException eof) {
                    break;
                }
            }
        }
        finally {
            input.close();
        }

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath), StandardCharsets.UTF_8));
        try {
            writer.write("# key_hex\tkey_dec\ttext");
            writer.newLine();
            for (Map.Entry<Integer, String> entry : entries.entrySet()) {
                int key = entry.getKey().intValue();
                writer.write(String.format("0x%04x\t%d\t%s", Integer.valueOf(key), Integer.valueOf(key), escape(entry.getValue())));
                writer.newLine();
            }
        }
        finally {
            writer.close();
        }
    }

    private static void buildLdict(String inputPath, String outputPath) throws IOException {
        TreeMap<Integer, String> entries = new TreeMap<Integer, String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputPath), StandardCharsets.UTF_8));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() > 0 && line.charAt(0) == '\ufeff')
                    line = line.substring(1);
                if (line.length() == 0 || line.startsWith("#"))
                    continue;
                String[] parts = line.split("\t", 3);
                if (parts.length < 3)
                    continue;
                if ("key_hex".equalsIgnoreCase(parts[0].trim())
                        || "key_dec".equalsIgnoreCase(parts[1].trim()))
                    continue;
                int key = parseKey(parts[0], parts[1]);
                entries.put(Integer.valueOf(key), unescape(parts[2]));
            }
        }
        finally {
            reader.close();
        }

        DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputPath)));
        try {
            for (Map.Entry<Integer, String> entry : entries.entrySet()) {
                output.writeShort(entry.getKey().intValue());
                output.writeUTF(entry.getValue());
            }
        }
        finally {
            output.close();
        }
    }

    private static int parseKey(String hexPart, String decPart) {
        String normalizedHex = hexPart == null ? "" : hexPart.trim().toLowerCase();
        if (normalizedHex.startsWith("0x"))
            return Integer.parseInt(normalizedHex.substring(2), 16);
        return Integer.parseInt(decPart.trim());
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\t", "\\t").replace("\r", "\\r").replace("\n", "\\n");
    }

    private static String unescape(String value) {
        StringBuilder result = new StringBuilder();
        boolean escaping = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!escaping) {
                if (c == '\\') {
                    escaping = true;
                } else {
                    result.append(c);
                }
                continue;
            }

            if (c == 'n')
                result.append('\n');
            else if (c == 'r')
                result.append('\r');
            else if (c == 't')
                result.append('\t');
            else
                result.append(c);
            escaping = false;
        }
        if (escaping)
            result.append('\\');
        return result.toString();
    }
}
