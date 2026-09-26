package hue.captains.singapura.js.homing.workspace.log;


import java.time.Instant;
import java.util.Objects;

/**
 * One line of a workspace log: an event, where it falls in the log and when it
 * was written. The sequence is the store's — climbing, never reused —
 * and the time is to the millisecond, the precision both languages keep.
 *
 * @param seq   from 1 to 2^53 − 1
 * @param at    whole milliseconds since the epoch, from 0 to 2^53 − 1
 * @param event what happened
 */
public record LoggedEvent(EventSeq seq, Instant at, WorkspaceEvent event) {

    public LoggedEvent {
        Objects.requireNonNull(seq, "LoggedEvent.seq");
        Objects.requireNonNull(at, "LoggedEvent.at");
        Objects.requireNonNull(event, "LoggedEvent.event");
        if (seq.value() < 1 || seq.value() > Scaled.MAX_SAFE) {
            throw new IllegalArgumentException("LoggedEvent.seq " + seq.value() + " — from 1 to 2^53 − 1");
        }
        if (at.getNano() % 1_000_000 != 0) {
            throw new IllegalArgumentException("LoggedEvent.at " + at + " — whole milliseconds only");
        }
        long ms = at.toEpochMilli();
        if (ms < 0 || ms > Scaled.MAX_SAFE) {
            throw new IllegalArgumentException("LoggedEvent.at " + at + " — from the epoch to 2^53 − 1 ms");
        }
    }
}
