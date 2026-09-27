package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.workspace.log.Checkpoint;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceInstanceId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceKind;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** A {@link CheckpointKeeper} in the server's memory: the latest checkpoint of each log, for as long as the server runs. */
public final class InMemoryCheckpointKeeper implements CheckpointKeeper {

    private record Log(WorkspaceKind kind, WorkspaceInstanceId workspace) {}

    private final ConcurrentHashMap<Log, Checkpoint> kept = new ConcurrentHashMap<>();

    @Override
    public boolean keep(Checkpoint checkpoint) {
        Objects.requireNonNull(checkpoint, "InMemoryCheckpointKeeper.checkpoint");
        var header = checkpoint.folded().header();
        var took = new boolean[1];
        kept.compute(new Log(header.kind(), header.workspaceId()), (log, had) -> {
            if (had != null && had.folded().through().value() >= checkpoint.folded().through().value()) return had;
            took[0] = true;
            return checkpoint;
        });
        return took[0];
    }

    @Override
    public Optional<Checkpoint> latest(WorkspaceKind kind, WorkspaceInstanceId workspace) {
        return Optional.ofNullable(kept.get(new Log(kind, workspace)));
    }
}
