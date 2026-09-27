package hue.captains.singapura.js.homing.workspace.log;

import java.util.Objects;

/**
 * Who writes a log: one page at a time, so that two pages of one workspace
 * never interleave two histories in one log - each numbering its own tabs,
 * the fold refusing what they make together. The page that holds a log's lock
 * records into it and takes its checkpoints; a page that does not reads it only.
 * Never logged: which page writes is not the workspace's state.
 *
 * @param log  whose lock
 * @param held where it is held, as this page sees it
 */
public record WriteLock(LogKey log, Held held) {

    /** What a lock's name begins with; the log's kind and workspace follow it. */
    public static final String NAME_PREFIX = "homing.workspace.log";

    /** Where a log's lock is held, as a page sees it. */
    public enum Held {
        /** This page holds it: this page writes the log. */
        HERE,
        /** Another page holds it: this one reads the log only. */
        ELSEWHERE,
        /** Another page took it from this one: this one reads only, from then on. */
        TAKEN,
        /** The browser keeps no locks: this page writes the log, unguarded. */
        UNGUARDED
    }

    public WriteLock {
        Objects.requireNonNull(log, "WriteLock.log");
        Objects.requireNonNull(held, "WriteLock.held");
    }

    /** Whether this page writes the log. */
    public boolean writes() { return held == Held.HERE || held == Held.UNGUARDED; }
}
