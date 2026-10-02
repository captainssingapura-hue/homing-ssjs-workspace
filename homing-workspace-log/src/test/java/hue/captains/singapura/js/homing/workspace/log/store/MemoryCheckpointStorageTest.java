package hue.captains.singapura.js.homing.workspace.log.store;

/** The storage contract, kept in memory. */
class MemoryCheckpointStorageTest extends CheckpointStorageContract {
    @Override CheckpointStorage storage() { return new MemoryCheckpointStorage(); }
}
