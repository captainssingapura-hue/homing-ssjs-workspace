package hue.captains.singapura.js.homing.workspace.log;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * A stored log the page could not read, set aside rather than cleared: whose
 * log it was, when it was set aside and why, and its lines as they were kept —
 * each a logged event's JSON, written as an export writes one, whether or not
 * it still reads. A page that cannot read its log starts afresh; what it could
 * not read is kept, and can be exported as a log file, where the validator
 * says which line fails, and why.
 *
 * <p>The lines are strings on purpose: a log is set aside because its events
 * do not read, so they are kept as they were, not as they would decode.</p>
 *
 * @param header whose log it was
 * @param at     when it was set aside, whole milliseconds since the epoch
 * @param why    what the page could not read
 * @param lines  the log's lines, after its header, as they were kept
 */
public record SetAsideLog(LogHeader header, Instant at, String why, List<String> lines) {

    public SetAsideLog {
        Objects.requireNonNull(header, "SetAsideLog.header");
        Objects.requireNonNull(at, "SetAsideLog.at");
        Objects.requireNonNull(why, "SetAsideLog.why");
        lines = List.copyOf(Objects.requireNonNull(lines, "SetAsideLog.lines"));
        if (at.getNano() % 1_000_000 != 0) {
            throw new IllegalArgumentException("SetAsideLog.at " + at + " — whole milliseconds only");
        }
        long ms = at.toEpochMilli();
        if (ms < 0 || ms > Scaled.MAX_SAFE) {
            throw new IllegalArgumentException("SetAsideLog.at " + at + " — from the epoch to 2^53 − 1 ms");
        }
    }
}
