// =============================================================================
// WorkspacePanesModule — the workspace's panes: a SplitGrid of cells, one dock
// per cell, a desk over all of them.
//
//   new WorkspacePanes(branch, { host, layout, budget?, onEvent?, keyboard?,
//                                keyboardId?, menus?, stripMenu?, keys? })
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

/**
 * The same tree, in the two spellings it has. The workspace says leaf/slotId
 * and holds a sub-tree under `pane`; the grid says cell/id and holds one under
 * `node`. Neither is better and there is no case where both are right, so they
 * meet here, at the one place the two languages touch, rather than either side
 * learning the other's words.
 */
function _toGrid(n) {
    if (!n) return null;
    if (n.kind === "leaf") return { kind: "cell", id: n.slotId };
    return { kind: "split", orientation: n.orientation,
             children: n.children.map(function (c) { return { node: _toGrid(c.pane), ratio: c.ratio }; }) };
}

function _fromGrid(n) {
    if (!n) return null;
    if (n.kind === "cell") return { kind: "leaf", slotId: n.id };
    return { kind: "split", orientation: n.orientation,
             children: n.children.map(function (c) { return { pane: _fromGrid(c.node), ratio: c.ratio }; }) };
}

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
        this._hosts = new Map();       // tabId  → the element the widget mounts in
        this._tabObjs = new Map();     // tabId  → the descriptor the holder gave us

        this._grid = new SplitGrid(branch.createBranch("grid"), {
            host: opts.host,
            layout: _toGrid(opts.layout) || { kind: "cell", id: "main" },
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

    _fire(ev) {
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

    layout() { return _fromGrid(this._grid.layout()); }

    /**
     * THE ONE SEAM BETWEEN THE TWO PANES. The component takes a WIDGET and
     * puts widget.root in a panel of its own; the studio's gave out a bare
     * content element and let the caller mount into it afterwards. The shell
     * still works the second way - add the tab, ask where it went, mount - so
     * a tab arrives here with no widget on it and this gives it one: a host of
     * its own, which is the element contentElOf then hands back.
     */
    addTab(slotId, tab) {
        var pane = this._panes.get(slotId);
        return pane ? pane.addTab(this._withHost(pane, tab)) : -1;
    }

    _withHost(pane, tab) {
        if (tab.widget && tab.widget.root) return tab;
        var safe = String(tab.id).replace(/[^A-Za-z0-9_-]/g, "_");
        var host = this._branch.createElement("host-" + safe, "div");
        css.addClass(host, wp_host);
        // -1: not in the Tab order, but able to take focus when the dock rests
        // it here. A tab is reached by its chip or by the pane's keys, never by
        // tabbing into the middle of a workspace.
        host.setAttribute("tabindex", "-1");
        this._hosts.set(tab.id, host);

        // THE LAW AT THE DOOR: a dock takes a widget that joins its focus
        // branch and answers activate(). The shell mounts a widget into this
        // host afterwards and the dock never sees that one, so the membership
        // is the host's - which is right, because the room is what the dock
        // hands the keys to and what is in the room is the room's business.
        //
        // JOIN, not createBranch: a leaf is what this is. createBranch is for a
        // container that will hold members of its own, and returns the BRANCH;
        // join returns the MEMBERSHIP, which is what carries leave() and in.
        var widget = { root: host };
        widget.focus = pane.focus.join(safe, widget);
        widget.activate = function () { if (host.focus) host.focus(); };
        widget.dispose = function () { try { widget.focus.leave(); } catch (e) {} };

        // AND THE OTHER HALF OF THE SAME SEAM. The studio's pane called
        // tab.render(el) to let a tab fill the element it had been given; the
        // component asks for a root and calls nothing. The shell still writes
        // render, and everything it mounts afterwards goes where render was
        // told - so the host is handed to it here, and the tab fills the room
        // exactly as it always did.
        if (typeof tab.render === "function") {
            try { tab.render(host); }
            catch (e) { console.error("[WorkspacePanes] tab '" + tab.id + "' render threw", e); }
        }

        // ONE object, not a copy. The holder keeps a reference and retitles it,
        // reads its widgetInstanceUuid back, hangs an onClose on it - all of
        // which the studio's pane allowed because it held the very object it
        // was given. A copy here would be a second truth that drifts on the
        // first retitle.
        tab.widget = widget;
        this._tabObjs.set(tab.id, tab);
        return tab;
    }

    removeTab(slotId, tabId) {
        var pane = this._panes.get(slotId);
        this._hosts.delete(tabId);
        this._tabObjs.delete(tabId);
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

    /**
     * By tab id alone, because the shell has one and not the pane it is in.
     * The host this gave the tab, so the widget lands inside its own root
     * rather than beside it in the panel.
     */
    contentElOf(tabId) {
        var host = this._hosts.get(tabId);
        if (host) return host;
        var found = null;
        this._panes.forEach(function (pane) {
            if (found) return;
            var el = pane.contentElOf(tabId);
            if (el) found = el;
        });
        return found;
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
        return { layout: _fromGrid(this._grid.layout()), tabs: tabs };
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

    /** A new, empty pane beside this one; the grid reports Subdivided. */
    split(slotId, side) { return this._grid.subdivide(slotId, side); }

    /** This pane goes, its room toward the pane named; the grid reports Removed. */
    merge(slotId, toward) { return this._grid.remove(slotId, toward); }

    /** The panes this one could merge with: across a whole divider, either side. */
    mergeableWith(slotId) { return this._grid.splitters(slotId) || []; }

    setRatios(path, ratios) { this._grid.setRatios(path, ratios); return this; }

    seam(on) { this._grid.seam(on); return this; }

    /**
     * A tab's chip says its new name.
     *
     * REACHING, and knowingly: the strip draws a chip's label once, from the
     * title it was handed, and the pane offers no way to say a title changed.
     * That belongs on the pane - retitle(id, title) beside switchTab - and
     * until it is there this finds the label and writes it, which is the one
     * place in this file that knows what a chip is made of.
     */
    retitle(slotId, tabId, title) {
        var pane = this._panes.get(slotId);
        if (!pane) return this;
        var tab = this._tabObjs.get(tabId);
        if (tab) tab.title = title;
        var chip = pane.chipOf(pane.tabIndexOf(tabId));
        if (!chip) return this;
        chip.title = title;
        var label = chip.querySelector("[class*='mtp-chip-label']");
        if (label) label.textContent = title;
        return this;
    }

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
