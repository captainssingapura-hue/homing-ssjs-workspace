package hue.captains.singapura.js.homing.workspace.log.fold;

import hue.captains.singapura.js.homing.workspace.log.Axis;
import hue.captains.singapura.js.homing.workspace.log.FloatId;
import hue.captains.singapura.js.homing.workspace.log.Host;
import hue.captains.singapura.js.homing.workspace.log.Layout;
import hue.captains.singapura.js.homing.workspace.log.RegionId;
import hue.captains.singapura.js.homing.workspace.log.Scaled;
import hue.captains.singapura.js.homing.workspace.log.Side;
import hue.captains.singapura.js.homing.workspace.log.TabId;
import hue.captains.singapura.js.homing.workspace.log.Track;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceEvent;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceEvent.*;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceState;
import hue.captains.singapura.js.homing.workspace.state.SplitPath;
import hue.captains.singapura.js.homing.workspace.state.WidgetKind;
import hue.captains.singapura.js.homing.workspace.state.WidgetTitle;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The fold's rules, one at a time: what each event does to the state, and what it refuses. */
class WorkspaceFoldTest {

    private static final RegionId MAIN = RegionId.of("main"), B = RegionId.of("b"), C = RegionId.of("c");
    private static final TabId T1 = TabId.of("tab-1"), T2 = TabId.of("tab-2");
    private static final FloatId F1 = FloatId.of("float-1");

    private static WorkspaceState fold(WorkspaceEvent... events) {
        WorkspaceState s = WorkspaceState.opening();
        for (WorkspaceEvent e : events) s = WorkspaceFold.apply(s, e);
        return s;
    }

    private static TabOpened open(TabId id, Host host, int index) {
        return new TabOpened(id, WidgetKind.of("note"), WidgetTitle.of("Note"), host, index);
    }

    private static Host main() { return new Host.InRegion(MAIN); }

    @Test
    void tabsOpenMoveShowAndClose_whereTheLogSays() {
        var s = fold(open(T1, main(), 0), open(T2, main(), 0), new TabShown(main(), T1),
                     new TabMoved(T1, main(), 0), new TabRenamed(T2, WidgetTitle.of("Mine")));
        assertEquals(List.of(T1, T2), s.regions().get(0).tabs(), "a reorder within the region");
        assertEquals(Optional.of(T1), s.regions().get(0).shown(), "a reorder keeps the tab shown");
        assertEquals("Mine", s.tabs().get(1).title().value());
        assertEquals(List.of(T1, T2), s.tabs().stream().map(t -> t.id()).toList(), "tabs in the order the hosts hold them");
        s = WorkspaceFold.apply(s, new TabClosed(T1));
        assertEquals(Optional.empty(), s.regions().get(0).shown(), "the shown tab closed: the region shows what the log says next");
    }

    @Test
    void aFloatIsAHost_withAPlaceAMeasureAndAStack() {
        var s = fold(open(T1, main(), 0), new FloatOpened(F1, 10, 20, 320, 220), new FloatOpened(FloatId.of("float-2"), 0, 0, 100, 100),
                     new TabMoved(T1, new Host.InFloat(F1), 0), new TabShown(new Host.InFloat(F1), T1),
                     new FloatMoved(F1, 30, 40), new FloatResized(F1, 400, 300), new FloatRaised(F1));
        assertEquals(List.of("float-2", "float-1"), s.floats().stream().map(f -> f.id().value()).toList(), "raised: on top");
        var f = s.floats().get(1);
        assertEquals(List.of(30, 40, 400, 300), List.of(f.x(), f.y(), f.w(), f.h()));
        assertEquals(List.of(T1), f.tabs());
        assertEquals(Optional.of(T1), f.shown());
        assertEquals(List.of(), s.regions().get(0).tabs(), "the tab afloat is in its float, and nowhere else");
    }

