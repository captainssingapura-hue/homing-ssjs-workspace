package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.workspace.log.LogKey;
import hue.captains.singapura.js.homing.workspace.log.json.Json;
import hue.captains.singapura.js.homing.workspace.log.json.JsonText;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link CheckpointStorage} of files under a directory: one per log,
 * {@code <root>/kind-<kind>/<workspace>.checkpoint.json}, holding the checkpoint
 * exactly as the page posted it - a file a person or a tool can read as it is.
 * A checkpoint is written to a file beside it and moved over it in one step,
 * so a reader never sees half of one; how far the kept one goes is read from
 * the file itself, its folded state's {@code through}, and a file that does not
 * read goes nowhere, so any checkpoint replaces it.
 *
 * <p>Writes to one log are taken one at a time within this server. Two servers
 * writing the same directory are not guarded against: a server that runs more
 * than one of itself keeps its checkpoints in a database or an object store.</p>
 */
public final class FileCheckpointStorage implements CheckpointStorage {

    private final Path root;
    private final ConcurrentHashMap<LogKey, Object> locks = new ConcurrentHashMap<>();

    public FileCheckpointStorage(Path root) {
        this.root = Objects.requireNonNull(root, "FileCheckpointStorage.root").toAbsolutePath();
    }

    /** The directory the checkpoints are kept under. */
    public Path root() { return root; }

    /** Where a log's checkpoint is kept. The kind's directory is prefixed, so that no kind is a name a file system keeps for itself. */
    public Path fileOf(LogKey log) {
        return root.resolve("kind-" + log.kind().value()).resolve(log.workspace() + ".checkpoint.json");
    }

    @Override
    public Optional<String> read(LogKey log) {
        Path file = fileOf(Objects.requireNonNull(log, "log"));
        try {
            return Files.exists(file) ? Optional.of(Files.readString(file, StandardCharsets.UTF_8)) : Optional.empty();
        } catch (IOException e) {
            throw new UncheckedIOException("failed to read the checkpoint of " + log + " at " + file, e);
        }
    }

    @Override
    public boolean writeIfNewer(LogKey log, long through, String checkpoint) {
        Objects.requireNonNull(checkpoint, "checkpoint");
        Path file = fileOf(Objects.requireNonNull(log, "log"));
        synchronized (locks.computeIfAbsent(log, k -> new Object())) {
            try {
                if (Files.exists(file) && throughIn(Files.readString(file, StandardCharsets.UTF_8)) >= through) return false;
                Files.createDirectories(file.getParent());
                Path next = Files.createTempFile(file.getParent(), ".checkpoint-", ".tmp");
                try {
                    Files.writeString(next, checkpoint, StandardCharsets.UTF_8);
                    Files.move(next, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } finally {
                    Files.deleteIfExists(next);
                }
                return true;
            } catch (IOException e) {
                throw new UncheckedIOException("failed to write the checkpoint of " + log + " at " + file, e);
            }
        }
    }

    /** How far a kept checkpoint goes: its folded state's {@code through}; -1 when it does not read. */
    static long throughIn(String text) {
        try {
            var folded = ((Json.Obj) ((Json.Obj) JsonText.parse(text)).members().get("folded")).members().get("through");
            return ((Json.Int) folded).value();
        } catch (RuntimeException e) {
            return -1;
        }
    }
}
