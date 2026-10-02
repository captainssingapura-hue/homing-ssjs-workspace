package hue.captains.singapura.js.homing.workspace.log;

import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceName;

import java.time.Instant;
import java.util.Objects;

/**
 * One workspace as the browser lists its kind's workspaces: which log it is,
 * what it is called, when it was first opened and when last. A workspace is
 * listed the first time a page writes it, under a name of its own among its
 * kind's, and noted each time one opens it to write. Never logged: what a
 * workspace is called is not its state.
 *
 * @param log     which workspace: its kind and its id
 * @param name    what it is called
 * @param created when it was first opened, whole milliseconds since the epoch
 * @param opened  when it was last opened to write, the same
 */
public record WorkspaceEntry(LogKey log, WorkspaceName name, Instant created, Instant opened) {

    public WorkspaceEntry {
        Objects.requireNonNull(log, "WorkspaceEntry.log");
        Objects.requireNonNull(name, "WorkspaceEntry.name");
        wholeMillis(Objects.requireNonNull(created, "WorkspaceEntry.created"), "created");
        wholeMillis(Objects.requireNonNull(opened, "WorkspaceEntry.opened"), "opened");
    }

    private static void wholeMillis(Instant at, String field) {
        if (at.getNano() % 1_000_000 != 0) {
            throw new IllegalArgumentException("WorkspaceEntry." + field + " " + at + " — whole milliseconds only");
        }
        long ms = at.toEpochMilli();
        if (ms < 0 || ms > Scaled.MAX_SAFE) {
            throw new IllegalArgumentException("WorkspaceEntry." + field + " " + at + " — from the epoch to 2^53 − 1 ms");
        }
    }
}
