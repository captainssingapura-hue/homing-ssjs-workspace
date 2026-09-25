// =============================================================================
// WorkspacePanesModule — the workspace's panes: a SplitGrid of cells, one dock
// per cell, a desk over all of them.
//
//   new WorkspacePanes(branch, { host, layout, budget?, onEvent?, keyboard?,
//                                keyboardId?, menus?, stripMenu?, keys?,
//                                seam?, thickness?, parties? })
//     seam       the lattice drawn - every splitter and the grid's edge; on
//                unless said false
//     thickness  its lines, in pixels; 1 unless said
//     parties    the parties the workspace exposes to its widgets, on every room's host
//
// A TAB'S NAME AND ICON are the tab-pane's, and this assembly is the tab-pane:
//   panes.retitle(tabId, title)   the tab's name, wherever it is - its dock's chip, or the
//                                 floating pane's head. A widget says it through its room's
//                                 host (host.title), and it arrives here.
//   panes.setIcon(tabId, icon)    the tab's icon, a widget kind's as the workspace declares
//                                 it ({ kind, value }), shown as an element made and kept here
//
// WHAT THIS REPLACES. The studio's MultiTabPane was a pane that SPLIT ITSELF:
// one object owning a tree of leaves, a strip per leaf, the dividers, the drag
// between strips, the selection paint and ten on* callbacks. Three components
// do that now, each minding one thing — the grid arranges cells and never
// knows what a cell holds, a dock holds tabs and never knows where it sits,
// the desk carries what floats — and each reports on ONE sink as frozen data.
//
// So this is not a port. It is the assembly, and the surface the shell already
// calls is kept deliberately: paneIdOf, addTab, switchTab, removeTab, getState,
// contentElOf, tabIndexOf, neighbourOf, setAddEnabled, split, merge. The shell
// keeps calling what it called; underneath, every one of them is now somebody's
// single job.
//
// A PANE IS ITS ID. paneIdOf used to walk the tree and build "_1_2" — the path
// to a leaf, handed on as though it were a name, which is why the split events
// carried a path where they meant a pane. A cell's id IS the pane's id, so it
// answers itself, and the events say what they mean.
//
// WHAT IS NOT HERE, and is not missing: the selection paint. Keys are design
// states the component wears from data-keys (RFC 0066 E3 §16), so a holder
// does not paint who has them. The paint* calls are kept as no-ops while the
// focus coordinator still makes them, and go with it.
// =============================================================================

const _owner = Object.freeze({ toString: () => "workspacePanes" });

/** direction → how splitters() names the same neighbour. */
const _ACROSS = Object.freeze({
    left:  { axis: "horizontal", side: "before" },
    right: { axis: "horizontal", side: "after"  },
    up:    { axis: "vertical",   side: "before" },
    down:  { axis: "vertical",   side: "after"  }
});

class WorkspacePanes {

    constructor(branch, opts) {
        if (!branch) throw new Error("[WorkspacePanes] a branch of its own is required");
        if (!opts || !opts.host) throw new Error("[WorkspacePanes] opts.host is required");
        var self = this;
        branch.activate(_owner);
        this._branch = branch;
        this._opts = opts;
        this._sink = typeof opts.onEvent === "function" ? opts.onEvent : null;
        this._panes = new Map();       // cellId → MultiTabPane
        this._tabObjs = new Map();     // tabId  → the tab the holder gave us
        this._floated = new Map();     // tabId  → { slotId, index } it left
        this._icons = new Map();       // tabId  → { branch, el }: the icon the tab shows
        this._iconSeq = 0;
        this._merging = null;          // { slotId, toward } while a merge carries its tabs across
        this._roomSeq = 0;
        this._cellSeq = 0;

        var layout = WorkspaceGrid.toGrid(opts.layout) || { kind: "cell", id: "main" };
        // Every cell id this assembly has known. A cell's name is taken for good
        // once minted - the grid keeps its element's name after the cell goes -
        // so a new pane is never given the id of one that went.
        this._cellIds = new Set(SplitGridTree.cells(layout));

        // The seam: a workspace is flat, and flat is not featureless - a
        // hairline says where one pane ends and the next begins, which the
        // panes' own edges are too pale to say against the ground.
        this._grid = new SplitGrid(branch.createBranch("grid"), {
            host: opts.host,
            layout: layout,
            seam: opts.seam !== false,
            thickness: opts.thickness == null ? 1 : opts.thickness,
            minCellPx: 160,
            onEvent: function (ev) { self._fire(ev); self._sync(); }
        });

        // The desk over the grid: a tab pulled off a strip floats, and lands on
        // whichever strip it is dropped over. The docks are the cells' panes.
        this._docking = new Docking(branch.createBranch("docking"), {
            host: opts.host,
            keyboard: opts.keyboard || null,
            keyboardId: opts.keyboardId ? opts.keyboardId + "/desk" : null,
            onEvent: function (ev) { self._fire(ev); }
        });

        this._sync();
    }

