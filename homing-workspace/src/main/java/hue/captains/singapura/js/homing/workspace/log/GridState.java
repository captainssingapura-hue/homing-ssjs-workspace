package hue.captains.singapura.js.homing.workspace.log;

import hue.captains.singapura.js.homing.workspace.log.LogIds.FloatId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.RegionId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.TabId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetKind;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetTitle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The split grid's layer of what a workspace log folds to ({@link
 * WorkspaceState}): its arrangement, whole. The layout of the regions; each
 * region's tabs in order and the one it shows; each float's place, measure,
 * tabs and the one it shows, the floats in their stacking order from the
 * bottom; and every open tab — its kind and title — in the order the hosts hold
 * them: the regions' in the layout's order, then the floats' from the bottom,
 * each host's as its strip shows them. Nothing in it is history a live
 * workspace does not show, so a workspace can be read back into one. Every tab
 * is in exactly one host, and every host holds only open tabs: a state that is
 * not so is refused, whatever built it. Folded from the grid's families -
 * {@link TabEvent}, {@link RegionEvent}, {@link FloatEvent} - and from nothing
 * else; it does not yet refer to the roster's widgets.
 *
 * @param layout  the regions as a tree
 * @param regions one per cell of the layout, in the layout's order
 * @param floats  the floats, bottom first
 * @param tabs    the open tabs, in the order the hosts hold them
 */
public record GridState(Layout layout, List<RegionState> regions, List<FloatState> floats, List<TabState> tabs) {

    /** An open tab: which, what it holds, what it is called. */
    public record TabState(TabId id, WidgetKind kind, WidgetTitle title) {
        public TabState {
            Objects.requireNonNull(id, "TabState.id");
            Objects.requireNonNull(kind, "TabState.kind");
            Objects.requireNonNull(title, "TabState.title");
        }
    }

    /** A region: its tabs in the order its strip shows them, and the one it shows, if any. */
    public record RegionState(RegionId id, List<TabId> tabs, Optional<TabId> shown) {
        public RegionState {
            Objects.requireNonNull(id, "RegionState.id");
            tabs = List.copyOf(Objects.requireNonNull(tabs, "RegionState.tabs"));
            Objects.requireNonNull(shown, "RegionState.shown");
            hostHolds("region " + id, tabs, shown);
        }
    }

    /**
     * A float: where it lies and how big, in whole pixels of the desk; its tabs in
     * the order its strip shows them, and the one it shows, if any.
     */
    public record FloatState(FloatId id, int x, int y, int w, int h, List<TabId> tabs, Optional<TabId> shown) {
        public FloatState {
            Objects.requireNonNull(id, "FloatState.id");
            if (w <= 0 || h <= 0) throw new IllegalArgumentException("FloatState " + id + " " + w + "×" + h + " — a measure above zero");
            tabs = List.copyOf(Objects.requireNonNull(tabs, "FloatState.tabs"));
            Objects.requireNonNull(shown, "FloatState.shown");
            hostHolds("float " + id, tabs, shown);
        }
    }

    /** What a host's tabs must be, said once: each once, and the one shown among them. */
    private static void hostHolds(String host, List<TabId> tabs, Optional<TabId> shown) {
        var seen = new HashSet<TabId>();
        for (TabId t : tabs) if (!seen.add(t)) throw new IllegalArgumentException("the " + host + " holds " + t + " twice");
        shown.ifPresent(s -> {
            if (!seen.contains(s)) throw new IllegalArgumentException("the " + host + " shows " + s + ", which it does not hold");
        });
    }

    public GridState {
        Objects.requireNonNull(layout, "GridState.layout");
        regions = List.copyOf(Objects.requireNonNull(regions, "GridState.regions"));
        floats = List.copyOf(Objects.requireNonNull(floats, "GridState.floats"));
        tabs = List.copyOf(Objects.requireNonNull(tabs, "GridState.tabs"));
        Layout.checked(layout);
        var cells = Layout.regions(layout);
        var held = regions.stream().map(RegionState::id).toList();
        if (!cells.equals(held)) throw new IllegalArgumentException("GridState — the regions " + held + " are not the layout's " + cells);
        var floatIds = new HashSet<FloatId>();
        for (FloatState f : floats) if (!floatIds.add(f.id())) throw new IllegalArgumentException("GridState — the float " + f.id() + " twice");
        var open = new HashSet<TabId>();
        for (TabState t : tabs) if (!open.add(t.id())) throw new IllegalArgumentException("GridState — the tab " + t.id() + " twice");
        Map<TabId, String> where = new HashMap<>();
        for (RegionState r : regions) hosts(where, open, r.tabs(), "region " + r.id());
        for (FloatState f : floats) hosts(where, open, f.tabs(), "float " + f.id());
        for (TabId t : open) if (!where.containsKey(t)) throw new IllegalArgumentException("GridState — the tab " + t + " is in no host");
        var inHosts = new ArrayList<TabId>();
        for (RegionState r : regions) inHosts.addAll(r.tabs());
        for (FloatState f : floats) inHosts.addAll(f.tabs());
        var listed = tabs.stream().map(TabState::id).toList();
        if (!inHosts.equals(listed)) throw new IllegalArgumentException("GridState — the tabs " + listed + " are not in the order the hosts hold them, " + inHosts);
    }

    private static void hosts(Map<TabId, String> where, HashSet<TabId> open, List<TabId> tabs, String host) {
        for (TabId t : tabs) {
            if (!open.contains(t)) throw new IllegalArgumentException("GridState — the " + host + " holds " + t + ", which is not open");
            String was = where.put(t, host);
            if (was != null) throw new IllegalArgumentException("GridState — the tab " + t + " is in the " + was + " and the " + host);
        }
    }

    /** The region the workspace opens with, before anything is logged: one, named {@code main}. */
    public static final String OPENING_REGION = "main";

    /** Where every log starts: one region, empty; no float; no tab. */
    public static GridState opening() {
        var main = RegionId.of(OPENING_REGION);
        return new GridState(new Layout.Cell(main), List.of(new RegionState(main, List.of(), Optional.empty())), List.of(), List.of());
    }
}
