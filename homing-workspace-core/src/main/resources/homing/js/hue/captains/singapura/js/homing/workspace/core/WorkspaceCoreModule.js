// =============================================================================
// WorkspaceCore — the workspace's headless core (RFC 0066 E3, the workspace
// detour): the ROSTER — the widgets the workspace holds, each under an id of
// its own that says what it is (WidgetIds) — and their LIFE. A widget is
// opened — a container lent by the placement, the widget made in it, and said
// opened — and closed — said closing, disposed, its container handed back,
// said closed. Whether it is shown, and where, is the placement's: a widget IS,
// whether it is mounted anywhere or not. The core knows the placement only as
// a port, and nothing of what it does.
//
// The dual of WorkspaceCore.java, step for step; the two agree on the ids and
// on what they say (WorkspaceCoreParityTest). What else a widget's opening
// means — its DomOps root grafted, its messaging parties joined — is a
// listener's, never the core's. It touches no DOM and imports no DOM module:
// the widgets' classes are handed to it.
//
//   new WorkspaceCore({ kinds, placement })
//     kinds      { [kind]: { Widget } } — made new Widget(container, params), disposed widget.dispose()
//     placement  { lend(entry) → container, release(entry) }
//   core.open(kind, params, id?) → the entry { id, kind, params, widget }: under the id given
//                (a widget coming back) — one its kind and params would make, not held — else
//                the next of its prefix; the sequence goes on past it
//   core.spend(prefix, last)  the ids of a prefix spent up to last, held or not — a workspace
//                coming back, which gave them before: the next of the prefix is past it
//   core.close(id)
//   core.entry(id) → the entry, or null     core.entries() → in the order opened
//   core.on(fn) → off   notices, in the order they happen:
//                { kind: "WidgetOpened", entry }   made, and in the roster
//                { kind: "WidgetClosing", entry }  about to be disposed, still whole
//                { kind: "WidgetClosed", id, widgetKind }  disposed, its container back, out of the roster
// =============================================================================

class WorkspaceCore {
    constructor(opts) {
        var o = opts || {};
        if (!o.kinds) throw new Error("[WorkspaceCore] opts.kinds is required: { [kind]: { Widget } }");
        if (!o.placement || typeof o.placement.lend !== "function" || typeof o.placement.release !== "function") {
            throw new Error("[WorkspaceCore] opts.placement is required: { lend(entry), release(entry) }");
        }
        this._kinds = o.kinds;
        this._placement = o.placement;
        this._roster = new Map();       // id → entry, in the order opened
        this._sequences = new Map();    // prefix → the last sequence given
        this._sinks = [];
    }

    open(kind, params, id) {
        var k = Object.prototype.hasOwnProperty.call(this._kinds, kind) ? this._kinds[kind] : null;
        if (!k) throw new Error("[WorkspaceCore] no kind '" + kind + "': " + Object.keys(this._kinds).sort().join(", "));
        var p = Object.freeze(Object.assign({}, params || {}));
        var prefix = WidgetIds.prefix(kind, p);
        var wid = id == null ? WidgetIds.of(prefix, (this._sequences.get(prefix) || 0) + 1) : this._given(id, prefix);
        this._sequences.set(prefix, Math.max(this._sequences.get(prefix) || 0, WidgetIds.split(wid).n));
        var lent = Object.freeze({ id: wid, kind: kind, params: p, widget: null });
        var container = this._placement.lend(lent);
        var widget;
        try { widget = new k.Widget(container, p); }
        catch (e) { this._placement.release(lent); throw e; }   // the id is spent: never reused
        var entry = Object.freeze({ id: wid, kind: kind, params: p, widget: widget });
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

    close(id) {
        var entry = this._roster.get(id);
        if (!entry) throw new Error("[WorkspaceCore] no widget '" + id + "'");
        this._say({ kind: "WidgetClosing", entry: entry });
        try { if (typeof entry.widget.dispose === "function") entry.widget.dispose(); }
        finally {
            this._roster.delete(id);
            this._placement.release(entry);
            this._say({ kind: "WidgetClosed", id: id, widgetKind: entry.kind });
        }
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
