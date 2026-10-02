// =============================================================================
// ContentPanel — a scope on the content bench: a composed widget, as a doc's
// section or a book browser is, with content parties of its own - one of each
// type it is made with, each with the content secretary and no steward - its
// parts made in them. Joined, each of its parties is linked under the party of
// its type it is given, and then its parts join its own: top-down. So what a
// part wants and the panel does not hold goes up through the link, and what
// comes down is kept in the panel too. Given nothing, its parties stand alone
// and tell its parts there is no steward.
//
// It is its parts' host: each lent a box, grafted, offered the kinds it was
// offered - a flow among them makes its own parts - and joined. A part that
// makes parts of its own is captioned with its type and params, a line down
// its side, so the bench shows which cards came through it.
//
//   new ContentPanel(container, { title, types, kinds, parts })
//     types  the content party types it keeps a scope of: [TYPE]
//     kinds  { [type]: WidgetClass } - what its parts may be
//     parts  [{ type, params }] - its parts, in order; params as a widget is made with them
//   panel.root  panel.roots   { dom, focus }
//   panel.join(given)  panel.leave()   panel.scope(name) → its party of that type
//   panel.parts() → the widgets made   panel.dispose()
// =============================================================================

const _contentPanelOwner = Object.freeze({ toString: () => "contentPanel" });
var _contentPanels = 0;

class ContentPanel {
    constructor(container, opts) {
        if (!container || typeof container.appendChild !== "function") throw new Error("[ContentPanel] a container is required: the one its page lends it");
        var o = opts || {}, name = "contentPanel-" + (++_contentPanels), self = this;
        this._dom = domOpsParties.mobile(name);
        this._dom.activate(_contentPanelOwner);
        this._focusParty = focusParties.mobile(name);
        var root = this._dom.createElement("root", "section");
        css.addClass(root, cb_panel);
        root.setAttribute("aria-label", o.title || "A scope");
        var title = this._dom.createElement("title", "h2");
        css.addClass(title, cb_panel_title);
        title.textContent = o.title || "A scope";
        root.appendChild(title);
        container.appendChild(root);
        this.root = root;
        this.roots = Object.freeze({ dom: this._dom, focus: this._focusParty });
        this._scopes = {};
        (o.types || []).forEach(function (t) { self._scopes[t.name] = new MessagingParty(t, ContentSecretary); });
        this._kinds = o.kinds || {};
        this._parts = (o.parts || []).map(function (part, i) { return self._part(part, i); });
        this._joined = false;
    }

    scope(name) { return this._scopes[name] || null; }

    parts() { return this._parts.slice(); }

    /** Joined: its parties linked under those given, then its parts joined to its own. */
    join(given) {
        if (this._joined) throw new Error("[ContentPanel] joined already: leave first");
        this._joined = true;
        var scopes = this._scopes, g = given || {};
        Object.keys(scopes).forEach(function (n) { if (g[n]) scopes[n].link(g[n], "panel"); });
        this._parts.forEach(function (w) { if (w && typeof w.join === "function") w.join(scopes); });
    }

    /** Left the other way round: its parts, the last first, then its links above. What its parties keep, stays. */
    leave() {
        if (!this._joined) return;
        this._parts.slice().reverse().forEach(function (w) { if (w && typeof w.leave === "function") w.leave(); });
        var scopes = this._scopes;
        Object.keys(scopes).forEach(function (n) { scopes[n].unlink(); });
        this._joined = false;
    }

    dispose() {
        this.leave();
        this._parts.forEach(function (w) { if (w && typeof w.dispose === "function") w.dispose(); });
        this._focusParty.dissolve();
        this._dom.dissolve();
    }

    /** A part: a box, and in it the widget made from its type and params, hosted as the panel is. */
    _part(part, i) {
        var box = this._dom.createElement("part" + i, "div"), Kind = this._kinds[part.type];
        css.addClass(box, fl_part);
        this.root.appendChild(box);
        if (!Kind) { box.textContent = "No widget of the type " + part.type + " here."; return null; }
        if (Kind.SIZING !== "flow") css.addClass(box, fl_part_fill);
        if (typeof Kind.prototype.compose === "function") {
            var p = part.params || {}, caption = this._dom.createElement("caption" + i, "p");
            css.addClass(box, cb_group);
            css.addClass(caption, cb_key);
            caption.textContent = part.type + " " + Object.keys(p).sort().map(function (k) { return k + "=" + p[k]; }).join(" ");
            box.appendChild(caption);
        }
        var widget = new Kind(box, part.params || {}), slot = "p" + i;
        if (widget.roots && widget.roots.dom) this._dom.graft(slot, widget.roots.dom);
        if (widget.roots && widget.roots.focus) this._focusParty.root.graft(slot, widget.roots.focus);
        if (typeof widget.compose === "function") widget.compose(this._kinds);
        return widget;
    }
}
