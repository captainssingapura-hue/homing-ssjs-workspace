// =============================================================================
// ParamsCard — a stand-in for the tree bench: a card of the params it was
// made with, each name and value, and nothing else - it knows no more than
// its params, so it can show no more. It flows: as tall as its content.
//
//   new ParamsCard(container, params)
//   card.root  card.roots   { dom, focus }   card.dispose()
// =============================================================================

const _paramsCardOwner = Object.freeze({ toString: () => "paramsCard" });
var _paramsCards = 0;

class ParamsCard {

    /** It takes the height its content needs, where a host lays widgets out in a flow. */
    static SIZING = "flow";

    constructor(container, params) {
        if (!container || typeof container.appendChild !== "function") throw new Error("[ParamsCard] a container is required: the one its host lends it");
        var name = "paramsCard-" + (++_paramsCards), p = params || {};
        this._dom = domOpsParties.mobile(name);
        this._dom.activate(_paramsCardOwner);
        this._focusParty = focusParties.mobile(name);
        var root = this._dom.createElement("root", "dl");
        css.addClass(root, tb_card);
        var self = this;
        Object.keys(p).sort().forEach(function (key, i) {
            var dt = self._dom.createElement("key" + i, "dt");
            css.addClass(dt, tb_key);
            dt.textContent = key;
            var dd = self._dom.createElement("value" + i, "dd");
            css.addClass(dd, tb_value);
            dd.textContent = String(p[key]);
            root.appendChild(dt);
            root.appendChild(dd);
        });
        container.appendChild(root);
        this.root = root;
        this.roots = Object.freeze({ dom: this._dom, focus: this._focusParty });
    }

    dispose() {
        this._focusParty.dissolve();
        this._dom.dissolve();
    }
}
