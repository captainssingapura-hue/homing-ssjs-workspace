package hue.captains.singapura.js.homing.workspace.log;

import hue.captains.singapura.js.homing.workspace.log.LogIds.EventSeq;
import hue.captains.singapura.js.homing.workspace.log.LogIds.FloatId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.RegionId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.SplitPath;
import hue.captains.singapura.js.homing.workspace.log.LogIds.TabId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetKind;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetTitle;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceInstanceId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceKind;
import hue.captains.singapura.js.homing.workspace.log.RegionEvent.Side;

import hue.captains.singapura.js.homing.workspace.log.RegionEvent.RegionParted;
import hue.captains.singapura.js.homing.workspace.log.TabEvent.TabClosed;
import hue.captains.singapura.js.homing.workspace.log.TabEvent.TabOpened;
import hue.captains.singapura.js.homing.workspace.log.RegionEvent.TracksChanged;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** The log's records hold their own rules: nothing out of them is ever written. */
class WorkspaceEventTest {

    private static final TabId T = TabId.of("tab-1");
    private static final RegionId MAIN = RegionId.of("main");

    @Test
    void idsKeepTheirGrammar() {
        assertThrows(IllegalArgumentException.class, () -> TabId.of("tab 1"));
        assertThrows(IllegalArgumentException.class, () -> RegionId.of(""));
        assertThrows(IllegalArgumentException.class, () -> WorkspaceKind.of("focus lab"));
        assertDoesNotThrow(() -> WorkspaceKind.of("focus-lab"));
    }

    @Test
    void anIndexIsNeverNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> new TabOpened(T, WidgetKind.of("note"), WidgetTitle.of("Note"), new Host.InRegion(MAIN), -1));
    }

    @Test
    void aTabIsInARegionOrAFloat_andAFloatHasAMeasure() {
        assertEquals(new Host.InFloat(FloatId.of("float-1")), Host.floating("float-1"));
        assertThrows(IllegalArgumentException.class, () -> FloatId.of("float 1"));
        assertThrows(IllegalArgumentException.class, () -> new FloatEvent.FloatOpened(FloatId.of("float-1"), 10, 10, 0, 200));
        assertThrows(IllegalArgumentException.class, () -> new FloatEvent.FloatResized(FloatId.of("float-1"), 300, -1));
        assertDoesNotThrow(() -> new FloatEvent.FloatMoved(FloatId.of("float-1"), -20, 0));
        assertThrows(NullPointerException.class, () -> new TabEvent.TabMoved(T, null, 0));
    }

    @Test
    void aRegionIsNotPartedIntoItself() {
        assertThrows(IllegalArgumentException.class, () -> new RegionParted(MAIN, MAIN, Side.RIGHT));
    }

    @Test
    void sharesAreExactAndAddUpToOne() {
        var third = Scaled.of(333_333, 6);
        assertDoesNotThrow(() -> new TracksChanged(SplitPath.ROOT, List.of(third, third, Scaled.of(333_334, 6))));
        assertThrows(IllegalArgumentException.class, () -> new TracksChanged(SplitPath.ROOT, List.of(third, third, third)));
        assertThrows(IllegalArgumentException.class, () -> new TracksChanged(SplitPath.ROOT, List.of(Scaled.of(5, 1), Scaled.of(50, 2))));
        assertThrows(IllegalArgumentException.class, () -> new TracksChanged(SplitPath.ROOT, List.of(Scaled.of(10, 1), Scaled.of(0, 1))));
        assertThrows(IllegalArgumentException.class, () -> new TracksChanged(SplitPath.ROOT, List.of(Scaled.of(1, 0))));
    }

    @Test
    void aScaledNumberIsExact() {
        assertEquals(new BigDecimal("0.333333"), Scaled.of(333_333, 6).toBigDecimal());
        assertEquals(Scaled.of(500_000, 6), Scaled.of(new BigDecimal("0.5"), 6));
        assertThrows(ArithmeticException.class, () -> Scaled.of(new BigDecimal("0.1234567"), 6));
        assertThrows(IllegalArgumentException.class, () -> Scaled.of(Scaled.MAX_SAFE + 1, 0));
    }

    @Test
    void aLoggedEventIsCountedFromOneToTheMillisecond() {
        var e = new TabClosed(T);
        assertThrows(IllegalArgumentException.class, () -> new LoggedEvent(EventSeq.of(0), Instant.ofEpochMilli(1), e));
        assertThrows(IllegalArgumentException.class, () -> new LoggedEvent(EventSeq.of(1), Instant.ofEpochSecond(1, 1), e));
        assertThrows(IllegalArgumentException.class, () -> new LoggedEvent(EventSeq.of(1), Instant.ofEpochMilli(-1), e));
        assertDoesNotThrow(() -> new LoggedEvent(EventSeq.of(1), Instant.ofEpochMilli(1_790_000_000_000L), e));
    }

    @Test
    void aHeaderSaysWhatItIs() {
        var id = WorkspaceInstanceId.fresh();
        assertThrows(IllegalArgumentException.class, () -> new LogHeader("other", 1, WorkspaceKind.of("demo"), id));
        assertThrows(IllegalArgumentException.class, () -> new LogHeader(LogHeader.FORMAT, 1, WorkspaceKind.of("demo"), id));
        assertEquals(LogHeader.FORMAT, LogHeader.of(WorkspaceKind.of("demo"), id).format());
    }
}
