package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.workspace.log.Checkpoint;
import hue.captains.singapura.js.homing.workspace.log.Host;
import hue.captains.singapura.js.homing.workspace.log.LogHeader;
import hue.captains.singapura.js.homing.workspace.log.LogIds.EventSeq;
import hue.captains.singapura.js.homing.workspace.log.LogIds.TabId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetKind;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetTitle;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceInstanceId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceKind;
import hue.captains.singapura.js.homing.workspace.log.LoggedEvent;
import hue.captains.singapura.js.homing.workspace.log.TabEvent;
import hue.captains.singapura.js.homing.workspace.log.codec.CheckpointCodec;
import hue.captains.singapura.js.homing.workspace.log.fold.CheckpointFold;
import hue.captains.singapura.js.homing.workspace.log.json.JsonText;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What every {@link CheckpointStorage} does, whatever keeps the text: none until
 * one is written; a checkpoint kept only when it goes further than the one
 * kept; the text read back as written; each log apart; and, of writes racing,
 * the furthest what stays. A backend's own test extends this and says how to
 * make one - a database's or an object store's as much as these.
 */
abstract class CheckpointStorageContract {

    /** A fresh, empty storage. */
    abstract CheckpointStorage storage();

    static final LogKey DEMO = new LogKey(WorkspaceKind.of("demo"), WorkspaceInstanceId.parse("7f1b6c2e-5000-9000-7f1b-6c2e00000001"));
    static final LogKey NOTES = new LogKey(WorkspaceKind.of("notes"), WorkspaceInstanceId.parse("7f1b6c2e-5000-9000-7f1b-6c2e00000001"));

    /** A real checkpoint of a log of tabs opened, through its n-th event, and its text. */
    static Checkpoint through(LogKey log, int n) {
        var events = new ArrayList<LoggedEvent>();
        for (int i = 1; i <= n; i++) {
            events.add(new LoggedEvent(EventSeq.of(i), Instant.ofEpochMilli(1_790_000_000_000L + i),
                    new TabEvent.TabOpened(TabId.of("tab-" + i), WidgetKind.of("note"), WidgetTitle.of("Note " + i), Host.region("main"), i - 1)));
        }
        return CheckpointFold.next(null, LogHeader.of(log.kind(), log.workspace()), events);
    }

    static String text(Checkpoint c) { return JsonText.write(CheckpointCodec.INSTANCE.transformTo(c)); }

    static boolean write(CheckpointStorage s, LogKey log, int n) { return s.writeIfNewer(log, n, text(through(log, n))); }

    @Test
    void noneUntilOneIsWritten_thenThatTextExactly() {
        var s = storage();
        assertEquals(Optional.empty(), s.read(DEMO));
        assertTrue(write(s, DEMO, 3));
        assertEquals(Optional.of(text(through(DEMO, 3))), s.read(DEMO));
    }

    @Test
    void onlyAFurtherOneReplacesTheOneKept() {
        var s = storage();
        assertTrue(write(s, DEMO, 5));
        assertFalse(write(s, DEMO, 4), "an older one arriving late");
        assertFalse(write(s, DEMO, 5), "the same again");
        assertEquals(text(through(DEMO, 5)), s.read(DEMO).orElseThrow());
        assertTrue(write(s, DEMO, 6));
        assertEquals(text(through(DEMO, 6)), s.read(DEMO).orElseThrow());
    }

    @Test
    void eachLogIsApart() {
        var s = storage();
        assertTrue(write(s, DEMO, 7));
        assertEquals(Optional.empty(), s.read(NOTES));
        assertTrue(write(s, NOTES, 2), "a log's checkpoint is not measured against another's");
        assertEquals(text(through(DEMO, 7)), s.read(DEMO).orElseThrow());
        assertEquals(text(through(NOTES, 2)), s.read(NOTES).orElseThrow());
    }

    @Test
    void ofWritesRacing_theFurthestStays() throws Exception {
        var s = storage();
        var texts = new String[41];
        for (int n = 1; n <= 40; n++) texts[n] = text(through(DEMO, n));
        ExecutorService pool = Executors.newFixedThreadPool(8);
        var kept = new AtomicInteger();
        var rnd = new Random(7);
        for (int i = 0; i < 200; i++) {
            int n = 1 + rnd.nextInt(40);
            pool.submit(() -> { if (s.writeIfNewer(DEMO, n, texts[n])) kept.incrementAndGet(); });
        }
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));
        assertTrue(kept.get() >= 1);
        String stays = s.read(DEMO).orElseThrow();
        int furthest = 0;
        for (int n = 1; n <= 40; n++) if (texts[n].equals(stays)) furthest = n;
        var again = new Random(7);
        int max = 0;
        for (int i = 0; i < 200; i++) max = Math.max(max, 1 + again.nextInt(40));
        assertEquals(max, furthest, "the furthest written is what stays");
    }
}
