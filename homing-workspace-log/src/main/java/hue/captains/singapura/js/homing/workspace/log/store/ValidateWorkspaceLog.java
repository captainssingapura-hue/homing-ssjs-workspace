package hue.captains.singapura.js.homing.workspace.log.store;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The validator: reads a workspace log exported from the browser into Java
 * objects, keeps them in the Java store, writes the store back out and
 * compares the two files byte for byte. A file passes only when Java reads
 * every line of it, the events climb, and what Java writes is exactly what the
 * browser wrote.
 *
 * <pre>
 *   ValidateWorkspaceLog &lt;file&gt;
 *     exit 0   valid
 *     exit 1   not valid: the first line that fails, and why
 *     exit 2   not run: no file named, or it could not be read
 * </pre>
 */
public final class ValidateWorkspaceLog {

    private ValidateWorkspaceLog() {}

    public static final int VALID = 0, INVALID = 1, NOT_RUN = 2;

    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    public static int run(String[] args, PrintStream out, PrintStream err) {
        if (args.length != 1) {
            err.println("usage: ValidateWorkspaceLog <file>");
            return NOT_RUN;
        }
        Path path = Path.of(args[0]);
        byte[] bytes;
        try { bytes = Files.readAllBytes(path); }
        catch (IOException e) {
            err.println("cannot read " + path + ": " + e.getMessage());
            return NOT_RUN;
        }
        String text;
        try {
            text = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            err.println(path + ": not valid UTF-8");
            return INVALID;
        }
        return validate(path.toString(), text, out, err);
    }

    /** The checks on a file's text, said to {@code out} when it passes and to {@code err} when it does not. */
    public static int validate(String name, String text, PrintStream out, PrintStream err) {
        InMemoryWorkspaceLog log;
        try {
            log = InMemoryWorkspaceLog.of(WorkspaceLogFile.read(text));
        } catch (WorkspaceLogFile.Refused e) {
            err.println(name + ": " + e.getMessage());
            return INVALID;
        }
        String again = log.file().write();
        if (!again.equals(text)) {
            String[] was = text.split("\n", -1), now = again.split("\n", -1);
            int n = 0;
            while (n < was.length && n < now.length && was[n].equals(now[n])) n++;
            err.println(name + ": line " + (n + 1) + " is not written as Java writes it");
            err.println("  in the file:     " + (n < was.length ? was[n] : "(nothing)"));
            err.println("  Java writes it:  " + (n < now.length ? now[n] : "(nothing)"));
            return INVALID;
        }
        var events = log.events();
        out.println(name + ": valid - " + events.size() + " event" + (events.size() == 1 ? "" : "s")
                + " of " + log.header().kind() + " " + log.header().workspaceId()
                + (events.isEmpty() ? "" : ", seq " + events.get(0).seq().value() + " to " + events.get(events.size() - 1).seq().value()));
        return VALID;
    }
}
