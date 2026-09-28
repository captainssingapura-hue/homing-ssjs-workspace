// =============================================================================
// WorkspaceCore — the workspace's headless core (RFC 0066 E3, the workspace
// detour): the ROSTER — the widgets the workspace holds, each under an id of
// its own that says what it is (WidgetIds) — and their LIFE, which is their
// panes' too: a widget and its pane live and die together. A widget is
// created — its pane lent by the register of panes, placed nowhere, the widget
// made in it, and said opened — and closed — said closing, disposed, its pane
// closed, said closed.
//
// What a user asks is a REQUEST (WorkspaceRequest), and the core executes it,
// handled by the core and the placement in an order fixed for its type: an
// open is create, then mount at the location it names — a mount that fails
// closes what was created; a close is unmount, then close. The placement
// mounts and unmounts, and never creates or closes; the core calls it only in
// executing a request. A restore creates without mounting (create), and each
// placement mounts from its own state.
//
// The dual of WorkspaceCore.java, step for step; the two agree on the ids and
// on what they say (WorkspaceCoreParityTest). What else a widget's opening
// means — its DomOps root grafted, its messaging parties joined — is a
// listener's, never the core's. It touches no DOM and imports no DOM module:
// the widgets' classes are handed to it.
//
//   new WorkspaceCore({ kinds, panes, placement })
//     kinds      { [kind]: { Widget } } — made new Widget(container, params), disposed widget.dispose()
//     panes      the register of panes: { lend(entry) → container, rename(entry), release(entry) } —
//                it titles a pane after its widget: the name a user gave it, else its identity
//     placement  { mount(entry, location), unmount(entry) }
//   core.execute(request) → the entry, for an open and a rename; null, for a close. A rename is
//                the roster's and the pane's alone: the name kept, said, then the pane titled again
//                by the register — the placement asked nothing
//   core.create(kind, params, id?, name?) → the entry { id, kind, params, name, widget }, placed nowhere: under
//                the id given (a widget coming back) — one its kind and params would make, not
//                held — else the next of its prefix; the sequence goes on past it. A restore's step
//   core.spend(prefix, last)  the ids of a prefix spent up to last, held or not — a workspace
//                coming back, which gave them before: the next of the prefix is past it
//   core.dispose()  the workspace taken down: every widget closed, the last first, no placement asked
//   core.entry(id) → the entry, or null     core.entries() → in the order opened
//   core.on(fn) → off   notices, in the order they happen:
//                { kind: "WidgetOpened", entry }   made, and in the roster
//                { kind: "WidgetRenamed", entry }  named, or its name taken back: the entry as it is now
//                { kind: "WidgetClosing", entry }  about to be disposed, still whole
//                { kind: "WidgetClosed", id, widgetKind }  disposed, its pane closed, out of the roster
// =============================================================================

class WorkspaceCore {
    constructor(opts) {
        var o = opts || {};
        if (!o.kinds) throw new Error("[WorkspaceCore] opts.kinds is required: { [kind]: { Widget } }");
        if (!o.panes || typeof o.panes.lend !== "function" || typeof o.panes.rename !== "function" || typeof o.panes.release !== "function") {
            throw new Error("[WorkspaceCore] opts.panes is required: the register of panes, { lend(entry), rename(entry), release(entry) }");
        }
        if (!o.placement || typeof o.placement.mount !== "function" || typeof o.placement.unmount !== "function") {
            throw new Error("[WorkspaceCore] opts.placement is required: { mount(entry, location), unmount(entry) }");
        }
        this._kinds = o.kinds;
        this._panes = o.panes;
        this._placement = o.placement;
        this._roster = new Map();       // id → entry, in the order opened
        this._sequences = new Map();    // prefix → the last sequence given
        this._sinks = [];
    }

