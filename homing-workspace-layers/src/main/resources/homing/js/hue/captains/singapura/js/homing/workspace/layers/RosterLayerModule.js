// =============================================================================
// RosterLayer — the roster's layer of the workspace log, live: the core's word
// recorded — a widget opened, its id, its kind and its params in the order of
// their keys; a widget closed — and a roster come back to. It sits beside the
// core, never in it: the core says what happens, and knows no log.
//
// A workspace comes back ROSTER FIRST — every id its prefixes gave spent,
// closed or not, so the ids go on past them; then each widget held made again
// under its id, in the order opened, created and mounted nowhere — and its
// placement after, mounting them as its own state has them. Nothing of the
// coming back is recorded: the recorder is attached after it.
//
// Java's RosterLayer, step for step; the two write the same lines
// (LayersParityTest).
//
//   RosterLayer.event(notice) → the WorkspaceEvent a core notice is recorded as, or null
//   RosterLayer.record(core, log) → off   each appended as it is said: log.append(event)
//   RosterLayer.restore(core, roster) → { opened, skipped }   roster: a RosterState; a widget
//                   that cannot be made again — a kind no longer there, or failing — is skipped, said
// =============================================================================

class RosterLayer {
    static event(n) {
        if (n.kind === "WidgetOpened") {
            var p = n.entry.params || {};
            var params = Object.keys(p).sort().map(function (k) { return new WidgetParam(k, String(p[k])); });
            return new WidgetOpened(new WidgetId(n.entry.id), new WidgetKind(n.entry.kind), params);
        }
        if (n.kind === "WidgetRenamed") return new WidgetRenamed(new WidgetId(n.entry.id), n.entry.name == null ? null : new WidgetName(n.entry.name));
        if (n.kind === "WidgetClosed") return new WidgetClosed(new WidgetId(n.id));
        return null;
    }

    static record(core, log) {
        if (!log || typeof log.append !== "function") throw new Error("[RosterLayer] a log to append to is required");
        return core.on(function (n) {
            var e = RosterLayer.event(n);
            if (e) RosterLayer._append(log, e);
        });
    }

    static restore(core, roster) {
        roster.sequences.forEach(function (s) { core.spend(s.prefix, s.last); });
        var opened = [], skipped = [];
        roster.widgets.forEach(function (w) {
            var params = {};
            w.params.forEach(function (p) { params[p.key] = p.value; });
            try {
                core.create(w.kind.value, params, w.id.value, w.name === null ? null : w.name.value);
                opened.push(w.id.value);
            } catch (e) {
                console.error("[RosterLayer] '" + w.id.value + "' does not come back: " + e.message);
                skipped.push(w.id.value);
            }
        });
        return Object.freeze({ opened: opened, skipped: skipped });
    }

    /** One event appended; a log that keeps it later and fails says so. */
    static _append(log, e) {
        var kept = log.append(e);
        if (kept && typeof kept.then === "function") {
            kept.then(null, function (err) { console.error("[RosterLayer] not recorded: " + (err && err.message)); });
        }
    }
}
