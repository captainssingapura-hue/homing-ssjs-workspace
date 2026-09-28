// =============================================================================
// WorkspaceRecorder — what the workspace's components report, written into its
// log: each report that changes the arrangement becomes the WorkspaceEvent it
// is — a class generated from its Java declaration — and goes to the store.
// Nothing here spells an event by hand; a value the class refuses is not
// recorded, and says so.
//
//   new WorkspaceRecorder({ store, isRegion, isFloat, kindOf, onCount? })
//     store     a WorkspaceLogStore
//     isRegion  (slotId) → whether a host is one of the grid's regions
//     isFloat   (slotId) → whether a host is one of the desk's floats
//     kindOf    (tabId) → the kind a tab holds: the tab source's
//     onCount   (n) → told how many it has recorded, after each
//   recorder.hear(report)       a desk's, a dock's, a float's or the grid's report
//   recorder.became(tp, kindId) a tab became another kind (the source's onBecame)
//   recorder.record(event)      a WorkspaceEvent another layer made - the roster's - appended
//                               and counted with the rest
//   recorder.stop()             nothing more is recorded: before the workspace
//                               is taken down, whose closing is not the user's
//
// A tab is always somewhere: a Host, in a region or in a float — a float is a
// state the workspace comes back to. The reports, and what each is in the log:
//   TabAdded                    TabOpened in its host, with the kind the source made it
//   TabMoved                    TabMoved to its host: a region, a float, its own
//   TabRenamed                  TabRenamed
//   TabRemoved                  TabClosed
//   TabActivated                TabShown in its host
//   Subdivided / Removed        RegionParted / RegionRemoved, its room toward the region the grid was told
//   TracksChanged               TracksChanged, each share an exact millionth
//   Opened / Moved / Resized    FloatOpened / FloatMoved / FloatResized, whole pixels
//   Raised / Closed             FloatRaised / FloatClosed
// =============================================================================

function _title(tab) { return typeof tab.title === "function" ? tab.title() : tab.title; }

class WorkspaceRecorder {
    constructor(opts) {
        var o = opts || {};
        if (!o.store) throw new Error("[WorkspaceRecorder] opts.store is required");
        if (typeof o.isRegion !== "function" || typeof o.isFloat !== "function" || typeof o.kindOf !== "function") {
            throw new Error("[WorkspaceRecorder] opts.isRegion, opts.isFloat and opts.kindOf are required");
        }
        this._store = o.store;
        this._isRegion = o.isRegion;
        this._isFloat = o.isFloat;
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

    record(event) { if (this._on) this._append(event); }

    stop() { this._on = false; }

    count() { return this._count; }

    /** The host a slot is: a region, a float — or neither, which the log does not know. */
    _host(slotId) {
        if (this._isRegion(slotId)) return new InRegion(new RegionId(slotId));
        if (this._isFloat(slotId)) return new InFloat(new FloatId(slotId));
        throw new Error("'" + slotId + "' is neither a region nor a float");
    }

    _of(r) {
        switch (r.kind) {
            case "TabAdded": {
                var kind = this._kindOf(r.tab.id);
                if (!kind) throw new Error("tab '" + r.tab.id + "' was not made by the tab source: its kind is not known");
                return new TabOpened(new TabId(r.tab.id), new WidgetKind(kind), new WidgetTitle(_title(r.tab)), this._host(r.slotId), r.index);
            }
            case "TabMoved":     return new TabMoved(new TabId(r.tab.id), this._host(r.destSlotId), r.destIndex);
            case "TabRenamed":   return new TabRenamed(new TabId(r.tabId), new WidgetTitle(r.title));
            case "TabRemoved":   return new TabClosed(new TabId(r.tab.id));
            case "TabActivated": return new TabShown(this._host(r.slotId), new TabId(r.tabId));
            case "Subdivided":   return new RegionParted(new RegionId(r.cellId), new RegionId(r.newCellId), Side.of(String(r.side).toUpperCase()));
            case "Removed":      return new RegionRemoved(new RegionId(r.cellId), r.toward ? new RegionId(r.toward) : null);
            case "TracksChanged": return new TracksChanged(new SplitPath(r.path), WorkspaceRecorder.shares(r.ratios));
            case "Opened":       return new FloatOpened(new FloatId(r.id), r.x, r.y, r.w, r.h);
            case "Moved":        return new FloatMoved(new FloatId(r.id), r.x, r.y);
            case "Resized":      return new FloatResized(new FloatId(r.id), r.w, r.h);
            case "Raised":       return new FloatRaised(new FloatId(r.id));
            case "Closed":       return new FloatClosed(new FloatId(r.id));
            default:             return null;
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