    /** Every event on to the holder, with what only the assembly knows filled in: WorkspacePaneEvents. */
    _fire(ev) { WorkspacePaneEvents.fire(this, ev); }

    _emit(ev) {
        if (!this._sink) return;
        try { this._sink(ev); }
        catch (e) { console.error("[WorkspacePanes] onEvent threw on " + ev.kind + ":", e); }
    }

    /** A dock for every cell, and none for a cell that has gone. */
    _sync() {
        var self = this;
        var live = this._grid.cells();
        for (var i = 0; i < live.length; i++) {
            if (!this._panes.has(live[i])) this._panes.set(live[i], this._dock(live[i]));
        }
        this._panes.forEach(function (pane, id) {
            if (live.indexOf(id) >= 0) return;
            self._docking.removeDock(pane);
            try { pane.dispose(); } catch (e) {}
            self._panes.delete(id);
        });
    }

    /** One dock, in one cell, knowing only that it is a dock. */
    _dock(cellId) {
        var self = this;
        var o = this._opts;
        var pane = new MultiTabPane(this._branch.createBranch("pane-" + cellId), {
            host: this._grid.cell(cellId),
            slotId: cellId,
            budget: o.budget,
            keys: o.keys,
            menus: o.menus || null,
            stripMenu: o.stripMenu || null,
            onEvent: function (ev) { self._fire(ev); }
        });
        if (o.keyboard && typeof pane.keyboard === "function") {
            pane.keyboard(o.keyboard, o.keyboardId ? o.keyboardId + "/" + cellId : cellId);
        }
        this._docking.addDock(pane);
        return pane;
    }

    // ── The surface the shell calls ─────────────────────────────────────────

    /** A pane is its id. Nothing to walk. */
    paneIdOf(slotId) { return this._panes.has(slotId) ? slotId : null; }

    slotIds() { return this._grid.cells(); }

    paneAt(slotId) { return this._panes.get(slotId) || null; }

    layout() { return WorkspaceGrid.fromGrid(this._grid.layout()); }

    /**
     * A room for a tab in this pane: the PANE half of a tab-pane, a member of
     * this dock's focus branch, on a branch of the assembly's own so it
     * travels with its tab. The holder puts a widget in it and adds the tab.
     */
    roomFor(slotId) {
        var pane = this._panes.get(slotId);
        if (!pane) return null;
        var self = this;
        var room = new WidgetPane(this._branch.createBranch("room" + (++this._roomSeq)), {
            focus: pane.focus,
            parties: this._opts.parties || {},
            // the widget named itself: its tab, wherever the tab is, says so
            onTitle: function (text) {
                self._tabObjs.forEach(function (tab) { if (tab.widget === room) self.retitle(tab.id, text); });
            }
        });
        return room;
    }

    /**
     * A tab carries its room as its widget, and the room keeps the dock's law
     * - so the dock is handed exactly what it asks for, and nothing is made up
     * here to get past its door.
     */
    addTab(slotId, tab) {
        var pane = this._panes.get(slotId);
        if (!pane) return -1;
        if (!tab.widget || !tab.widget.root || !tab.widget.focus) {
            throw new Error("[WorkspacePanes] tab '" + tab.id + "' carries no room: roomFor(slot), setWidget, then addTab");
        }
        this._tabObjs.set(tab.id, tab);
        return pane.addTab(tab);
    }

    /** The tab goes out of the table when its dock reports it gone. */
    removeTab(slotId, tabId) {
        var pane = this._panes.get(slotId);
        return pane ? pane.removeTab(tabId) : null;
    }

    switchTab(slotId, tabId) {
        var pane = this._panes.get(slotId);
        if (pane) pane.switchTab(tabId);
        return this;
    }

    tabIndexOf(slotId, tabId) {
        var pane = this._panes.get(slotId);
        return pane ? pane.tabIndexOf(tabId) : -1;
    }

    /** Where a tab's widget lives: its room's root. */
    contentElOf(tabId) {
        var tab = this._tabObjs.get(tabId);
        return tab && tab.widget ? tab.widget.root : null;
    }

    /** A tab's room, for a holder that changes what is in it. */
    roomOf(tabId) {
        var tab = this._tabObjs.get(tabId);
        return tab ? tab.widget : null;
    }

    /**
     * The descriptor a tab was made from. The holder needs it back to retitle,
     * to hang an onClose, to read the widget id it put there. It used to reach
     * into the pane's own _tabsBySlot for this, which is why picking a widget
     * stopped working the moment the pane behind it changed.
     */
    tabOf(tabId) { return this._tabObjs.get(tabId) || null; }

