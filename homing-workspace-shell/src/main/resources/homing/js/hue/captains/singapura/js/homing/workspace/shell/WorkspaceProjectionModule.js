// =============================================================================
// WorkspaceProjection — a WorkspaceState and a live workspace, both ways: the
// state laid out, and the workspace read back into a state. The round trip is
// the proof: restored from a state, a workspace reads back as that state, byte
// for byte.
//
//   WorkspaceProjection.gridLayout(layout)   → the grid's own tree for the log's Layout:
//                                              each share, whole millionths, as a ratio
//   WorkspaceProjection.logLayout(tree)      → the log's Layout for the grid's tree: each
//                                              split's ratios as whole millionths, as the
//                                              recorder writes them
//   WorkspaceProjection.restore(ws, state)   the tabs back where the state has them: every
//                                            region's in its order, under their ids, kinds
//                                            and titles, and the tab each region shows; then
//                                            every float, bottom first, under its name, where
//                                            it lay and as big, its tabs likewise.
//                                            The grid is the workspace's to lay out, from
//                                            gridLayout, when it is made
//   WorkspaceProjection.read(ws)             → the WorkspaceState the workspace shows now
//   WorkspaceProjection.same(a, b)           → whether two states are the same, byte for byte
// =============================================================================

class WorkspaceProjection {
    static gridLayout(layout) {
        if (layout instanceof Cell) return { kind: "cell", id: layout.region.value };
        return { kind: "split", orientation: layout.axis === Axis.HORIZONTAL ? "horizontal" : "vertical",
                 children: layout.tracks.map(function (t) { return { node: WorkspaceProjection.gridLayout(t.node), ratio: t.share.units / 1000000 }; }) };
    }

    static logLayout(tree) {
        if (tree.kind === "cell") return new Cell(new RegionId(tree.id));
        var shares = WorkspaceRecorder.shares(tree.children.map(function (c) { return c.ratio; }));
        return new Split(tree.orientation === "horizontal" ? Axis.HORIZONTAL : Axis.VERTICAL,
                         tree.children.map(function (c, i) { return new Track(WorkspaceProjection.logLayout(c.node), shares[i]); }));
    }

    static restore(ws, state) {
        var kinds = new Map();
        state.tabs.forEach(function (t) { kinds.set(t.id.value, t); });
        function fill(host, h) {
            h.tabs.forEach(function (id) {
                var t = kinds.get(id.value);
                if (!ws.source.has(t.kind.value)) { console.error("[WorkspaceProjection] tab '" + id.value + "' holds a kind this page does not know: " + t.kind.value); return; }
                ws.source.add(host, t.kind.value, "quiet", { id: t.id.value, title: t.title.value });
            });
            if (h.shown && host.has(h.shown.value)) host.switchTab(h.shown.value);
        }
        state.regions.forEach(function (r) {
            var region = ws.docks.region(r.id.value);
            if (!region) { console.error("[WorkspaceProjection] no region '" + r.id.value + "' to restore into"); return; }
            fill(region.dock, r);
        });
        state.floats.forEach(function (f) { fill(ws.desk.float({ id: f.id.value, x: f.x, y: f.y, w: f.w, h: f.h }).host, f); });
    }

    static read(ws) {
        var layout = WorkspaceProjection.logLayout(ws.docks.grid.layout()), tabs = [];
        var regions = _cellsOf(layout).map(function (id) {
            var dock = ws.docks.region(id).dock, ids = dock.tabs(), active = dock.activeTab();
            ids.forEach(function (tabId) { tabs.push(_tabState(ws, tabId)); });
            return new RegionState(new RegionId(id), ids.map(function (x) { return new TabId(x); }), active ? new TabId(active) : null);
        });
        var floats = ws.desk.floats().map(function (f) {
            var b = f.frame.bounds(), ids = f.host.tabs(), active = f.host.activeTab();
            ids.forEach(function (tabId) { tabs.push(_tabState(ws, tabId)); });
            return new FloatState(new FloatId(f.id), b.x, b.y, b.w, b.h, ids.map(function (x) { return new TabId(x); }), active ? new TabId(active) : null);
        });
        return new WorkspaceState(layout, regions, floats, tabs);
    }

    static same(a, b) {
        return JSON.stringify(WorkspaceStateCodec.transformTo(a)) === JSON.stringify(WorkspaceStateCodec.transformTo(b));
    }
}

function _tabState(ws, tabId) {
    return new TabState(new TabId(tabId), new WidgetKind(ws.source.kindOf(tabId)), new WidgetTitle(ws.desk.register.get(tabId).title()));
}

function _cellsOf(l) {
    if (l instanceof Cell) return [l.region.value];
    var out = [];
    l.tracks.forEach(function (t) { out = out.concat(_cellsOf(t.node)); });
    return out;
}
