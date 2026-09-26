package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.workspace.log.LogHeader;
import hue.captains.singapura.js.homing.workspace.log.LoggedEvent;
import hue.captains.singapura.js.homing.workspace.log.codec.LogHeaderCodec;
import hue.captains.singapura.js.homing.workspace.log.codec.LoggedEventCodec;
import hue.captains.singapura.js.homing.workspace.log.json.JsonText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A workspace log file: JSON lines, UTF-8, each ended by a line feed. The first
 * line is the {@link LogHeader}, each after it one {@link LoggedEvent}, written
 * through the generated codecs as {@code JSON.stringify} writes them — so the
 * file JavaScript exports and the file Java writes for the same log are the same
 * bytes.
 *
 * @param header whose log, in what format
 * @param events the events, in the order they are in the file
 */
public record WorkspaceLogFile(LogHeader header, List<LoggedEvent> events) {

    public WorkspaceLogFile {
        Objects.requireNonNull(header, "WorkspaceLogFile.header");
        events = List.copyOf(Objects.requireNonNull(events, "WorkspaceLogFile.events"));
    }

    /** A line the file could not be read at: which, and why. */
    public static final class Refused extends IllegalArgumentException {
        public final int line;
        public Refused(int line, String why) {
            super("line " + line + ": " + why);
            this.line = line;
        }
    }

    /** The file's text. */
    public String write() {
        var sb = new StringBuilder();
        sb.append(JsonText.write(LogHeaderCodec.INSTANCE.transformTo(header))).append('\n');
        for (LoggedEvent e : events) sb.append(line(e)).append('\n');
        return sb.toString();
    }

    /** One event's line, without its line feed. */
    public static String line(LoggedEvent e) {
        return JsonText.write(LoggedEventCodec.INSTANCE.transformTo(e));
    }

    /**
     * Reads a file's text: every line decoded into its Java object, or refused
     * naming the line. How it was written is not checked here — write it back
     * and compare for that.
     */
    public static WorkspaceLogFile read(String text) {
        Objects.requireNonNull(text, "text");
        if (text.isEmpty()) throw new Refused(1, "the file is empty: a log starts with its header");
        if (!text.endsWith("\n")) throw new Refused(count(text), "the last line is not ended by a line feed");
        String[] lines = text.substring(0, text.length() - 1).split("\n", -1);
        LogHeader header = decode(1, lines[0], true);
        var events = new ArrayList<LoggedEvent>();
        for (int i = 1; i < lines.length; i++) events.add(decode(i + 1, lines[i], false));
        return new WorkspaceLogFile(header, events);
    }

    @SuppressWarnings("unchecked")
    private static <T> T decode(int n, String line, boolean header) {
        if (line.indexOf('\r') >= 0) throw new Refused(n, "a carriage return: lines end with a line feed alone");
        if (line.isEmpty()) throw new Refused(n, "an empty line");
        try {
            var json = JsonText.parse(line);
            return (T) (header ? LogHeaderCodec.INSTANCE.transformFrom(json) : LoggedEventCodec.INSTANCE.transformFrom(json));
        } catch (IllegalArgumentException e) {
            throw new Refused(n, e.getMessage());
        }
    }

    private static int count(String text) {
        int n = 1;
        for (int i = 0; i < text.length(); i++) if (text.charAt(i) == '\n') n++;
        return n;
    }
}
