package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.workspace.log.Checkpoint;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceInstanceId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceKind;

import java.util.Optional;

/**
 * Where a server keeps the checkpoints its pages send it: the latest of each
 * log, a log being its kind and its workspace. The page only posts, and the
 * checkpoint arrives here read and checked, a Java {@link Checkpoint}.
 *
 * <p>{@link StoredCheckpointKeeper} is the one a server wants: it holds the
 * rules and keeps the text in a {@link CheckpointStorage} - files, memory, or a
 * database or an object store behind a class of the server's own.</p>
 */
public interface CheckpointKeeper {

    /**
     * Keeps it when it is the log's latest - through further than the one kept -
     * and says whether it did; an older one arriving late is not kept.
     */
    boolean keep(Checkpoint checkpoint);

    /** The log's latest checkpoint, if one was kept. */
    Optional<Checkpoint> latest(WorkspaceKind kind, WorkspaceInstanceId workspace);
}
