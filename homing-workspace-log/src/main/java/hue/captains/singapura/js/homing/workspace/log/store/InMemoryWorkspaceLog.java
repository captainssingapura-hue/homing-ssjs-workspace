package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.workspace.events.contract.EventSeq;
import hue.captains.singapura.js.homing.workspace.log.LogHeader;
import hue.captains.singapura.js.homing.workspace.log.LoggedEvent;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceEvent;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * One workspace's log, in Java, kept in memory: the same store the browser
 * keeps in IndexedDB. Appending numbers an event from one and stamps it to the
 * millisecond; a file read in is taken whole, its numbers and times as they
 * were, as long as they climb; and what it holds is written out as the same
 * file the browser exports.
 */
public final class InMemoryWorkspaceLog {

    private final LogHeader header;
    private final Clock clock;
    private final List<LoggedEvent> events = new ArrayList<>();

    public InMemoryWorkspaceLog(LogHeader header) { this(header, Clock.systemUTC()); }

    public InMemoryWorkspaceLog(LogHeader header, Clock clock) {
        this.header = Objects.requireNonNull(header, "header");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /** A log holding what a file holds, refused at the first event out of order. */
    public static InMemoryWorkspaceLog of(WorkspaceLogFile file) {
        var log = new InMemoryWorkspaceLog(file.header());
        for (int i = 0; i < file.events().size(); i++) {
            try { log.restore(file.events().get(i)); }
            catch (IllegalArgumentException e) { throw new WorkspaceLogFile.Refused(i + 2, e.getMessage()); }
        }
        return log;
    }

    public LogHeader header() { return header; }

    public List<LoggedEvent> events() { return List.copyOf(events); }

    /** The next event, numbered after the last and stamped now. */
    public LoggedEvent append(WorkspaceEvent event) {
        Objects.requireNonNull(event, "event");
        long next = events.isEmpty() ? 1 : events.get(events.size() - 1).seq().value() + 1;
        var logged = new LoggedEvent(EventSeq.of(next), Instant.ofEpochMilli(clock.millis()), event);
        events.add(logged);
        return logged;
    }

    /** An event as it was logged elsewhere: its number must come after the last one's. */
    public void restore(LoggedEvent logged) {
        Objects.requireNonNull(logged, "logged");
        if (!events.isEmpty()) {
            long last = events.get(events.size() - 1).seq().value();
            if (logged.seq().value() <= last) {
                throw new IllegalArgumentException("seq " + logged.seq().value() + " does not come after " + last);
            }
        }
        events.add(logged);
    }

    /** The log as a file. */
    public WorkspaceLogFile file() { return new WorkspaceLogFile(header, events); }
}