    /** Which pane holds this tab, or null. */
    slotOf(tabId) {
        var found = null;
        this._panes.forEach(function (pane, id) {
            if (!found && pane.tabIndexOf(tabId) >= 0) found = id;
        });
        return found;
    }

    /** The old shape: { layout, tabs: { slotId: { tabs, activeTabId } } }. */
    getState() {
        var tabs = {};
        this._panes.forEach(function (pane, id) {
            var s = pane.getState();
            tabs[id] = { tabs: s.tabs.map(function (t) { return { id: t.id, title: t.title }; }),
                         activeTabId: s.activeTabId };
        });
        return { layout: WorkspaceGrid.fromGrid(this._grid.layout()), tabs: tabs };
    }

    setAddEnabled(on) {
        this._panes.forEach(function (pane) { pane.setAddEnabled(on); });
        return this;
    }

    /**
     * The pane across a WHOLE divider from this one in that direction — one
     * pane alone on each side. The grid answers it, because the grid is what
     * knows; the old pane measured every leaf's rectangle and compared gaps.
     */
    neighbourOf(fromSlot, direction) {
        var want = _ACROSS[direction];
        if (!want) return null;
        var across = this._grid.splitters(fromSlot) || [];
        for (var i = 0; i < across.length; i++) {
            if (across[i].axis === want.axis && across[i].side === want.side) return across[i].id;
        }
        return null;
    }

    /** A tab off its dock and onto the desk, at a point: the float. */
    undockAt(pane, tab, at) {
        var from = { slotId: pane.slotId, index: pane.tabIndexOf(tab.id) };
        this._docking.undockAt(pane, tab, at);
        this._floated.set(tab.id, from);
        return this;
    }

    /** A new, empty pane beside this one, under a name no pane has had; the grid reports Subdivided. */
    split(slotId, side) {
        var id;
        do { id = "cell-" + (++this._cellSeq); } while (this._cellIds.has(id));
        this._cellIds.add(id);
        return this._grid.subdivide(slotId, side, id);
    }

    /**
     * This pane goes, and everything in it goes where its room goes: its tabs
     * to the pane that gains the room - the one named, when it faces this one
     * across a whole divider, else the one the grid leans to - appended in
     * order, that pane still showing what it showed. Then the cell, which the
     * grid reports Removed, carrying the pane named so a replay goes the same
     * way. The last pane cannot go.
     */
    merge(slotId, toward) {
        var heir = (this._grid.heirs(slotId, toward) || [])[0];
        var from = this._panes.get(slotId), to = heir ? this._panes.get(heir) : null;
        if (!from || !to) return null;
        this._merging = { slotId: slotId, toward: toward || null };
        try {
            var ids = from.tabs();
            for (var i = 0; i < ids.length; i++) to.attachTab(from.detachTab(ids[i]), null);
            return this._grid.remove(slotId, toward);
        } finally {
            this._merging = null;
        }
    }

    /** The panes this one could merge with: across a whole divider, either side. */
    mergeableWith(slotId) { return this._grid.splitters(slotId) || []; }

    setRatios(path, ratios) { this._grid.setRatios(path, ratios); return this; }

    seam(on) { this._grid.seam(on); return this; }

    /** A tab's name, now, wherever the tab is — its dock's chip, or the floating head: WorkspaceTabNames. */
    retitle(tabId, title) { WorkspaceTabNames.retitle(this, tabId, title); return this; }

    /** A tab's icon: a widget kind's ({ kind, value }), or null for none: WorkspaceTabNames. */
    setIcon(tabId, icon) { WorkspaceTabNames.setIcon(this, tabId, icon); return this; }

    /** A tab gone: its icon with it. */
    _forgetIcon(tabId) { WorkspaceTabNames.forget(this, tabId); }

    /** The tab a pane is showing, or null. */
    activeTabOf(slotId) {
        var pane = this._panes.get(slotId);
        return pane ? pane.activeTab() : null;
    }

    /**
     * Rest the keys in a pane's showing tab. What the coordinator called
     * entering deep, said by the party: the dock blurs whatever had native
     * focus and activates the widget it is showing.
     */
    land(slotId, tabId) {
        var pane = this._panes.get(slotId);
        if (!pane) return this;
        if (tabId != null && pane.tabIndexOf(tabId) >= 0) pane.switchTab(tabId);
        if (typeof pane.land === "function") pane.land(pane.activeTab());
        return this;
    }

    dispose() {
        this._panes.forEach(function (pane) { try { pane.dispose(); } catch (e) {} });
        this._panes.clear();
        try { this._docking.dispose(); } catch (e) {}
        try { this._grid.dispose(); } catch (e) {}
        try { this._branch.dissolve(); } catch (e) {}
    }
}
