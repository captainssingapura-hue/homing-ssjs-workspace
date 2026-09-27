// =============================================================================
// PartyLog — what the page's parties say, line by line, a monitor widget
// (Monitor): every notice of the focus party — a member joined, left or moved,
// by its path — and every event of the keyboard steward — the keys granted,
// taken, released, offered, withdrawn, the marker moved — the member named by
// its path while the party still has it. The DomOps party says nothing, so
// nothing of it is here. The last `keep` lines are kept, the newest last,
// drawn once a frame however many came, and the end kept in view while the
// view is at the end.
//
//   new PartyLog(container, params)
//     params     as the page carries them, strings, checked where the address was
//                read (PartyLogDeclaration): keep, the lines kept - 10 to 1000;
//                KEEP when absent
//   log.lines()  the lines kept, as text
// =============================================================================

const _partyLogOwner = Object.freeze({ toString: () => "partyLog" });
var _partyLogs = 0;

class PartyLog extends Monitor {
    constructor(container, params) {
        super(container, "partyLog-" + (++_partyLogs), "Party events");
        var p = params || {}, self = this;
        this._keep = p.keep ? Number(p.keep) : PartyLog.KEEP;
        this._lines = [];
        this._said = 0;
        this._pending = false;
        this._rows = null;
        var log = this._dom.createElement("log", "div");
        css.addClass(log, fm_tree);
        log.setAttribute("role", "log");
        log.setAttribute("aria-label", "Party events");
        this.box.appendChild(log);
        this._log = log;
        this._offParty = focusParty.on(function (n) { self._say(n.kind, n.path, ""); });
        this._offSteward = KeyboardStewardInstance.on(function (ev) { self._say(ev.kind, PartyLog._who(ev.id), PartyLog._how(ev)); });
    }

    lines() {
        return this._lines.map(function (l) { return l.n + " " + l.kind + " " + l.who + (l.how ? " " + l.how : ""); });
    }

    /** A member by its path, while the party has it; else by its id. */
    static _who(id) { var m = focusParty.find(id); return m ? m.path : id; }

    /** What an event of the steward says beyond its kind and its member. */
    static _how(ev) {
        if (ev.kind === "Granted") return "by " + ev.by;
        if (ev.kind === "Taken") return ev.by ? "by " + PartyLog._who(ev.by) : "by no one";
        if (ev.kind === "Marked") return ev.state;
        return "";
    }

    _say(kind, who, how) {
        this._lines.push({ n: ++this._said, kind: kind, who: who, how: how });
        if (this._lines.length > this._keep) this._lines.splice(0, this._lines.length - this._keep);
        if (this._pending) return;
        this._pending = true;
        var self = this;
        requestAnimationFrame(function () { self._draw(); });
    }

    /** The lines kept, drawn afresh: the rows minted on a sub-branch dissolved each time. */
    _draw() {
        this._pending = false;
        if (!this._off) return;   // disposed while a frame was pending
        var box = this.box, atEnd = box.scrollTop + box.clientHeight >= box.scrollHeight - 2;
        if (this._rows) this._dom.dissolveBranch(this._rows.name);
        var rows = this._dom.createBranch("rows");
        rows.activate(_partyLogOwner);
        this._rows = rows;
        var log = this._log, seq = 0;
        while (log.firstChild) log.removeChild(log.firstChild);
        function span(cls, text) {
            var s = rows.createElement("s" + (++seq), "span");
            css.addClass(s, cls);
            s.textContent = text;
            return s;
        }
        this._lines.forEach(function (l) {
            var el = rows.createElement("r" + (++seq), "div");
            css.addClass(el, fm_row);
            el.appendChild(span(fm_kind, l.n + " " + l.kind));
            el.appendChild(span(fm_name, l.who));
            if (l.how) el.appendChild(span(fm_component, l.how));
            log.appendChild(el);
        });
        if (atEnd) box.scrollTop = box.scrollHeight;
    }

    dispose() {
        this._offParty();
        this._offSteward();
        super.dispose();
    }
}

/** The lines kept when the params say nothing: the declaration's default. */
PartyLog.KEEP = 200;
