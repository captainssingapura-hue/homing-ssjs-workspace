package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.workspace.log.LogKey;
import hue.captains.singapura.js.homing.workspace.log.Checkpoint;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceInstanceId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceKind;
import hue.captains.singapura.js.homing.workspace.log.codec.CheckpointCodec;
import hue.captains.singapura.js.homing.workspace.log.json.JsonText;

import java.util.Objects;
import java.util.Optional;

/**
 * The {@link CheckpointKeeper}, over any {@link CheckpointStorage}: it holds the
 * rules, the storage only the text. A checkpoint is stored as the canonical
 * JSON the generated codec writes - the bytes the page posted - with how far it
 * goes through its log, and read back through the same codec, every value
 * checked as its declaration checks it.
 */
public final class StoredCheckpointKeeper implements CheckpointKeeper {

    private final CheckpointStorage storage;

    public StoredCheckpointKeeper(CheckpointStorage storage) {
        this.storage = Objects.requireNonNull(storage, "StoredCheckpointKeeper.storage");
    }

    /** A keeper in the server's memory, for as long as it runs. */
    public static StoredCheckpointKeeper inMemory() { return new StoredCheckpointKeeper(new MemoryCheckpointStorage()); }

    @Override
    public boolean keep(Checkpoint checkpoint) {
        Objects.requireNonNull(checkpoint, "StoredCheckpointKeeper.checkpoint");
        return storage.writeIfNewer(LogKey.of(checkpoint.folded().header()), checkpoint.folded().through().value(),
                JsonText.write(CheckpointCodec.INSTANCE.transformTo(checkpoint)));
    }

    /** @throws IllegalStateException when what the storage keeps for the log is not a checkpoint */
    @Override
    public Optional<Checkpoint> latest(WorkspaceKind kind, WorkspaceInstanceId workspace) {
        var log = new LogKey(kind, workspace);
        return storage.read(log).map(text -> {
            try { return CheckpointCodec.INSTANCE.transformFrom(JsonText.parse(text)); }
            catch (RuntimeException e) { throw new IllegalStateException("the checkpoint stored for " + log + " does not read: " + e.getMessage(), e); }
        });
    }
}
