package hue.captains.singapura.js.homing.workspace.log.store;

import java.util.Optional;

/**
 * Where a server's checkpoints are stored: the one thing a backend implements.
 * Per log, one checkpoint - its text, the canonical JSON the page posted, and
 * how far through the log it goes - and never one over it that goes less far.
 *
 * <p>Deliberately small, so that a database or an object store is a class of
 * its own: {@link StoredCheckpointKeeper} holds the rules - what a checkpoint
 * is, how it is written and read, which is newer - and a storage only keeps
 * text. A database keeps a row per log and writes with one conditional upsert
 * ({@code ... ON CONFLICT (kind, workspace) DO UPDATE ... WHERE through < excluded.through});
 * an object store, an object per log with its {@code through} beside it, written
 * by a conditional put. {@link FileCheckpointStorage} keeps a file per log;
 * {@link MemoryCheckpointStorage}, a map.</p>
 */
public interface CheckpointStorage {

    /** The log's checkpoint as kept, or none. */
    Optional<String> read(LogKey log);

    /**
     * Keeps this as the log's checkpoint when it goes further through the log than
     * the one kept - or none is kept - and says whether it did. Atomic: of two
     * writes racing, the one that goes further is what stays.
     *
     * @param through    how far through the log it goes: its folded state's last event
     * @param checkpoint its text, the canonical JSON
     */
    boolean writeIfNewer(LogKey log, long through, String checkpoint);
}
