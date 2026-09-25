// =============================================================================
// WorkspacePaneEvents — every event from the grid, the docks and the desk, on
// to the panes' holder, with what only the assembly knows filled in. Static,
// over the WorkspacePanes it is handed, as the pane's own PaneTabs is: the
// assembly keeps the tables, this reads and writes them as an event passes.
//
//   WorkspacePaneEvents.fire(panes, ev)   the event, filled in, to the holder's sink
//
// The holder keeps its records by the tab it opened, so every event names THAT
// record, never the tab-pane the docks and the desk report: a dock names the
// tab it activated only by id, and the tab-pane carries nothing the holder
// wrote on it. A tab closed goes out of the table, its icon with it.
//
// A FLOAT IS TRANSIENT, so the holder never hears of one. A tab moved into a
// float is still where it left, and that place is kept here; what a float
// shows is nobody's record; a tab that comes down from a float onto a dock
// moved from where it left to where it landed; a tab closed afloat closed
// where it left. Shift+↓ on a dock asks for its tab to float: answered here,
// at the tab's chip, as the tab menu's Detach is.
//
// A merge is ONE fact. The tabs it carries across, and the tab each dock then
// shows, are part of it: marked, so the holder keeps its books by them but
// records only the merge, whose replay moves the tabs the same way; and the
// grid's Removed carries the pane the merge was asked toward.
// =============================================================================

class WorkspacePaneEvents {

    static fire(panes, ev) {
        var tabs = panes._tabObjs;
        if (ev.tab && ev.tab !== tabs.get(ev.tab.id) && tabs.has(ev.tab.id)) {
            ev = WorkspacePaneEvents._with(ev, { tab: tabs.get(ev.tab.id) });
        }
        var merging = panes._merging;
        if (merging && (ev.slotId !== undefined || ev.srcSlotId !== undefined) && ev.kind !== "Removed") {
            ev = WorkspacePaneEvents._with(ev, { merging: merging.slotId });
        }
        var afloat = function (slotId) { return slotId != null && !panes._panes.has(slotId); };
        switch (ev.kind) {
            case "TabActivated":
                if (afloat(ev.slotId)) return;
                ev = WorkspacePaneEvents._with(ev, { tab: tabs.get(ev.tabId) || null });
                break;
            case "TabRemoved": {
                tabs.delete(ev.tab.id);
                panes._forgetIcon(ev.tab.id);
                var left = panes._floated.get(ev.tab.id);
                if (left) {
                    panes._floated.delete(ev.tab.id);
                    ev = PaneEvents.TabRemoved(left.slotId, ev.tab, left.index);
                }
                break;
            }
            case "TabMoved": {
                var into = afloat(ev.destSlotId), outOf = afloat(ev.srcSlotId);
                if (into) {
                    if (!outOf) panes._floated.set(ev.tab.id, { slotId: ev.srcSlotId, index: ev.srcIndex });
                    return;
                }
                if (outOf) {
                    var from = panes._floated.get(ev.tab.id) || { slotId: ev.srcSlotId, index: ev.srcIndex };
                    panes._floated.delete(ev.tab.id);
                    ev = PaneEvents.TabMoved(from.slotId, ev.tab, from.index, ev.destSlotId, ev.destIndex);
                }
                break;
            }
            case "DetachRequested": {
                var tp = panes._desk.register.get(ev.tabId);
                if (tp && !tp.pinned) {
                    var r = tp.chip.getBoundingClientRect();
                    panes._desk.detach(tp, { x: r.left + 60, y: r.bottom + 14 });
                }
                return;
            }
            case "Removed":
                if (merging && merging.slotId === ev.cellId) ev = WorkspacePaneEvents._with(ev, { toward: merging.toward });
                break;
        }
        panes._emit(ev);
    }

    /** The event with more said on it: a copy, frozen as every event is. */
    static _with(ev, more) { return Object.freeze(Object.assign({}, ev, more)); }
}
