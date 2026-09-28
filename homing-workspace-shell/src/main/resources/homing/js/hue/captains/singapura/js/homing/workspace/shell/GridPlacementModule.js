// =============================================================================
// GridPlacement — the split grid as a workspace's placement (RFC 0066 E3, the
// workspace detour: requests). It mounts a widget's tab-pane in a HOST — a
// region's dock, or a float — and unmounts it, and never creates or closes
// one: the tab-panes are the register's (WidgetTabs), lent and closed with
// their widgets by the core. What a tab does once it is in the grid — moved,
// floated, shown — is the desk's and the docks' own, and they report it.
//
// A mount is a move into the host, which the desk reports (TabAdded). An
// UNMOUNT is the placement's alone — the host did not start it, so the host
// says nothing — and so the placement says it, as the host would have:
// TabRemoved where it was; then the tab-pane is taken out.
//
// It comes back from its own state, the grid's layer of the log (GridState),
// the roster already back — every widget made again under its id, its
// tab-pane in no host. It mounts each where the state has it: every region's
// tabs in order and the one it shows; every float, bottom first, under its
// name, where it lay and as big, its tabs likewise. A tab the roster does not
// hold cannot come back: a STRAY, said. letGo says the strays gone as the
// grid's events — each closed; each host they were in showing what it shows
// now; a float they leave empty, closed.
//
//   new GridPlacement({ desk, docks, tabs, onEvent })
//     desk, docks   the Desk and its DockGrid
//     tabs          the register: tabs.tab(id) → the widget's tab-pane
//     onEvent       the sink the desk and the docks report on
//   placement.mount(entry, location)   { slotId?, index?, how? }: the first region unless said,
//                                      at the end unless said; how "quiet", "front" (unless said)
//                                      or "focus" — shown, and its widget handed the keys  (the core's port)
//   placement.unmount(entry)                                                             (the core's port)
//   placement.host(slotId) → the region's dock or the float's host, or null
//   placement.unplaced() → the ids of the widgets whose tab is in no host
//   placement.restore(state) → { strays: [{ id, slotId, index }] }   index: where the log had it
//   placement.letGo(strays)
// =============================================================================

var _HOWS = Object.freeze(["quiet", "front", "focus"]);

class GridPlacement {
    constructor(opts) {
        var o = opts || {};
        if (!o.desk || !o.docks || !o.tabs) throw new Error("[GridPlacement] opts.desk, opts.docks and opts.tabs are required");
        this._desk = o.desk;
        this._docks = o.docks;
        this._tabs = o.tabs;
        this._sink = typeof o.onEvent === "function" ? o.onEvent : function () {};
    }

    mount(entry, location) {
        var l = location || {}, how = l.how == null ? "front" : String(l.how);
        if (_HOWS.indexOf(how) < 0) throw new Error("[GridPlacement] '" + how + "' is not how a widget is mounted: " + _HOWS.join(", "));
        var host = l.slotId == null ? this._first() : this.host(l.slotId);
        if (!host) throw new Error("[GridPlacement] no region or float '" + l.slotId + "' to mount '" + entry.id + "' in");
        var tp = this._tabs.tab(entry.id);
        if (!tp) throw new Error("[GridPlacement] '" + entry.id + "' has no tab-pane");
        this._desk.move(tp, host, l.index == null ? null : l.index);
        if (how !== "quiet") this._desk.show(tp);
        if (how === "focus") tp.widget.activate();
    }

    unmount(entry) {
        var tp = this._tabs.tab(entry.id), host = tp ? tp.host() : null;
        if (!host) return;
        this._sink(PaneEvents.TabRemoved(host.slotId, tp, host.tabIndexOf(tp.id)));
        this._desk.unmount(tp);
    }

    host(slotId) { return this._desk.docks().filter(function (d) { return d.slotId === slotId; })[0] || null; }

    unplaced() {
        var desk = this._desk;
        return desk.register.ids().filter(function (id) { return !desk.register.get(id).host(); });
    }

    restore(state) {
        var self = this, strays = [];
        function fill(host, h) {
            h.tabs.forEach(function (id, i) {
                var tp = self._tabs.tab(id.value);
                if (tp && !tp.host()) self._desk.move(tp, host);
                else strays.push(Object.freeze({ id: id.value, slotId: host.slotId, index: i }));
            });
            if (h.shown && host.has(h.shown.value)) host.switchTab(h.shown.value);
        }
        state.regions.forEach(function (r) {
            var region = self._docks.region(r.id.value);
            if (region) fill(region.dock, r);
            else console.error("[GridPlacement] no region '" + r.id.value + "' to restore into");
        });
        state.floats.forEach(function (f) { fill(self._desk.float({ id: f.id.value, x: f.x, y: f.y, w: f.w, h: f.h }).host, f); });
        if (strays.length) console.warn("[GridPlacement] tabs the roster does not hold do not come back: " + strays.map(function (s) { return s.id; }).join(", "));
        return Object.freeze({ strays: strays });
    }

    letGo(strays) {
        var self = this, hosts = [];
        strays.forEach(function (s) {
            self._sink(PaneEvents.TabRemoved(s.slotId, { id: s.id }, s.index));
            if (hosts.indexOf(s.slotId) < 0) hosts.push(s.slotId);
        });
        hosts.forEach(function (slotId) {
            var host = self.host(slotId);
            if (!host) return;
            if (host.activeTab() !== null) { self._sink(PaneEvents.TabActivated(slotId, host.activeTab())); return; }
            var f = self._desk.floats().filter(function (x) { return x.host === host; })[0];
            if (f) f.close();
        });
    }

    _first() { var r = this._docks.regions()[0]; return r ? r.dock : null; }
}
