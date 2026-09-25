// =============================================================================
// WorkspacePaneEvents — every event from the grid, the docks and the desk, on
// to the panes' holder, with what only the assembly knows filled in. Static,
// over the WorkspacePanes it is handed, as the pane's own PaneTabs is: the
// assembly keeps the tables, this reads and writes them as an event passes.
//
//   WorkspacePaneEvents.fire(panes, ev)   the event, filled in, to the holder's sink
//
// The holder keeps its records by the tab it made, so every event names THAT
// tab. A dock names the one it activated only by id; and a tab that floated
// comes back from the desk as the desk's own { id, title, widget }, which the
// dock it lands on keeps and reports from then on - the same tab, with nothing
// the holder wrote on it. A tab closed goes out of the table, its icon with
// it. And a tab floated off a dock and closed on the desk IS that tab closed:
// the desk only knows a pane went, so it is said here as the dock would have
// said it, from the place the tab left.
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
        if (merging && ev.slotId !== undefined && ev.kind !== "Removed") ev = WorkspacePaneEvents._with(ev, { merging: merging.slotId });
        switch (ev.kind) {
            case "TabActivated":
                ev = WorkspacePaneEvents._with(ev, { tab: tabs.get(ev.tabId) || null });
                break;
            case "TabRemoved":
                tabs.delete(ev.tab.id);
                panes._forgetIcon(ev.tab.id);
                break;
            case "Docked": {
                panes._floated.delete(ev.tabId);
                // A tab dropped on a dock is the one it shows: the hand put it
                // there to look at it, and a replay of the move shows it too.
                var dock = panes._panes.get(ev.slotId);
                panes._emit(ev);
                if (dock && dock.activeTab() !== ev.tabId) dock.switchTab(ev.tabId);
                return;
            }
            case "Removed":
                if (merging && merging.slotId === ev.cellId) ev = WorkspacePaneEvents._with(ev, { toward: merging.toward });
                break;
            case "Closed": {
                var from = panes._floated.get(ev.id), tab = tabs.get(ev.id);
                if (!from || !tab) break;
                panes._floated.delete(ev.id);
                tabs.delete(ev.id);
                panes._forgetIcon(ev.id);
                panes._emit(ev);
                ev = PaneEvents.TabRemoved(from.slotId, tab, from.index);
                break;
            }
        }
        panes._emit(ev);
    }

    /** The event with more said on it: a copy, frozen as every event is. */
    static _with(ev, more) { return Object.freeze(Object.assign({}, ev, more)); }
}
