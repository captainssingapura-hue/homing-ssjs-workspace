package hue.captains.singapura.js.homing.workspace.log.store;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** A {@link CheckpointStorage} in the server's memory: a checkpoint per log, for as long as it runs. */
public final class MemoryCheckpointStorage implements CheckpointStorage {

    private record Kept(long through, String checkpoint) {}

    private final ConcurrentHashMap<LogKey, Kept> kept = new ConcurrentHashMap<>();

    @Override
    public Optional<String> read(LogKey log) {
        return Optional.ofNullable(kept.get(Objects.requireNonNull(log, "log"))).map(Kept::checkpoint);
    }

    @Override
    public boolean writeIfNewer(LogKey log, long through, String checkpoint) {
        Objects.requireNonNull(checkpoint, "checkpoint");
        var took = new boolean[1];
        kept.compute(Objects.requireNonNull(log, "log"), (k, had) -> {
            if (had != null && had.through() >= through) return had;
            took[0] = true;
            return new Kept(through, checkpoint);
        });
        return took[0];
    }
}
