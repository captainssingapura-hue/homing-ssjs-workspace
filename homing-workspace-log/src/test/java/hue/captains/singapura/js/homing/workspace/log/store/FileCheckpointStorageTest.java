package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.workspace.log.LogKey;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The storage contract, kept in files; and what files add: the checkpoint is
 * the file, as posted, where the layout says; it outlives the server that wrote
 * it; nothing half-written is left beside it; a file that does not read is
 * replaced by any checkpoint, and read back by the keeper, is said to be broken.
 */
class FileCheckpointStorageTest extends CheckpointStorageContract {

    @TempDir Path dir;
    private int made;

    @Override CheckpointStorage storage() { return new FileCheckpointStorage(dir.resolve("s" + (made++))); }

    @Test
    void theCheckpointIsTheFile_asPosted_whereTheLayoutSays() throws IOException {
        var s = new FileCheckpointStorage(dir);
        assertTrue(write(s, DEMO, 4));
        Path file = dir.resolve("kind-demo").resolve("7f1b6c2e-5000-9000-7f1b-6c2e00000001.checkpoint.json");
        assertEquals(file, s.fileOf(DEMO));
        assertEquals(text(through(DEMO, 4)), Files.readString(file, StandardCharsets.UTF_8));
        try (Stream<Path> all = Files.walk(dir)) {
            assertEquals(1, all.filter(Files::isRegularFile).count(), "nothing half-written left beside it");
        }
    }

    @Test
    void itOutlivesTheServerThatWroteIt() {
        assertTrue(write(new FileCheckpointStorage(dir), DEMO, 9));
        var again = new FileCheckpointStorage(dir);
        assertEquals(text(through(DEMO, 9)), again.read(DEMO).orElseThrow());
        assertFalse(write(again, DEMO, 8), "the kept one is still measured against");
    }

    @Test
    void aKindThatIsANameTheFileSystemKeepsIsStillADirectoryOfItsOwn() {
        var s = new FileCheckpointStorage(dir);
        var con = new LogKey(WorkspaceKind.of("con"), DEMO.workspace());
        assertTrue(write(s, con, 2));
        assertEquals(dir.resolve("kind-con"), s.fileOf(con).getParent());
        assertEquals(text(through(con, 2)), s.read(con).orElseThrow());
    }

    @Test
    void aFileThatDoesNotRead_isReplacedByAnyCheckpoint_andTheKeeperSaysItIsBroken() throws IOException {
        var s = new FileCheckpointStorage(dir);
        Files.createDirectories(s.fileOf(DEMO).getParent());
        Files.writeString(s.fileOf(DEMO), "{ half a checkpoint", StandardCharsets.UTF_8);
        var keeper = new StoredCheckpointKeeper(s);
        var broken = assertThrows(IllegalStateException.class, () -> keeper.latest(DEMO.kind(), DEMO.workspace()));
        assertTrue(broken.getMessage().contains("demo/"), broken.getMessage());
        assertTrue(keeper.keep(through(DEMO, 1)));
        assertEquals(1, keeper.latest(DEMO.kind(), DEMO.workspace()).orElseThrow().events());
    }

    @Test
    void theKeeperOverFiles_keepsAndReadsBackTheCheckpoint() {
        var keeper = new StoredCheckpointKeeper(new FileCheckpointStorage(dir));
        var c = through(DEMO, 6);
        assertTrue(keeper.keep(c));
        assertFalse(keeper.keep(through(DEMO, 5)));
        assertEquals(c, keeper.latest(DEMO.kind(), DEMO.workspace()).orElseThrow());
    }
}
