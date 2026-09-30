// =============================================================================
// Flow — a column of widgets, each of a type with params: a slot's parts, in
// order. It knows only the params it was made with: it asks the flow party for
// its parts with them, and makes a widget for each part from the part's type
// and params, and nothing else. What the parts are - prose, code, a note - is
// theirs; it never learns where it sits.
//
// It is their host, as any host is: each part is lent a box of the column,
// its DomOps party grafted into the flow's, its focus party into the flow's,
// and joined to the parties the flow was given - the same ones, so a part
// asks where the flow asked. A part that flows (static SIZING = "flow") is as
// tall as its content; any other fills a box of its own height, which the
// reader may drag. A part that makes widgets of its own is offered the kinds
// the flow was offered. A type it is not offered is said, in its box.
//
//   new Flow(container, params)   params: what the flow party knows it by - key, doc, …
//   flow.root  flow.roots   { dom, focus }: its own, its parts' grafted in them
//   flow.compose(kinds)     the types its parts may be of: { [type]: WidgetClass } - its host's
//   flow.join(given)        given: { [party type name]: party }; its parts asked of the flow party
//   flow.leave()   flow.parts() → the widgets made   flow.dispose()
// =============================================================================

const _flowOwner = Object.freeze({ toString: () => "flow" });
var _flows = 0;

class Flow {

    /** A flow is as tall as its parts. */
    static SIZING = "flow";

    constructor(container, params) {
        if (!container || typeof container.appendChild !== "function") throw new Error("[Flow] a container is required: the one its host lends it");
        var name = "flow-" + (++_flows);
        this._params = ContentParams.of(params);
        this._dom = domOpsParties.mobile(name);
        this._dom.activate(_flowOwner);
        this._focusParty = focusParties.mobile(name);
        var root = this._dom.createElement("root", "div");
        css.addClass(root, fl_column);
        root.setAttribute("role", "group");
        container.appendChild(root);
        this.root = root;
        this.roots = Object.freeze({ dom: this._dom, focus: this._focusParty });
        this._note = this._dom.createElement("note", "p");
        css.addClass(this._note, fl_note);
        root.appendChild(this._note);
        this._say("Its parts are not asked for yet.");
        this._kinds = {};
        this._given = null;
        this._member = null;
        this._parts = [];
        this._made = 0;
        this._shown = false;
    }

    compose(kinds) { this._kinds = kinds || {}; }

    /** Joined: the flow party asked for its parts; what else was given kept for them. */
    join(given) {
        if (this._given) throw new Error("[Flow] joined already: leave first");
        var self = this;
        this._given = given || {};
        var party = this._given[FLOW.name];
        if (!party) { this._say("No flow party here to ask for its parts."); return; }
        this._say("Waiting for its parts.");
        this._member = party.join("flow", {
            Content: function (m) { if (ContentParams.same(m.params, self._params)) self._show(m.content.parts); },
            Unavailable: function (m) { if (ContentParams.same(m.params, self._params)) self._say("Its parts are unavailable: " + m.why); }
        });
        this._member.tell({ kind: "Wanted", params: this._params });
    }

    /** Left: its parts first, the last first, then its own membership. What was made stays. */
    leave() {
        this._parts.slice().reverse().forEach(function (w) { if (typeof w.leave === "function") w.leave(); });
        if (this._member) { this._member.leave(); this._member = null; }
        this._given = null;
    }

    parts() { return this._parts.slice(); }

    dispose() {
        this.leave();
        this._parts.forEach(function (w) { if (typeof w.dispose === "function") w.dispose(); });
        this._parts = [];
        this._focusParty.dissolve();
        this._dom.dissolve();
    }

    /** What it says while it has no parts to show; said away once it has. */
    _say(text) {
        this._note.textContent = text;
        css.toggleClass(this._note, fl_hidden, !text);
    }

    /** Its parts, made once: the first answer is the flow's, and a flow's content does not change. */
    _show(parts) {
        if (this._shown) return;
        this._shown = true;
        var self = this;
        if (!parts.length) { this._say("It has no parts."); return; }
        this._say("");
        parts.forEach(function (part) { self.root.appendChild(self._part(part)); });
    }

    /** A part: a box, and in it the widget made from its type and params, hosted as the flow is. */
    _part(part) {
        var i = this._made++, box = this._dom.createElement("part" + i, "div");
        css.addClass(box, fl_part);
        var Kind = Object.prototype.hasOwnProperty.call(this._kinds, part.type) ? this._kinds[part.type] : null;
        if (!Kind) {
            var note = this._dom.createElement("missing" + i, "p");
            css.addClass(note, fl_note);
            note.textContent = "No widget of the type " + part.type + " here.";
            box.appendChild(note);
            console.error("[Flow] " + note.textContent);
            return box;
        }
        if (Kind.SIZING !== "flow") css.addClass(box, fl_part_fill);
        var widget = new Kind(box, ContentParams.object(part.params)), slot = "p" + i;
        if (widget.roots && widget.roots.dom) this._dom.graft(slot, widget.roots.dom);
        if (widget.roots && widget.roots.focus) this._focusParty.root.graft(slot, widget.roots.focus);
        if (typeof widget.compose === "function") widget.compose(this._kinds);
        if (typeof widget.join === "function") widget.join(this._given);
        this._parts.push(widget);
        return box;
    }
}
