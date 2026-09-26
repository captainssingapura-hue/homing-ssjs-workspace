package hue.captains.singapura.js.homing.workspace.codecs.log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Build-time entry point: the workspace log's Java codecs — one class per
 * manifest type, {@code <Type>Codec}, a {@code TransformationFunctions<Type,
 * Json>} — written as Java source under the directory given, in the codec
 * package's layout. Run by the exec plugin of the output-only module that
 * compiles them, at generate-sources; the source never lives in {@code src/}.
 */
public final class WorkspaceLogJavaGen {

    private WorkspaceLogJavaGen() {}

    public static void main(String[] args) throws IOException {
        if (args.length != 1) throw new IllegalArgumentException("Usage: WorkspaceLogJavaGen <output-source-directory>");
        Path dir = Path.of(args[0]).resolve(LogShapes.JAVA_PACKAGE.replace('.', '/'));
        Files.createDirectories(dir);
        var sources = generate();
        for (var s : sources.entrySet()) Files.writeString(dir.resolve(s.getKey()), s.getValue(), StandardCharsets.UTF_8);
        System.out.println("[WorkspaceLogJavaGen] wrote " + sources.size() + " codecs to " + dir);
    }

    /** File name to source, one per manifest type. */
    public static Map<String, String> generate() {
        WorkspaceLogManifest.check(WorkspaceLogManifest.ENTRIES);
        var out = new LinkedHashMap<String, String>();
        for (var e : WorkspaceLogManifest.ENTRIES) {
            out.put(e.type().getSimpleName() + "Codec.java", e.javaFunctions().generate(e.definition()));
        }
        return out;
    }
}
