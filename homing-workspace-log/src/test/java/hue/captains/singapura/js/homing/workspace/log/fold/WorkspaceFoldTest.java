package hue.captains.singapura.js.homing.workspace.log.fold;

import hue.captains.singapura.js.homing.workspace.log.GridState;
import hue.captains.singapura.js.homing.workspace.log.Host;
import hue.captains.singapura.js.homing.workspace.log.LogIds.RegionId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.TabId;
import hue.captains.singapura.js.homing.workspace.core.models.WidgetId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetKind;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetTitle;
import hue.captains.singapura.js.homing.workspace.log.PaneEvent.PaneShown;
import hue.captains.singapura.js.homing.workspace.log.PaneState;
import hue.captains.singapura.js.homing.workspace.log.RosterEvent.WidgetClosed;
import hue.captains.singapura.js.homing.workspace.log.RosterEvent.WidgetOpened;
import hue.captains.singapura.js.homing.workspace.log.RosterState;
import hue.captains.singapura.js.homing.workspace.log.RosterState.PrefixSequence;
import hue.captains.singapura.js.homing.workspace.log.TabEvent.TabOpened;
import hue.captains.singapura.js.homing.workspace.log.WidgetParam;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceEvent;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The fold by layers: each event handed, by its family, to its layer's fold -
 * the roster's, the one pane's over it, the grid's - and each layer's rules,
 * what it does and what it refuses. The grid's own rules are GridFoldTest's.
 */
class WorkspaceFoldTest {

    private static final WidgetId G1 = WidgetId.of("books-grid-1"), G2 = WidgetId.of("books-grid-2"), J1 = WidgetId.of("book-jumbotron-1");

    private static WorkspaceState fold(WorkspaceEvent... events) {
        WorkspaceState s = WorkspaceState.opening();
        for (WorkspaceEvent e : events) s = WorkspaceFold.apply(s, e);
        return s;
    }

    private static WidgetOpened opened(WidgetId id) {
        return new WidgetOpened(id, WidgetKind.of(id.prefix().replaceAll("_.*", "")), List.of());
    }

    private static PaneShown shown(WidgetId id) { return new PaneShown(Optional.ofNullable(id)); }

    @Test
    void theRosterHoldsWhatIsOpen_inTheOrderOpened_andEachPrefixsLastSequence() {
        var s = fold(opened(G1), opened(J1), opened(G2), new WidgetClosed(G1));
        assertEquals(List.of(J1, G2), s.roster().widgets().stream().map(w -> w.id()).toList());
        assertEquals(List.of(new PrefixSequence("book-jumbotron", 1), new PrefixSequence("books-grid", 2)), s.roster().sequences(),
                "each prefix's last, in the order of the prefixes");
        s = fold(opened(G1), new WidgetClosed(G1));
        assertEquals(List.of(), s.roster().widgets());
        assertEquals(List.of(new PrefixSequence("books-grid", 1)), s.roster().sequences(), "closed, and its id spent all the same");
    }

    @Test
    void aWidgetsParamsAreKept_inTheOrderOfTheirKeys() {
        var params = WidgetParam.of(Map.of("columns", "title,rating", "author", "x"));
        assertEquals(List.of("author", "columns"), params.stream().map(WidgetParam::key).toList());
        var id = WidgetId.of("books-grid_x-title-rating-1");
        var s = fold(new WidgetOpened(id, WidgetKind.of("books-grid"), params));
        assertEquals(params, s.roster().widgets().get(0).params());
        assertThrows(IllegalArgumentException.class, () -> new WidgetOpened(id, WidgetKind.of("books-grid"), List.of(params.get(1), params.get(0))),
                "out of order");
        assertThrows(IllegalArgumentException.class, () -> new WidgetOpened(id, WidgetKind.of("books-grid"), List.of(params.get(0), params.get(0))),
                "a key twice");
    }

    @Test
    void theRosterRefusesAnIdTwice_orOnceMore() {
        assertThrows(WorkspaceFold.Refused.class, () -> fold(opened(G1), opened(G1)), "held already");
        assertThrows(WorkspaceFold.Refused.class, () -> fold(opened(G1), new WidgetClosed(G1), opened(G1)), "given again, after its close");
        assertThrows(WorkspaceFold.Refused.class, () -> fold(opened(G2), opened(G1)), "not past the last its prefix gave");
        assertThrows(WorkspaceFold.Refused.class, () -> fold(new WidgetClosed(G1)), "not held");
        fold(opened(G1), opened(WidgetId.of("books-grid-3")));   // a gap: an id spent unlogged
    }

    @Test
    void thePaneShowsWhatTheRosterHolds_orNothing() {
        var s = fold(opened(G1), opened(J1), shown(J1));
        assertEquals(Optional.of(J1), s.pane().shown());
        assertEquals(Optional.empty(), fold(opened(G1), shown(G1), shown(null)).pane().shown());
        assertThrows(WorkspaceFold.Refused.class, () -> fold(opened(G1), shown(J1)), "not in the roster");
        assertThrows(WorkspaceFold.Refused.class, () -> fold(opened(G1), new WidgetClosed(G1), shown(G1)), "closed");
    }

    @Test
    void thePaneFollowsTheRoster_aWidgetClosedIsShownNowhere() {
        // a live pane says what it shows next, then the widget is closed
        var live = fold(opened(G1), opened(J1), shown(G1), shown(J1), new WidgetClosed(G1));
        assertEquals(Optional.of(J1), live.pane().shown());
        // the pane was not showing when the widget closed: the workspace laid out by another placement
        var other = fold(opened(G1), opened(J1), shown(G1), new WidgetClosed(G1));
        assertEquals(Optional.empty(), other.pane().shown());
        var kept = fold(opened(G1), opened(J1), shown(J1), new WidgetClosed(G1));
        assertEquals(Optional.of(J1), kept.pane().shown(), "another widget closed: the pane keeps what it shows");
    }

    @Test
    void eachFamilyGoesToItsLayer_andNoOther() {
        var main = new Host.InRegion(RegionId.of("main"));
        var s = fold(opened(G1), new TabOpened(TabId.of("tab-1"), WidgetKind.of("note"), WidgetTitle.of("Note"), main, 0), shown(G1));
        assertEquals(1, s.roster().widgets().size());
        assertEquals(Optional.of(G1), s.pane().shown());
        assertEquals(1, s.grid().tabs().size(), "the grid's tab is the grid's; it names no widget yet");
        assertEquals(GridState.opening(), fold(opened(G1), shown(G1)).grid(), "the roster and the pane leave the grid as it was");
        assertEquals(RosterState.empty(), fold(new TabOpened(TabId.of("tab-1"), WidgetKind.of("note"), WidgetTitle.of("Note"), main, 0)).roster());
        assertEquals(PaneState.empty(), WorkspaceState.opening().pane());
    }

    @Test
    void aStateShowingWhatTheRosterDoesNotHold_isRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> new WorkspaceState(RosterState.empty(), new PaneState(Optional.of(G1)), GridState.opening()));
        assertThrows(IllegalArgumentException.class,
                () -> new RosterState(List.of(new RosterState.RosterEntry(G2, WidgetKind.of("books-grid"), List.of())),
                                      List.of(new PrefixSequence("books-grid", 1))), "held past its prefix's last");
    }
}
