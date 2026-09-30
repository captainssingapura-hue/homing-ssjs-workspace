// =============================================================================
// WorkspaceParties — the workspace's messaging parties, headless, beside its
// core (RFC 0066 E3, the workspace detour): the workspace HOSTS its own root
// parties, and they are its widgets' substrate for messaging. They are
// resolved at type level, in Java (WorkspaceDeclaration): one of each type its
// kinds declare, each with the secretary at its root — and made here, when the
// workspace is made, before any widget opens. No page keeps a root of its own.
//
// A widget is joined when it is opened and leaves when it is closed — told by
// the core — and nothing a placement does touches either: a widget not shown is
// still joined, and still hears. It is given its KIND's types, as its
// declaration has them, not as the widget says of itself (Messaging Parties Are
// Joined Top-Down): each of them, and no other.
//
// Its dual in Java comes with the dual of the parties' runtime.
//
//   new WorkspaceParties(core, { parties, kinds })   — a workspace's manifest has both
//     parties  [{ type, secretary, steward? }] — the root parties, in the order made;
//              a steward, the class a root hires when its parties fetch
//     kinds    { [kind]: { parties: [type] } } — the types each kind is given
//              A kind given a type with no root party is refused here, before any widget opens
//   parties.party(name) → the root instance of that type, or null when the workspace has none
//   parties.names()     → the types, in the order made
//   parties.dispose()   the core no longer listened to
// =============================================================================

class WorkspaceParties {
    constructor(core, opts) {
        if (!core || typeof core.on !== "function") throw new Error("[WorkspaceParties] the core is required");
        var o = opts || {}, self = this;
        this._kinds = o.kinds || {};
        this._parties = new Map();   // type name → the root instance, in the order made
        (o.parties || []).forEach(function (p) {
            if (!p || !p.type || !p.secretary) throw new Error("[WorkspaceParties] a root party is { type, secretary }");
            if (self._parties.has(p.type.name)) throw new Error("[WorkspaceParties] two root parties of '" + p.type.name + "'");
            self._parties.set(p.type.name, new MessagingParty(p.type, p.secretary, p.steward || null));
        });
        Object.keys(this._kinds).forEach(function (k) {
            (self._kinds[k].parties || []).forEach(function (t) {
                if (!self._parties.has(t.name)) throw new Error("[WorkspaceParties] the kind '" + k + "' is given '" + t.name + "', which has no root party");
            });
        });
        this._off = core.on(function (n) {
            if (n.kind === "WidgetOpened") self._join(n.entry);
            else if (n.kind === "WidgetClosing") self._leave(n.entry);
        });
    }

    party(name) { return this._parties.get(name) || null; }

    names() { return Array.from(this._parties.keys()); }

    dispose() { if (this._off) { this._off(); this._off = null; } }

    /** A widget opened: given the root instance of each type its kind declares, and joined. */
    _join(entry) {
        var kind = Object.prototype.hasOwnProperty.call(this._kinds, entry.kind) ? this._kinds[entry.kind] : null;
        var types = kind && kind.parties ? kind.parties : [], w = entry.widget;
        if (!types.length) return;
        if (!w || typeof w.join !== "function") {
            // said, not thrown: the core has opened it, and a notice's listener must not undo that
            console.error("[WorkspaceParties] '" + entry.id + "' is of a kind given " + types.map(function (t) { return t.name; }).join(", ") + ", and cannot join");
            return;
        }
        var given = {}, self = this;
        types.forEach(function (t) { given[t.name] = self._parties.get(t.name); });
        w.join(Object.freeze(given));
    }

    _leave(entry) {
        var w = entry.widget;
        if (w && typeof w.leave === "function") w.leave();
    }
}
