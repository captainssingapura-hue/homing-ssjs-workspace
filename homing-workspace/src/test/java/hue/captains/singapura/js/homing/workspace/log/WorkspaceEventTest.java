package hue.captains.singapura.js.homing.workspace.log;

import hue.captains.singapura.js.homing.workspace.events.contract.EventSeq;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceEvent.RegionParted;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceEvent.TabClosed;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceEvent.TabOpened;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceEvent.TracksChanged;
import hue.captains.singapura.js.homing.workspace.state.SplitPath;
import hue.captains.singapura.js.homing.workspace.state.WidgetKind;
import hue.captains.singapura.js.homing.workspace.state.WidgetTitle;
import hue.captains.singapura.js.homing.workspace.state.WorkspaceInstanceId;
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
        assertThrows(IllegalArgumentException.class, () -> WorkspaceSpecKind.of("focus lab"));
        assertDoesNotThrow(() -> WorkspaceSpecKind.of("focus-lab"));
    }

    @Test
    void anIndexIsNeverNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> new TabOpened(T, WidgetKind.of("note"), WidgetTitle.of("Note"), MAIN, -1));
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
        assertThrows(IllegalArgumentException.class, () -> new LogHeader("other", 1, WorkspaceSpecKind.of("demo"), id));
        assertThrows(IllegalArgumentException.class, () -> new LogHeader(LogHeader.FORMAT, 2, WorkspaceSpecKind.of("demo"), id));
        assertEquals(LogHeader.FORMAT, LogHeader.of(WorkspaceSpecKind.of("demo"), id).format());
    }
}
