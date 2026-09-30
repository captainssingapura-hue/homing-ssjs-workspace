// =============================================================================
// BenchContentSteward — a steward of the content bench: the one member of a
// content hierarchy that does I/O. Sent a Fetch, it fetches each item from the
// bench's server by its type and params, and tells the party Loaded with what
// came, or Failed with why. It keeps what it fetched - each item, whether it
// came, and how often the server says it has been asked for it - so a page
// can show that each was fetched once.
//
//   new BenchContentSteward(tell, type)   tell: the party's, as the steward; type: note | flow
//   steward.reactors    { Fetch }: what it hears
//   steward.type
//   steward.fetches()   [{ key, ok: true | false | null (on its way), served }] - in the order sent for
// =============================================================================

class BenchContentSteward {
    constructor(tell, type) {
        if (typeof tell !== "function") throw new Error("[BenchContentSteward] hired with the means to tell its party");
        var self = this;
        this.type = type;
        this._tell = tell;
        this._fetches = [];
        this.reactors = Object.freeze({ Fetch: function (m) { m.items.forEach(function (item) { self._fetch(item.params); }); } });
    }

    fetches() { return this._fetches.map(function (f) { return Object.freeze(Object.assign({}, f)); }); }

    _fetch(params) {
        var self = this, at = { key: ContentParams.key(params), ok: null, served: 0 };
        this._fetches.push(at);
        fetch(BenchContentSteward.ROUTE + "?type=" + encodeURIComponent(this.type) + "&" + at.key)
            .then(function (r) {
                if (!r.ok) throw new Error("the bench has no " + self.type + " '" + ContentParams.object(params).key + "' (" + r.status + ")");
                return r.json();
            })
            .then(function (j) {
                at.ok = true;
                at.served = j.served;
                self._tell({ kind: "Loaded", params: params, content: j.content });
            })
            .catch(function (e) {
                at.ok = false;
                self._tell({ kind: "Failed", params: params, why: String(e && e.message || e) });
            });
    }
}

/** Where the bench's server answers. */
BenchContentSteward.ROUTE = "/bench/content";
