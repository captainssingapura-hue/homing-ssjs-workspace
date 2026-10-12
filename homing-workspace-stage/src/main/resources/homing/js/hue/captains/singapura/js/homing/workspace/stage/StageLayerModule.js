// =============================================================================
// StageLayer — the page's stage, as it looks: one layer over everything, a
// scrim and a frame filling the view. Its head says what is on it and has the
// close; its seat is where an element lent to the stage sits, given the whole
// of it - a box sized by its host, which a widget in it fills. Modal while it
// shows: everything else on the page inert, the focus moved into the seat,
// and given back when it goes to whoever had it. Escape, or the close, asks it
// closed: its owner says what closing means, and takes it down. A BRANCH
// component: its owner makes a sub-branch for it and hands it in; the layer
// goes on the page the first time it shows.
//
//   var layer = new StageLayer(branch, { onClose: fn })
//   layer.show(element, title, restoreTo?)   the element seated - one at a time - and the layer up; the focus
//                                 goes back on hide to restoreTo, else to what had it when shown
//   layer.hide()                  the layer down, the focus given back: the seated element is its owner's to take back first
//   layer.showing() → the element seated, or null      layer.dispose()
// =============================================================================

const _stageLayerOwner = Object.freeze({ toString: () => "stage" });
/** Its title: the house's heading of a widget's title - the fourth level - on its own tag. */
const _TITLE_HEADING = Object.freeze({ LEAF: "heading4" });

/** The stage's close pressed: the stage closed. */
class StageLayerClose {
    constructor(layer) { this._layer = layer; }
    canHandle(type) { return type === EVENT.CLICK; }
    handle() { this._layer._onClose(); }
}

class StageLayer {
    constructor(branch, opts) {
        if (!branch) throw new Error("[StageLayer] a branch of its own is required");
        var o = opts || {}, self = this;
        branch.activate(_stageLayerOwner);
        this.branch = branch;
        this._onClose = typeof o.onClose === "function" ? o.onClose : function () { self.hide(); };
        this._seated = null;
        this._modality = null;
        var layer = branch.createElement("layer", "div");
        css.addClass(layer, sg_layer);
        css.addClass(layer, sg_hidden);
        layer.setAttribute("role", "dialog");
        layer.setAttribute("aria-modal", "true");
        var scrim = branch.createElement("scrim", "div");
        css.addClass(scrim, sg_scrim);
        layer.appendChild(scrim);
        var frame = branch.createElement("frame", "div");
        css.addClass(frame, sg_frame);
        var head = branch.createElement("head", "div");
        css.addClass(head, sg_head);
        this._title = branch.createElement("title", "h4");
        css.addClass(this._title, sg_title);
        new Look(_TITLE_HEADING, ROLE.STANDALONE).wear(this._title);
        this._title.setAttribute("id", "stage-title");
        layer.setAttribute("aria-labelledby", "stage-title");
        head.appendChild(this._title);
        var close = new PlainButton(PlainButtonFactory.create(branch, "close"), { label: "Close", size: -1 });
        close.setOnClick(new StageLayerClose(self));
        close.el.setAttribute("aria-label", "Close the stage");
        head.appendChild(close.el);
        frame.appendChild(head);
        this._seat = branch.createElement("seat", "div");
        css.addClass(this._seat, sg_seat);
        this._seat.setAttribute("tabindex", "-1");
        frame.appendChild(this._seat);
        layer.appendChild(frame);
        layer.addEventListener("keydown", function (ev) {
            if (ev.key === "Escape" && !ev.defaultPrevented && self._seated) { ev.preventDefault(); self._onClose(); }
        });
        this._layer = layer;
    }

    showing() { return this._seated; }

    show(element, title, restoreTo) {
        if (!element) throw new Error("[StageLayer] an element to seat is required");
        if (this._seated) throw new Error("[StageLayer] one at a time: the stage is showing already");
        if (!this._layer.parentNode) document.body.appendChild(this._layer);
        var had = restoreTo || document.activeElement;   // read before seating: a focused element moved is blurred
        this._title.textContent = title || "";
        this._seat.appendChild(element);
        this._seated = element;
        css.toggleClass(this._layer, sg_hidden, false);
        this._modality = new Modality(this._layer, { restoreTo: had });
        this._seat.focus({ preventScroll: true });
    }

    hide() {
        if (!this._seated) return;
        this._seated = null;
        css.toggleClass(this._layer, sg_hidden, true);
        if (this._modality) { this._modality.release(); this._modality = null; }
    }

    dispose() {
        this.hide();
        this.branch.dissolve();
    }
}