    @Test
    void theFoldRefusesWhatCannotBe() {
        assertThrows(WorkspaceFold.Refused.class, () -> fold(open(T1, main(), 0), open(T1, main(), 0)), "open twice");
        assertThrows(WorkspaceFold.Refused.class, () -> fold(open(T1, main(), 1)), "past the end");
        assertThrows(WorkspaceFold.Refused.class, () -> fold(new TabMoved(T1, main(), 0)), "not open");
        assertThrows(WorkspaceFold.Refused.class, () -> fold(open(T1, new Host.InFloat(F1), 0)), "no such float");
        assertThrows(WorkspaceFold.Refused.class, () -> fold(open(T1, main(), 0), new TabShown(new Host.InRegion(B), T1)), "no such region");
        assertThrows(IllegalArgumentException.class, () -> fold(open(T1, main(), 0), new RegionParted(MAIN, B, Side.RIGHT),
                                                               new TabShown(new Host.InRegion(B), T1)), "not held there");
        assertThrows(WorkspaceFold.Refused.class, () -> fold(open(T1, main(), 0), new RegionParted(MAIN, B, Side.RIGHT),
                                                              new RegionRemoved(MAIN, Optional.of(B))), "a region still holding a tab");
        assertThrows(WorkspaceFold.Refused.class, () -> fold(open(T1, main(), 0), new FloatOpened(F1, 0, 0, 1, 1),
                                                              new TabMoved(T1, new Host.InFloat(F1), 0), new FloatClosed(F1)), "a float still holding a tab");
        assertThrows(LayoutAlgebra.Refused.class, () -> fold(new RegionRemoved(MAIN, Optional.empty())), "the last region");
        assertThrows(LayoutAlgebra.Refused.class, () -> fold(new RegionParted(MAIN, B, Side.RIGHT), new RegionParted(B, MAIN, Side.TOP)), "a name taken");
        assertThrows(LayoutAlgebra.Refused.class, () -> fold(new TracksChanged(SplitPath.ROOT, List.of(Scaled.of(5, 1), Scaled.of(5, 1)))), "no split");
    }

    @Test
    void regionsPartAsTheGridParts_andTheirRoomGoesAsItGoes() {
        var s = fold(new RegionParted(MAIN, B, Side.RIGHT), new RegionParted(B, C, Side.RIGHT));
        var row = (Layout.Split) s.layout();
        assertEquals(Axis.HORIZONTAL, row.axis());
        assertEquals(List.of(500_000L, 250_000L, 250_000L), row.tracks().stream().map(t -> t.share().units()).toList(), "a sibling halves the room it came from");
        assertEquals(List.of(MAIN, B, C), s.regions().stream().map(r -> r.id()).toList(), "regions in the layout's order");

        s = WorkspaceFold.apply(s, new RegionParted(B, RegionId.of("d"), Side.BOTTOM));
        var inner = (Layout.Split) ((Layout.Split) s.layout()).tracks().get(1).node();
        assertEquals(Axis.VERTICAL, inner.axis(), "across the row: the cell becomes a split of two");

        s = WorkspaceFold.apply(s, new TracksChanged(SplitPath.ROOT, List.of(Scaled.of(333_333, 6), Scaled.of(333_333, 6), Scaled.of(333_334, 6))));
        s = WorkspaceFold.apply(s, new RegionRemoved(C, Optional.of(MAIN)));
        var after = (Layout.Split) s.layout();
        assertEquals(2, after.tracks().size());
        assertEquals(List.of(333_333L, 666_667L), after.tracks().stream().map(t -> t.share().units()).toList(),
                "toward main, which neither shares a splitter with c nor is beside it: to the neighbour before it");
        s = WorkspaceFold.apply(s, new RegionRemoved(RegionId.of("d"), Optional.of(B)));
        assertEquals(List.of(MAIN, B), s.regions().stream().map(r -> r.id()).toList(), "the split of one gave way to b");
        assertTrue(s.layout() instanceof Layout.Split);
    }

    @Test
    void theRoomCrossesASplitterWhenTheRegionNamedSharesOne() {
        // main | [ b over c ]: removing b toward c — c is across b's own splitter, and takes the whole of b's room
        var s = fold(new RegionParted(MAIN, B, Side.RIGHT), new RegionParted(B, C, Side.BOTTOM),
                     new RegionRemoved(B, Optional.of(C)));
        var row = (Layout.Split) s.layout();
        assertEquals(List.of(new Track(new Layout.Cell(MAIN), Scaled.of(500_000, 6)), new Track(new Layout.Cell(C), Scaled.of(500_000, 6))), row.tracks());
    }
}