    execute(request) {
        if (!request || ["Open", "Close", "Rename"].indexOf(request.kind) < 0) throw new Error("[WorkspaceCore] not a request: " + JSON.stringify(request));
        if (request.kind === "Open") {
            var entry = this.create(request.widgetKind, request.params, null);
            try { this._placement.mount(entry, request.location); }
            catch (e) { this._close(entry); throw e; }
            return entry;
        }
        if (request.kind === "Rename") {
            var was = this._held(request.id);
            var named = Object.freeze({ id: was.id, kind: was.kind, params: was.params, name: request.name, widget: was.widget });
            this._roster.set(named.id, named);
            this._say({ kind: "WidgetRenamed", entry: named });
            this._panes.rename(named);
            return named;
        }
        var held = this._held(request.id);
        this._placement.unmount(held);
        this._close(held);
        return null;
    }

    create(kind, params, id, name) {
        var k = Object.prototype.hasOwnProperty.call(this._kinds, kind) ? this._kinds[kind] : null;
        if (!k) throw new Error("[WorkspaceCore] no kind '" + kind + "': " + Object.keys(this._kinds).sort().join(", "));
        var p = Object.freeze(Object.assign({}, params || {}));
        var prefix = WidgetIds.prefix(kind, p);
        var wid = id == null ? WidgetIds.of(prefix, (this._sequences.get(prefix) || 0) + 1) : this._given(id, prefix);
        this._sequences.set(prefix, Math.max(this._sequences.get(prefix) || 0, WidgetIds.split(wid).n));
        var given = name == null ? null : name;
        var lent = Object.freeze({ id: wid, kind: kind, params: p, name: given, widget: null });
        var container = this._panes.lend(lent);
        var widget;
        try { widget = new k.Widget(container, p); }
        catch (e) { this._panes.release(lent); throw e; }   // the id is spent: never reused
        var entry = Object.freeze({ id: wid, kind: kind, params: p, name: given, widget: widget });
        this._roster.set(wid, entry);
        this._say({ kind: "WidgetOpened", entry: entry });
        return entry;
    }

    spend(prefix, last) {
        if (!Number.isInteger(last) || last < 1) throw new Error("[WorkspaceCore] a sequence starts at 1: " + last);
        if (typeof prefix !== "string") throw new Error("[WorkspaceCore] spend wants a prefix");
        this._sequences.set(prefix, Math.max(this._sequences.get(prefix) || 0, last));
    }

    _given(id, prefix) {
        if (this._roster.has(id)) throw new Error("[WorkspaceCore] '" + id + "' is held already");
        var s = WidgetIds.split(id);
        if (!s || !WidgetIds.GRAMMAR.test(String(id))) throw new Error("[WorkspaceCore] '" + id + "' is not an id the core makes");
        if (s.prefix !== prefix) throw new Error("[WorkspaceCore] '" + id + "' is not what its kind and params make: " + prefix + "-n");
        return String(id);
    }

    _held(id) {
        var entry = this._roster.get(id);
        if (!entry) throw new Error("[WorkspaceCore] no widget '" + id + "'");
        return entry;
    }

    _close(entry) {
        this._say({ kind: "WidgetClosing", entry: entry });
        try { if (typeof entry.widget.dispose === "function") entry.widget.dispose(); }
        finally {
            this._roster.delete(entry.id);
            this._panes.release(entry);
            this._say({ kind: "WidgetClosed", id: entry.id, widgetKind: entry.kind });
        }
    }

    dispose() {
        var all = Array.from(this._roster.values());
        for (var i = all.length - 1; i >= 0; i--) this._close(all[i]);
    }

    entry(id) { return this._roster.get(id) || null; }

    entries() { return Array.from(this._roster.values()); }

    on(fn) {
        if (typeof fn !== "function") throw new Error("[WorkspaceCore] on wants a function");
        var sinks = this._sinks;
        sinks.push(fn);
        return function () { var i = sinks.indexOf(fn); if (i >= 0) sinks.splice(i, 1); };
    }

    _say(notice) {
        var n = Object.freeze(notice), sinks = this._sinks.slice();
        for (var i = 0; i < sinks.length; i++) sinks[i](n);
    }
}
