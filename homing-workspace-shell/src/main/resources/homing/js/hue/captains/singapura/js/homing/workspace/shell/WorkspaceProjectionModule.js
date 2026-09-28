// =============================================================================
// WorkspaceProjection — the split grid's layer of a WorkspaceState — a GridState —
// and a live workspace: the state's layout for the grid, and the workspace read
// back into a state. The round trip is the proof: placed as a state has it
// (GridPlacement.restore), a workspace reads back as that state, byte for byte.
//
//   WorkspaceProjection.gridLayout(layout)   → the grid's own tree for the log's Layout:
//                                              each share, whole millionths, as a ratio
//   WorkspaceProjection.logLayout(tree)      → the log's Layout for the grid's tree: each
//                                              split's ratios as whole millionths, as the
//                                              recorder writes them
//   WorkspaceProjection.read(ws)             → the GridState the workspace shows now
//   WorkspaceProjection.same(a, b)           → whether two states are the same, byte for byte
//   WorkspaceProjection.untitled(state)      → the state with every tab titled by its kind: the
//                                            placement alone, for a workspace whose titles are
//                                            its widgets', which the grid does not keep
//   WorkspaceProjection.without(state, ids)  → the state with those tabs gone from it: each host
//                                            showing its first when the one it showed went, as a
//                                            host settles; a float kept, though they leave it empty
//
// A workspace read back answers ws.kindOf(tabId): the kind its tab holds.
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
        return new GridState(layout, regions, floats, tabs);
    }

    static same(a, b) {
        return JSON.stringify(GridStateCodec.transformTo(a)) === JSON.stringify(GridStateCodec.transformTo(b));
    }

    static untitled(s) {
        return new GridState(s.layout, s.regions, s.floats, s.tabs.map(function (t) { return new TabState(t.id, t.kind, new WidgetTitle(t.kind.value)); }));
    }

    static without(s, ids) {
        if (!ids || ids.length === 0) return s;
        var gone = new Set(ids);
        function kept(h) {
            var tabs = h.tabs.filter(function (t) { return !gone.has(t.value); });
            return { tabs: tabs, shown: h.shown && !gone.has(h.shown.value) ? h.shown : (tabs[0] || null) };
        }
        return new GridState(s.layout,
            s.regions.map(function (r) { var k = kept(r); return new RegionState(r.id, k.tabs, k.shown); }),
            s.floats.map(function (f) { var k = kept(f); return new FloatState(f.id, f.x, f.y, f.w, f.h, k.tabs, k.shown); }),
            s.tabs.filter(function (t) { return !gone.has(t.id.value); }));
    }
}

function _tabState(ws, tabId) {
    return new TabState(new TabId(tabId), new WidgetKind(ws.kindOf(tabId)), new WidgetTitle(ws.desk.register.get(tabId).title()));
}

function _cellsOf(l) {
    if (l instanceof Cell) return [l.region.value];
    var out = [];
    l.tracks.forEach(function (t) { out = out.concat(_cellsOf(t.node)); });
    return out;
}
