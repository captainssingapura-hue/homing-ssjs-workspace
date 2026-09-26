package hue.captains.singapura.js.homing.workspace.log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * What a workspace log folds to: the arrangement, whole. The layout of the
 * regions; each region's tabs in order and the one it shows; each float's
 * place, measure, tabs and the one it shows, the floats in their stacking order
 * from the bottom; and every open tab — its kind and title — in the order the
 * hosts hold them: the regions' in the layout's order, then the floats' from
 * the bottom, each host's as its strip shows them. Nothing in it is history a
 * live workspace does not show, so a workspace can be read back into one. Every
 * tab is in exactly one host, and every host holds only open tabs: a state that
 * is not so is refused, whatever built it.
 *
 * @param layout  the regions as a tree
 * @param regions one per cell of the layout, in the layout's order
 * @param floats  the floats, bottom first
 * @param tabs    the open tabs, in the order the hosts hold them
 */
public record WorkspaceState(Layout layout, List<RegionState> regions, List<FloatState> floats, List<TabState> tabs) {

    public WorkspaceState {
        Objects.requireNonNull(layout, "WorkspaceState.layout");
        regions = List.copyOf(Objects.requireNonNull(regions, "WorkspaceState.regions"));
        floats = List.copyOf(Objects.requireNonNull(floats, "WorkspaceState.floats"));
        tabs = List.copyOf(Objects.requireNonNull(tabs, "WorkspaceState.tabs"));
        Layout.checked(layout);
        var cells = Layout.regions(layout);
        var held = regions.stream().map(RegionState::id).toList();
        if (!cells.equals(held)) throw new IllegalArgumentException("WorkspaceState — the regions " + held + " are not the layout's " + cells);
        var floatIds = new HashSet<FloatId>();
        for (FloatState f : floats) if (!floatIds.add(f.id())) throw new IllegalArgumentException("WorkspaceState — the float " + f.id() + " twice");
        var open = new HashSet<TabId>();
        for (TabState t : tabs) if (!open.add(t.id())) throw new IllegalArgumentException("WorkspaceState — the tab " + t.id() + " twice");
        Map<TabId, String> where = new HashMap<>();
        for (RegionState r : regions) hosts(where, open, r.tabs(), "region " + r.id());
        for (FloatState f : floats) hosts(where, open, f.tabs(), "float " + f.id());
        for (TabId t : open) if (!where.containsKey(t)) throw new IllegalArgumentException("WorkspaceState — the tab " + t + " is in no host");
        var inHosts = new java.util.ArrayList<TabId>();
        for (RegionState r : regions) inHosts.addAll(r.tabs());
        for (FloatState f : floats) inHosts.addAll(f.tabs());
        var listed = tabs.stream().map(TabState::id).toList();
        if (!inHosts.equals(listed)) throw new IllegalArgumentException("WorkspaceState — the tabs " + listed + " are not in the order the hosts hold them, " + inHosts);
    }

    private static void hosts(Map<TabId, String> where, HashSet<TabId> open, List<TabId> tabs, String host) {
        for (TabId t : tabs) {
            if (!open.contains(t)) throw new IllegalArgumentException("WorkspaceState — the " + host + " holds " + t + ", which is not open");
            String was = where.put(t, host);
            if (was != null) throw new IllegalArgumentException("WorkspaceState — the tab " + t + " is in the " + was + " and the " + host);
        }
    }

    /** The region the workspace opens with, before anything is logged: one, named {@code main}. */
    public static final String OPENING_REGION = "main";

    /** Where every log starts: one region, empty; no float; no tab. */
    public static WorkspaceState opening() {
        var main = RegionId.of(OPENING_REGION);
        return new WorkspaceState(new Layout.Cell(main), List.of(new RegionState(main, List.of(), java.util.Optional.empty())), List.of(), List.of());
    }
}
