// =============================================================================
// GridArrangement — a split grid's arrangement of a workspace, laid out on a
// workspace that has nothing yet: its first state. The frame is parted from the
// grid's one region — each split's parts to the right, or below — and each
// split re-shared by its parts' weights; then the widgets are opened region by
// region in the frame's order, each region's tabs in its strip's order, the one
// it shows in front. All of it through the grid and the core, as a user's own
// acts go, so the log keeps it as it keeps theirs, and a page coming back to
// the workspace comes back to it from the log alone.
//
// The arrangement is generated in Java from its declaration (GridArrangementJs,
// from an Arrangement<W, SplitGrid>):
//   { engine: "split-grid", workspace,
//     widgets: { [ref]: { kind, params } },
//     frame: { kind: "region", name, tabs: [ref], shown: ref | null }
//          | { kind: "split", axis: "horizontal" | "vertical", parts: [{ weight, frame }] } }
//
//   GridArrangement.apply(ws, arrangement) → { regions: { [name]: region id }, opened: [widget id] }
//     ws  a GridWorkspace with no widget, and one region: refused otherwise
// =============================================================================

class GridArrangement {

    /** The engine an arrangement it lays out is for. */
    static ENGINE = "split-grid";

    static apply(ws, arrangement) {
        var a = arrangement;
        if (!a || a.engine !== GridArrangement.ENGINE) throw new Error("[GridArrangement] a split grid's arrangement is required, not one for " + JSON.stringify(a && a.engine));
        var regions = ws.docks.regions();
        if (regions.length !== 1 || ws.core.entries().length) throw new Error("[GridArrangement] the workspace is not empty: an arrangement is its first state");
        var names = {}, opened = [];
        GridArrangement._lay(ws.docks, regions[0].id, a.frame, names);
        GridArrangement._regions(a.frame).forEach(function (r) {
            r.tabs.forEach(function (ref) {
                var w = a.widgets[ref];
                var entry = ws.open(w.kind, w.params, { slotId: names[r.name], how: ref === r.shown ? "front" : "quiet" });
                if (entry) opened.push(entry.id);
            });
        });
        return Object.freeze({ regions: Object.freeze(names), opened: Object.freeze(opened) });
    }

    /** A frame laid in a region: a region named so; a split parted from it, re-shared, and each part laid in its own. */
    static _lay(docks, id, frame, names) {
        if (frame.kind === "region") { names[frame.name] = id; return; }
        var side = frame.axis === "horizontal" ? "right" : "bottom", ids = [id], at = id;
        for (var i = 1; i < frame.parts.length; i++) { at = docks.part(at, side).id; ids.push(at); }
        var weights = frame.parts.map(function (p) { return p.weight; });
        var found = GridArrangement._splitOf(docks.grid.layout(), ids, "");
        if (found && !GridArrangement._shared(found.node, weights)) docks.grid.setRatios(found.path, weights);
        frame.parts.forEach(function (p, j) { GridArrangement._lay(docks, ids[j], p.frame, names); });
    }

    /** The split whose children are those cells, in order, and its path: "" the root, then child indices. */
    static _splitOf(node, ids, path) {
        if (node.kind !== "split") return null;
        var kids = node.children;
        if (kids.length === ids.length && kids.every(function (c, i) { return c.node.kind === "cell" && c.node.id === ids[i]; })) return { node: node, path: path };
        for (var i = 0; i < kids.length; i++) {
            var found = GridArrangement._splitOf(kids[i].node, ids, path === "" ? String(i) : path + "/" + i);
            if (found) return found;
        }
        return null;
    }

    /** Whether the split is shared so already - a part made equal by parting, left as it is. */
    static _shared(node, weights) {
        var sum = weights.reduce(function (s, w) { return s + w; }, 0);
        return node.children.every(function (c, i) { return typeof c.ratio === "number" && Math.abs(c.ratio - weights[i] / sum) < 1e-9; });
    }

    /** The regions, in the frame's order: left to right, top to bottom, depth first. */
    static _regions(frame) {
        if (frame.kind === "region") return [frame];
        return frame.parts.reduce(function (out, p) { return out.concat(GridArrangement._regions(p.frame)); }, []);
    }
}
