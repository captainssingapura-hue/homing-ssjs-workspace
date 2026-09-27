// =============================================================================
// PartySimulator — a messaging party on the bench, simulated by hand: the
// party at the root of a type the widget under test joins, its secretary the
// bench's manual one (BenchSecretary). What passes in the party is logged —
// who joined, what a member told, what went down to whom, what was refused;
// and any message typed as JSON, free-form, is sent down to every member as
// the secretary would say it, once it reads as a kind of the party's type.
// A template of each kind can be put in the box to start from.
//
// A tab's widget by the pane's law (make(branch, tab)): a member of the focus
// branch it is handed, answering activate(), its Escape giving the keys back.
// Bench tooling, not a widget of the workspace's: it is handed the party.
//
//   new PartySimulator(branch, tab, party)
//     .root  .focus
//     .send(text) → true when it read and went down; else the box's line says why
//     .dispose()   it stops listening and leaves; the party is the bench's, and stays
// =============================================================================

const _simulatorOwner = Object.freeze({ toString: () => "partySimulator" });

class PartySimulator {
    constructor(branch, tab, party) {
        if (!branch || !tab || !tab.focus) throw new Error("[PartySimulator] a branch and a tab are required");
        if (!party || !party.type) throw new Error("[PartySimulator] the party to simulate is required");
        var self = this;
        branch.activate(_simulatorOwner);
        this.branch = branch;
        this._party = party;
        this._lines = [];
        this._seq = 0;
        this._rows = null;
        this._pending = false;
        var root = branch.createElement("root", "div");
        css.addClass(root, wb_sim);
        root.setAttribute("role", "region");
        root.setAttribute("aria-label", "Party " + party.type.name);
        this._members = branch.createElement("members", "div");
        css.addClass(this._members, wb_sim_note);
        this._log = branch.createElement("log", "div");
        css.addClass(this._log, wb_sim_log);
        this._log.setAttribute("role", "log");
        var input = branch.createElement("input", "textarea");
        css.addClass(input, wb_sim_input);
        input.setAttribute("aria-label", "A message, as JSON");
        input.setAttribute("spellcheck", "false");
        input.placeholder = '{ "kind": "…" }';
        this._input = input;
        var bar = branch.createElement("bar", "div");
        css.addClass(bar, wb_sim_bar);
        var pick = branch.createElement("pick", "select");
        css.addClass(pick, wb_sim_pick);
        pick.setAttribute("aria-label", "A template of a kind");
        pick.appendChild(PartySimulator._option(branch, "pick-none", "", "Template…"));
        Object.keys(party.type.kinds).forEach(function (k) { pick.appendChild(PartySimulator._option(branch, "pick-" + k, k, k)); });
        pick.addEventListener("change", function () { if (pick.value) input.value = JSON.stringify(PartySimulator.template(party.type, pick.value)); pick.value = ""; });
        var b = new ButtonBuilder();
        var send = b.label("Send to all").plain().size(-1).onClick(function () { self.send(input.value); }).build(branch.createElement("send", b.tag));
        this._said = branch.createElement("said", "div");
        css.addClass(this._said, wb_sim_said);
        this._said.setAttribute("role", "status");
        bar.appendChild(pick);
        bar.appendChild(send.el);
        bar.appendChild(this._said);
        root.appendChild(this._members);
        root.appendChild(this._log);
        root.appendChild(input);
        root.appendChild(bar);
        this.root = root;
        this.focus = tab.focus.join(tab.name, this);
        this._offKeys = Keys.claimOn(root, this.focus);
        this._offParty = party.on(function (p) { self._heard(p); });
        this._showMembers();
    }

    /** A message typed as JSON, sent down to every member - or why not. */
    send(text) {
        var message;
        try { message = JSON.parse(text); }
        catch (e) { this._say("Not JSON: " + e.message); return false; }
        var why = MessagingParty.check(this._party.type, message);
        if (why) { this._say(why); return false; }
        this._party.send(message);
        this._say("");
        return true;
    }

    /** A kind's template: each field at a blank of its type. */
    static template(type, kind) {
        var shape = type.kinds[kind], m = { kind: kind };
        Object.keys(shape).forEach(function (f) { m[f] = shape[f] === "number" ? 0 : shape[f] === "boolean" ? false : ""; });
        return m;
    }

    static _option(branch, name, value, text) {
        var o = branch.createElement(name, "option");
        o.value = value;
        o.textContent = text;
        return o;
    }

    activate() { Keys.claim(this.focus); }

    keyDown(ev) {
        if (ev.key === "Escape") { Keys.yield(this.focus); return true; }
        return false;
    }

    /** Why a message did not go, beside the button - marked as an error while there is one. */
    _say(text) {
        this._said.textContent = text;
        css.toggleClass(this._said, wb_sim_error, !!text);
    }

    _showMembers() {
        var ms = this._party.members();
        this._members.textContent = ms.length
            ? "Members: " + ms.map(function (m) { return m.name + " (hears " + (m.hears.join(", ") || "nothing") + ")"; }).join("; ")
            : "No members";
    }

    /**
     * A passage, as a line: the last KEPT kept, drawn once for all the passages
     * of one dispatch - when it is done, in a microtask, so the log is right
     * whether the page is on view or not.
     */
    _heard(p) {
        var who = p.dir === "up" || p.dir === "joined" || p.dir === "left" ? p.name : p.dir === "down" ? "to " + p.name : p.from || "";
        this._lines.push({ n: ++this._seq, dir: p.dir, who: who || "", what: (p.message ? JSON.stringify(p.message) : "") + (p.reason ? "  " + p.reason : "") });
        if (this._lines.length > PartySimulator.KEPT) this._lines.shift();
        if (p.dir === "joined" || p.dir === "left") this._showMembers();
        if (this._pending) return;
        this._pending = true;
        var self = this;
        queueMicrotask(function () { self._draw(); });
    }

    _draw() {
        this._pending = false;
        if (!this._offParty) return;   // disposed while a draw was pending
        var log = this._log, atEnd = log.scrollTop + log.clientHeight >= log.scrollHeight - 2;
        if (this._rows) this.branch.dissolveBranch(this._rows.name);
        var rows = this.branch.createBranch("rows");
        rows.activate(_simulatorOwner);
        this._rows = rows;
        while (log.firstChild) log.removeChild(log.firstChild);
        var seq = 0;
        function span(cls, text) { var s = rows.createElement("s" + (++seq), "span"); css.addClass(s, cls); s.textContent = text; return s; }
        this._lines.forEach(function (l) {
            var el = rows.createElement("r" + (++seq), "div");
            css.addClass(el, fm_row);
            el.appendChild(span(fm_kind, l.n + " " + l.dir));
            el.appendChild(span(fm_name, l.who));
            if (l.what) el.appendChild(span(fm_component, l.what));
            log.appendChild(el);
        });
        if (atEnd) log.scrollTop = log.scrollHeight;
    }

    /** It stops listening and leaves; the branch is the tab-pane's, dissolved by it. */
    dispose() {
        if (this._offParty) { this._offParty(); this._offParty = null; }
        if (this._offKeys) { this._offKeys(); this._offKeys = null; }
        if (this.focus && this.focus.in) this.focus.leave();
    }
}

/** How many lines the log keeps. */
PartySimulator.KEPT = 200;
