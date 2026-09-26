// =============================================================================
// WorkspaceRecorder — what the workspace's components report, written into its
// log: each report that changes the arrangement becomes the WorkspaceEvent it
// is — a class generated from its Java declaration — and goes to the store.
// Nothing here spells an event by hand; a value the class refuses is not
// recorded, and says so.
//
//   new WorkspaceRecorder({ store, isRegion, kindOf, onCount? })
//     store     a WorkspaceLogStore
//     isRegion  (slotId) → whether a host is one of the grid's regions; a float
//               is not, and a tab afloat is, to the log, where it left
//     kindOf    (tabId) → the kind a tab holds: the tab source's
//     onCount   (n) → told how many it has recorded, after each
//   recorder.hear(report)       a desk's, a dock's or the grid's report
//   recorder.became(tp, kindId) a tab became another kind (the source's onBecame)
//   recorder.stop()             nothing more is recorded: before the workspace
//                               is taken down, whose closing is not the user's
//
// The reports, and what each is in the log:
//   TabAdded in a region        TabOpened, with the kind the source made it
//   TabMoved into a region      TabMoved; into a float, nothing
//   TabRemoved                  TabClosed
//   TabActivated in a region    TabShown
//   Subdivided / Removed        RegionParted / RegionRemoved
//   TracksChanged               TracksChanged, each share an exact millionth
// =============================================================================

function _title(tab) { return typeof tab.title === "function" ? tab.title() : tab.title; }

class WorkspaceRecorder {
    constructor(opts) {
        var o = opts || {};
        if (!o.store) throw new Error("[WorkspaceRecorder] opts.store is required");
        if (typeof o.isRegion !== "function" || typeof o.kindOf !== "function") throw new Error("[WorkspaceRecorder] opts.isRegion and opts.kindOf are required");
        this._store = o.store;
        this._isRegion = o.isRegion;
        this._kindOf = o.kindOf;
        this._onCount = typeof o.onCount === "function" ? o.onCount : null;
        this._count = 0;
        this._on = true;
    }

    hear(report) {
        if (!this._on || !report) return;
        var event;
        try { event = this._of(report); }
        catch (e) { console.error("[WorkspaceRecorder] " + report.kind + " is not recorded:", e.message); return; }
        if (event) this._append(event);
    }

    became(tp, kindId) {
        if (!this._on) return;
        try { this._append(new TabBecame(new TabId(tp.id), new WidgetKind(kindId), new WidgetTitle(_title(tp)))); }
        catch (e) { console.error("[WorkspaceRecorder] TabBecame is not recorded:", e.message); }
    }

    stop() { this._on = false; }

    count() { return this._count; }

    _of(r) {
        switch (r.kind) {
            case "TabAdded": {
                if (!this._isRegion(r.slotId)) return null;
                var kind = this._kindOf(r.tab.id);
                if (!kind) throw new Error("tab '" + r.tab.id + "' was not made by the tab source: its kind is not known");
                return new TabOpened(new TabId(r.tab.id), new WidgetKind(kind), new WidgetTitle(_title(r.tab)), new RegionId(r.slotId), r.index);
            }
            case "TabMoved":
                return this._isRegion(r.destSlotId) ? new TabMoved(new TabId(r.tab.id), new RegionId(r.destSlotId), r.destIndex) : null;
            case "TabRemoved":
                return new TabClosed(new TabId(r.tab.id));
            case "TabActivated":
                return this._isRegion(r.slotId) ? new TabShown(new RegionId(r.slotId), new TabId(r.tabId)) : null;
            case "Subdivided":
                return new RegionParted(new RegionId(r.cellId), new RegionId(r.newCellId), Side.of(String(r.side).toUpperCase()));
            case "Removed":
                return new RegionRemoved(new RegionId(r.cellId));
            case "TracksChanged":
                return new TracksChanged(new SplitPath(r.path), WorkspaceRecorder.shares(r.ratios));
            default:
                return null;
        }
    }

    /** A split's ratios as exact millionths adding up to one: each rounded, the largest taking what rounding left over. */
    static shares(ratios) {
        var whole = 1000000, units = ratios.map(function (r) { return Math.max(1, Math.round(r * whole)); });
        var sum = 0, big = 0;
        for (var i = 0; i < units.length; i++) { sum += units[i]; if (units[i] > units[big]) big = i; }
        units[big] += whole - sum;
        return units.map(function (u) { return new Scaled(u, 6); });
    }

    _append(event) {
        var self = this;
        this._store.append(event).then(function () {
            self._count++;
            if (self._onCount) self._onCount(self._count);
        }, function (e) { console.error("[WorkspaceRecorder] the store refused an event:", e && e.message); });
    }
}
