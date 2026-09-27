// =============================================================================
// MessagingParty — one instance of a messaging party (the doctrine Messaging
// Parties Are Joined Top-Down): flat — one secretary and its members — and of
// one TYPE, which is its identity. What happens in it is actions: a member
// tells; the secretary, pure, steps — its new state, and what to do: send to
// one member, broadcast to all, or send to its parent.
//
// A party is flat, and a scope is a party of its own linked to the one above
// it: link(upstream, name) joins this party to that one as a member named
// `name`. What the party above says comes in to this secretary, from
// "upstream"; what this secretary sends to its parent goes up through the
// link — bubbling up is the secretary's to decide, kind by kind, and a
// scope that sends nothing up terminates it. Not linked, a send to the
// parent stops here, recorded.
//
// Every message is checked against the type's vocabulary where it enters, a
// member's and the secretary's alike: one that does not read is refused,
// recorded, and goes nowhere. A member hears only the kinds it has a reactor
// for; one that throws is recorded, and the rest still hear. A secretary that
// throws keeps its state, and does nothing.
//
//   new MessagingParty(type, secretary)
//     type       a party type, generated from its Java declaration (PartyType):
//                { name, kinds: { Kind: { field: "string" | "number" | "boolean" } } }
//     secretary  { initial, behavior(state, envelope) → { newState, actions } }, pure
//   party.type
//   party.join(name, reactors) → the membership
//       reactors { Kind: function (message, envelope) }
//       m.id  m.name  m.tell(message) → true when it entered  m.leave()
//   party.send(message, to?)  the substrate's own: to one member by id, or to all
//   party.link(upstream, name) → the membership above: this party joined to a party of
//                             its own type; unlink() leaves it. One link at a time
//   party.state()             the secretary's state
//   party.members()           [{ id, name, hears }]
//   party.on(fn) → off        every passage: { dir, from?, to?, message?, name?, reason? } —
//                             dir "joined", "left", "linked", "unlinked", "up" (a member to the
//                             secretary), "in" (from upstream to the secretary), "down" (to a
//                             member), "out" (to the parent, through the link), "stopped" (to a
//                             parent, none), "refused", "threw"
//   party.inspect()           { type, state, linked, members, refused, stopped, bubbled, threw } — the lists bounded
//   MessagingParty.check(type, message) → null, or why it does not read
// =============================================================================

var _partySeq = 0;

class MessagingParty {
    constructor(type, secretary) {
        if (!type || typeof type.name !== "string" || !type.kinds) throw new Error("[MessagingParty] a party type is required: { name, kinds }");
        if (!secretary || typeof secretary.behavior !== "function") throw new Error("[MessagingParty] '" + type.name + "': a secretary is required: { initial, behavior }");
        this.type = type;
        this._secretary = secretary;
        this._state = secretary.initial;
        this._members = new Map();   // id → { id, name, reactors }
        this._sinks = [];
        this._refused = [];
        this._stopped = [];
        this._bubbled = [];
        this._threw = [];
        this._link = null;   // this party's membership of the party above it, while linked
    }

    /** A member joins, by a name of its own and the kinds it hears. */
    join(name, reactors) {
        if (typeof name !== "string" || !name) throw new Error("[MessagingParty] '" + this.type.name + "': a member joins by a name");
        var self = this, id = "p" + (++_partySeq), hears = {};
        Object.keys(reactors || {}).forEach(function (k) {
            if (!self.type.kinds[k]) throw new Error("[MessagingParty] '" + self.type.name + "': " + name + " hears '" + k + "', which is not a kind of it");
            if (typeof reactors[k] !== "function") throw new Error("[MessagingParty] '" + self.type.name + "': " + name + "'s reactor for " + k + " is not a function");
            hears[k] = reactors[k];
        });
        this._members.set(id, { id: id, name: name, reactors: hears });
        this._said({ dir: "joined", from: id, name: name });
        return Object.freeze({
            id: id,
            name: name,
            tell: function (message) { return self._up(id, message); },
            leave: function () { if (self._members.delete(id)) self._said({ dir: "left", from: id, name: name }); }
        });
    }

    /** The substrate's own send: what the secretary would say, to one member, or to all. */
    send(message, to) {
        var m = this._entered(message, to == null ? "substrate" : "substrate to " + to);
        if (!m) return false;
        if (to == null) this._broadcast("substrate", m);
        else this._deliver(to, "substrate", m);
        return true;
    }

    /**
     * Linked to the party above it, of its own type: joined there as `name`, hearing every
     * kind - each brought in to this secretary, from "upstream". → the membership above,
     * by which the scope may ask there too.
     */
    link(upstream, name) {
        if (this._link) throw new Error("[MessagingParty] '" + this.type.name + "': linked already - unlink first");
        if (!(upstream instanceof MessagingParty) || upstream === this) throw new Error("[MessagingParty] '" + this.type.name + "': a link is to another party");
        if (upstream.type.name !== this.type.name) throw new Error("[MessagingParty] '" + this.type.name + "': a link is to a party of its own type, not '" + upstream.type.name + "'");
        var self = this, hears = {};
        Object.keys(this.type.kinds).forEach(function (k) { hears[k] = function (m) { self._in(m); }; });
        this._link = upstream.join(name, hears);
        this._said({ dir: "linked", name: name });
        return this._link;
    }

    unlink() {
        if (!this._link) return;
        var name = this._link.name;
        this._link.leave();
        this._link = null;
        this._said({ dir: "unlinked", name: name });
    }

    state() { return this._state; }

    members() {
        return Array.from(this._members.values()).map(function (m) { return Object.freeze({ id: m.id, name: m.name, hears: Object.keys(m.reactors) }); });
    }

