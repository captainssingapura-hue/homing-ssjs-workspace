// =============================================================================
// PartyTraffic — bench tooling: what passes between a page's content parties,
// line by line, each named by the label it was given - a member telling its
// party, the party telling a member, a send up a link, a steward hired, a send
// that went nowhere - and over the lines, a summary of each party: whose
// steward it has (hired, above, none), what it holds, waits for and failed;
// and for each steward hired, what it fetched and how often the server says it
// was asked for each. Drawn again on every passage.
//
//   new PartyTraffic(container, { parties: [{ label, party }] })
//   traffic.root  traffic.roots   { dom }
//   traffic.lines() → the log's lines   traffic.summary() → the summary's lines
//   traffic.dispose()   it stops listening
// =============================================================================

const _partyTrafficOwner = Object.freeze({ toString: () => "partyTraffic" });
var _partyTraffics = 0;

class PartyTraffic {
    constructor(container, opts) {
        if (!container || typeof container.appendChild !== "function") throw new Error("[PartyTraffic] a container is required");
        var self = this;
        this._parties = (opts && opts.parties) || [];
        this._dom = domOpsParties.mobile("partyTraffic-" + (++_partyTraffics));
        this._dom.activate(_partyTrafficOwner);
        var root = this._dom.createElement("root", "section");
        css.addClass(root, cb_traffic);
        root.setAttribute("aria-label", "Party traffic");
        var title = this._dom.createElement("title", "h2");
        css.addClass(title, cb_panel_title);
        title.textContent = "Traffic";
        this._summaryEl = this._dom.createElement("summary", "p");
        css.addClass(this._summaryEl, cb_lines);
        this._logEl = this._dom.createElement("log", "p");
        css.addClass(this._logEl, cb_lines);
        css.addClass(this._logEl, cb_log);
        this._logEl.setAttribute("role", "log");
        root.appendChild(title);
        root.appendChild(this._summaryEl);
        root.appendChild(this._logEl);
        container.appendChild(root);
        this.root = root;
        this.roots = Object.freeze({ dom: this._dom });
        this._lines = [];
        this._offs = this._parties.map(function (p) { return p.party.on(function (passage) { self._passage(p.label, passage); }); });
        this._draw();
    }

    lines() { return this._lines.slice(); }

    summary() { return this._summary().slice(); }

    dispose() {
        this._offs.forEach(function (off) { off(); });
        this._offs = [];
        this._dom.dissolve();
    }

    _passage(label, p) {
        var what = PartyTraffic._what(p);
        if (what === null) return;
        this._lines.push(String(this._lines.length + 1).padStart(3, " ") + "  " + label.padEnd(12, " ") + what);
        this._draw();
    }

    /** A passage, said; null for those not worth a line. */
    static _what(p) {
        var m = p.message ? PartyTraffic._message(p.message) : "";
        if (p.dir === "up") return "<- " + p.name + ": " + m;
        if (p.dir === "down") return "-> " + p.name + ": " + m;
        if (p.dir === "in") return "<- from above: " + m;
        if (p.dir === "out") return "^  up the link: " + m;
        if (p.dir === "hired") return "hired its steward";
        if (p.dir === "linked") return "linked above, as " + p.name;
        if (p.dir === "unrouted") return "no steward for: " + m;
        if (p.dir === "refused") return "refused: " + p.reason;
        if (p.dir === "threw") return "threw: " + p.reason;
        return null;
    }

    /** A message by its kind and what it is about: the items' keys. */
    static _message(m) {
        var about = m.params ? PartyTraffic._key(m.params) : m.items ? m.items.map(function (i) { return PartyTraffic._key(i.params); }).join(", ") : "";
        return m.kind + (about ? " " + about : "");
    }

    static _key(params) { var o = ContentParams.object(params); return o.key !== undefined ? o.key : ContentParams.key(params); }

    _summary() {
        var out = [];
        this._parties.forEach(function (p) {
            var i = p.party.inspect(), s = i.state;
            out.push(p.label.padEnd(12, " ") + "steward " + i.steward.padEnd(8, " ") + "held " + Object.keys(s.held).length
                     + "  pending " + Object.keys(s.pending).length + "  failed " + Object.keys(s.failed).length);
            var steward = p.party.steward();
            if (steward && typeof steward.fetches === "function") {
                steward.fetches().forEach(function (f) {
                    out.push("  " + p.label.padEnd(10, " ") + "fetched " + (ContentParams.object(PartyTraffic._params(f.key)).key || f.key).padEnd(9, " ")
                             + (f.ok === null ? "on its way" : f.ok ? "served " + f.served + "x" : "failed"));
                });
            }
        });
        return out;
    }

    /** A key's params back, from its text. */
    static _params(key) {
        return key.split("&").filter(Boolean).map(function (kv) {
            var at = kv.indexOf("=");
            return { name: decodeURIComponent(kv.slice(0, at)), value: decodeURIComponent(kv.slice(at + 1)) };
        });
    }

    _draw() {
        this._summaryEl.textContent = this._summary().join("\n");
        this._logEl.textContent = this._lines.join("\n");
        this._logEl.scrollTop = this._logEl.scrollHeight;
    }
}
