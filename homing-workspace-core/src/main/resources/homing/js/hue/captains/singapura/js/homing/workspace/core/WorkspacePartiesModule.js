// =============================================================================
// WorkspaceParties — the workspace's messaging parties, headless, beside its
// core (RFC 0066 E3, the workspace detour): the core owns them, and they are
// its SUBSTRATE for messaging — a root instance of each type the opened
// widgets declare (widget.parties), made when a widget first needs it, its
// secretary the one given for its type. A widget is joined when it is opened
// and leaves when it is closed — told by the core — and nothing a placement
// does touches either: a widget not shown is still joined, and still hears.
// Its parties are the one thing it is given (Messaging Parties Are Joined
// Top-Down): each type it declares, and no other.
//
// Its dual in Java comes with the dual of the parties' runtime.
//
//   new WorkspaceParties(core, { secretaries })
//     secretaries  { [type name]: { initial, behavior } } — each type's root secretary.
//                  A type with none given is refused where a widget first needs it
//   parties.party(name) → the root instance of that type, or null while none is made
//   parties.names()     → the types made, in the order made
//   parties.on(fn) → off   { kind: "PartyMade", name, party } when a type's instance is made
//   parties.dispose()   the core no longer listened to
// =============================================================================

class WorkspaceParties {
    constructor(core, opts) {
        if (!core || typeof core.on !== "function") throw new Error("[WorkspaceParties] the core is required");
        var o = opts || {}, self = this;
        this._secretaries = o.secretaries || {};
        this._parties = new Map();   // type name → the root instance
        this._sinks = [];
        this._off = core.on(function (n) {
            if (n.kind === "WidgetOpened") self._join(n.entry);
            else if (n.kind === "WidgetClosing") self._leave(n.entry);
        });
    }

    party(name) { return this._parties.get(name) || null; }

    names() { return Array.from(this._parties.keys()); }

    on(fn) {
        if (typeof fn !== "function") throw new Error("[WorkspaceParties] on wants a function");
        var sinks = this._sinks;
        sinks.push(fn);
        return function () { var i = sinks.indexOf(fn); if (i >= 0) sinks.splice(i, 1); };
    }

    dispose() { if (this._off) { this._off(); this._off = null; } }

    /** A widget opened: given the root instance of each type it declares - made first when none is - and joined. */
    _join(entry) {
        var w = entry.widget, types = w && Array.isArray(w.parties) ? w.parties : [];
        if (!types.length || typeof w.join !== "function") return;
        var given = {}, self = this;
        types.forEach(function (t) { given[t.name] = self._made(t); });
        w.join(given);
    }

    _leave(entry) {
        var w = entry.widget;
        if (w && typeof w.leave === "function") w.leave();
    }

    /** The root instance of a type, made the first time one is needed, with the secretary given for it. */
    _made(type) {
        var party = this._parties.get(type.name);
        if (party) return party;
        var secretary = Object.prototype.hasOwnProperty.call(this._secretaries, type.name) ? this._secretaries[type.name] : null;
        if (!secretary) throw new Error("[WorkspaceParties] no secretary given for '" + type.name + "': a widget needs one");
        party = new MessagingParty(type, secretary);
        this._parties.set(type.name, party);
        var notice = Object.freeze({ kind: "PartyMade", name: type.name, party: party });
        this._sinks.slice().forEach(function (fn) { fn(notice); });
        return party;
    }
}
