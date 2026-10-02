package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.workspace.log.FoldedState;
import hue.captains.singapura.js.homing.workspace.log.codec.FoldedStateCodec;
import hue.captains.singapura.js.homing.workspace.log.fold.WorkspaceFold;
import hue.captains.singapura.js.homing.workspace.log.json.Json;
import hue.captains.singapura.js.homing.workspace.log.json.JsonText;

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
 * compares the two files byte for byte; then folds the log into the state it
 * means. Given the state the browser folded the same log to, it compares that
 * too, byte for byte. A log passes only when Java reads every line of it, the
 * events climb, what Java writes is exactly what the browser wrote, every event
 * is possible where it falls — and, with a state, when the two folds agree.
 *
 * <pre>
 *   ValidateWorkspaceLog &lt;log&gt; [&lt;state&gt;]
 *     exit 0   valid
 *     exit 1   not valid: the first line that fails, or the first place the states part, and why
 *     exit 2   not run: no file named, or one could not be read
 * </pre>
 */
public final class ValidateWorkspaceLog {

    private ValidateWorkspaceLog() {}

    public static final int VALID = 0, INVALID = 1, NOT_RUN = 2;

    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    public static int run(String[] args, PrintStream out, PrintStream err) {
        if (args.length < 1 || args.length > 2) {
            err.println("usage: ValidateWorkspaceLog <log> [<state>]");
            return NOT_RUN;
        }
        String log, state = null;
        try {
            log = read(Path.of(args[0]));
            if (args.length == 2) state = read(Path.of(args[1]));
        } catch (IOException e) {
            err.println("cannot read: " + e.getMessage());
            return NOT_RUN;
        } catch (NotUtf8 e) {
            err.println(e.getMessage() + ": not valid UTF-8");
            return INVALID;
        }
        return validate(args[0], log, args.length == 2 ? args[1] : null, state, out, err);
    }

    /** A file whose bytes are not UTF-8. */
    private static final class NotUtf8 extends Exception {
        NotUtf8(Path path) { super(path.toString()); }
    }

    private static String read(Path path) throws IOException, NotUtf8 {
        byte[] bytes = Files.readAllBytes(path);
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            throw new NotUtf8(path);
        }
    }

    /** A log's checks alone. */
    public static int validate(String name, String text, PrintStream out, PrintStream err) {
        return validate(name, text, null, null, out, err);
    }

    /** A log's checks, and — given the state the browser folded it to — the two folds compared. */
    public static int validate(String name, String text, String stateName, String stateText, PrintStream out, PrintStream err) {
        InMemoryWorkspaceLog log;
        FoldedState folded;
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
        try {
            folded = WorkspaceFold.fold(log.file());
        } catch (WorkspaceLogFile.Refused e) {
            err.println(name + ": " + e.getMessage());
            return INVALID;
        }
        String said = "";
        if (stateText != null) {
            int code = compare(stateName, stateText, folded, err);
            if (code != VALID) return code;
            said = "; " + stateName + " is the state Java folds it to, byte for byte";
        }
        var events = log.events();
        var s = folded.state();
        out.println(name + ": valid - " + events.size() + " event" + (events.size() == 1 ? "" : "s")
                + " of " + log.header().kind() + " " + log.header().workspaceId()
                + (events.isEmpty() ? "" : ", seq " + events.get(0).seq().value() + " to " + events.get(events.size() - 1).seq().value())
                + "; it folds to " + s.roster().widgets().size() + " widget" + (s.roster().widgets().size() == 1 ? "" : "s")
                + ", the pane showing " + s.pane().shown().map(w -> w.value()).orElse("nothing")
                + ", " + s.grid().regions().size() + " region" + (s.grid().regions().size() == 1 ? "" : "s")
                + ", " + s.grid().floats().size() + " float" + (s.grid().floats().size() == 1 ? "" : "s")
                + ", " + s.grid().tabs().size() + " tab" + (s.grid().tabs().size() == 1 ? "" : "s") + said);
        return VALID;
    }

    private static int compare(String name, String text, FoldedState folded, PrintStream err) {
        Json mine = FoldedStateCodec.INSTANCE.transformTo(folded);
        String java = JsonText.write(mine) + "\n";
        if (java.equals(text)) return VALID;
        if (!text.endsWith("\n") || text.indexOf('\n') != text.length() - 1) {
            err.println(name + ": a state is one line, ended by a line feed");
            return INVALID;
        }
        Json theirs;
        try {
            theirs = JsonText.parse(text.substring(0, text.length() - 1));
            FoldedStateCodec.INSTANCE.transformFrom(theirs);
        } catch (IllegalArgumentException e) {
            err.println(name + ": not a folded state: " + e.getMessage());
            return INVALID;
        }
        String where = JsonDiff.first(theirs, mine, "");
        err.println(name + ": the browser's fold and Java's part at "
                + (where != null ? where + "  (browser / Java)" : "no field: the same values, written otherwise"));
        return INVALID;
    }
}