    on(fn) {
        if (typeof fn !== "function") throw new Error("[MessagingParty] on wants a function");
        var sinks = this._sinks;
        sinks.push(fn);
        return function () { var i = sinks.indexOf(fn); if (i >= 0) sinks.splice(i, 1); };
    }

    inspect() {
        return Object.freeze({ type: this.type.name, state: this._state, linked: this._link ? this._link.name : null, members: this.members(),
                               refused: this._refused.slice(), stopped: this._stopped.slice(), bubbled: this._bubbled.slice(), threw: this._threw.slice() });
    }

    /** Why a message does not read as one of the type's kinds, or null: every field declared, of its type, and no other. */
    static check(type, message) {
        if (!message || typeof message !== "object" || Array.isArray(message)) return "a message is an object with a kind";
        var shape = Object.prototype.hasOwnProperty.call(type.kinds, message.kind) ? type.kinds[message.kind] : null;
        if (!shape) return "'" + message.kind + "' is not a kind of " + type.name + ": " + Object.keys(type.kinds).join(", ");
        for (var f in shape) if (typeof message[f] !== shape[f]) return message.kind + "." + f + " is a " + shape[f] + ", not " + (message[f] === undefined ? "missing" : typeof message[f]);
        for (var k in message) if (k !== "kind" && !Object.prototype.hasOwnProperty.call(shape, k)) return message.kind + " has no field '" + k + "'";
        return null;
    }

    // ── the passages ───────────────────────────────────────────────────────

    /** A member tells the secretary. */
    _up(from, message) {
        if (!this._members.has(from)) return false;
        var m = this._entered(message, from);
        if (!m) return false;
        var name = this._members.get(from).name;
        this._said({ dir: "up", from: from, name: name, message: m });
        this._step(Object.freeze({ from: from, name: name, message: m }));
        return true;
    }

    /** The party above says something: in to this secretary, from "upstream". Checked there, and here. */
    _in(message) {
        var m = this._entered(message, "upstream");
        if (!m) return;
        var name = this._link ? this._link.name : "upstream";
        this._said({ dir: "in", from: "upstream", name: name, message: m });
        this._step(Object.freeze({ from: "upstream", name: name, message: m }));
    }

    /** The secretary steps: its state committed first, then its actions done - so what they cause meets the new state. */
    _step(envelope) {
        var step, m = envelope.message;
        try { step = this._secretary.behavior(this._state, envelope); }
        catch (e) {   // its state kept as it was, and nothing done
            MessagingParty._keep(this._threw, { member: "the secretary", kind: m.kind, error: String(e && e.message || e) });
            this._said({ dir: "threw", from: envelope.from, name: "the secretary", message: m, reason: String(e && e.message || e) });
            console.error("[MessagingParty] '" + this.type.name + "': the secretary threw on " + m.kind + ":", e);
            return;
        }
        this._state = step.newState;
        var self = this;
        (step.actions || []).forEach(function (a) { self._act(a); });
    }

    _act(a) {
        var m = this._entered(a && a.message, "the secretary");
        if (!m) return;
        if (a.kind === "BroadcastToMembers") this._broadcast("secretary", m);
        else if (a.kind === "SendToMember") this._deliver(a.to, "secretary", m);
        else if (a.kind === "SendToParent") this._toParent(m);
        else this._refuse(a.message, "the secretary", "'" + (a && a.kind) + "' is not an action: SendToMember, BroadcastToMembers, SendToParent");
    }

    /** Up through the link, as this party's own word there; with none, it stops here. */
    _toParent(m) {
        if (!this._link) { MessagingParty._keep(this._stopped, m); this._said({ dir: "stopped", message: m }); return; }
        MessagingParty._keep(this._bubbled, m);
        this._said({ dir: "out", name: this._link.name, message: m });
        this._link.tell(m);
    }

    _broadcast(from, m) {
        var self = this;
        Array.from(this._members.keys()).forEach(function (id) { self._deliver(id, from, m); });
    }

    _deliver(to, from, m) {
        var member = this._members.get(to);
        if (!member) { this._refuse(m, from, "no member '" + to + "'"); return; }
        this._said({ dir: "down", from: from, to: to, name: member.name, message: m });
        var reactor = member.reactors[m.kind];
        if (!reactor) return;
        try { reactor(m, Object.freeze({ from: from, message: m })); }
        catch (e) {
            MessagingParty._keep(this._threw, { member: member.name, kind: m.kind, error: String(e && e.message || e) });
            this._said({ dir: "threw", to: to, name: member.name, message: m, reason: String(e && e.message || e) });
            console.error("[MessagingParty] '" + this.type.name + "': " + member.name + "'s reactor for " + m.kind + " threw:", e);
        }
    }

    /** The message as it entered — a frozen copy — or null, refused. */
    _entered(message, from) {
        var why = MessagingParty.check(this.type, message);
        if (why) { this._refuse(message, from, why); return null; }
        return Object.freeze(Object.assign({}, message));
    }

    _refuse(message, from, why) {
        MessagingParty._keep(this._refused, { from: from, reason: why });
        this._said({ dir: "refused", from: from, message: message, reason: why });
    }

    _said(passage) {
        var p = Object.freeze(passage), sinks = this._sinks.slice();
        for (var i = 0; i < sinks.length; i++) {
            try { sinks[i](p); } catch (e) { console.error("[MessagingParty] a listener threw on " + p.dir + ":", e); }
        }
    }

    /** A bounded list: the last few, never all. */
    static _keep(list, item) { list.push(item); if (list.length > MessagingParty.KEPT) list.shift(); }
}

/** How many refused, stopped and thrown are kept for inspect(). */
MessagingParty.KEPT = 10;
