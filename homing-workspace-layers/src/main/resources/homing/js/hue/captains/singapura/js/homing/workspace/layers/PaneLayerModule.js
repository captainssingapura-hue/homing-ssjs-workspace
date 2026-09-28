// =============================================================================
// PaneLayer — the one pane's layer of the workspace log, live: every change of
// what the pane (PanePlacement) shows recorded; and what it showed come back
// to, after the roster has come back — a widget that did not come back is not
// shown. Java's PaneLayer, step for step; the two write the same lines
// (LayersParityTest).
//
//   PaneLayer.event(notice) → the WorkspaceEvent a pane notice is recorded as
//   PaneLayer.record(pane, log) → off   each appended as it is said: log.append(event)
//   PaneLayer.restore(pane, core, state) → the id shown, or null   state: a PaneState. Every
//                widget the core holds mounted behind, in the order opened — in single mode
//                every widget opened is mounted in the pane — then the one the log last showed
// =============================================================================

class PaneLayer {
    static event(n) {
        return n.kind === "PaneShown" ? new PaneShown(n.id === null ? null : new WidgetId(n.id)) : null;
    }

    static record(pane, log) {
        if (!log || typeof log.append !== "function") throw new Error("[PaneLayer] a log to append to is required");
        return pane.on(function (n) {
            var e = PaneLayer.event(n);
            if (!e) return;
            var kept = log.append(e);
            if (kept && typeof kept.then === "function") {
                kept.then(null, function (err) { console.error("[PaneLayer] not recorded: " + (err && err.message)); });
            }
        });
    }

    static restore(pane, core, state) {
        core.entries().forEach(function (e) { pane.mount(e, "behind"); });
        var id = state.shown === null ? null : state.shown.value;
        if (id !== null && pane.mounted().indexOf(id) < 0) id = null;
        pane.show(id);
        return id;
    }
}
