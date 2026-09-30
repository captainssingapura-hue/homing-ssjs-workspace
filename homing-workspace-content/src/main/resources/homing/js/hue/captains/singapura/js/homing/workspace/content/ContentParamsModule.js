// =============================================================================
// ContentParams — a widget's params as a content party carries them: a list of
// { name, value }, in the order of their names, so the same params are the
// same list wherever they are asked from; and the one text an item is kept
// under. Every value is text, as an arrangement's params are.
//
//   ContentParams.of({ key: "design/keys", doc: "/notes" }) → [{ name: "doc", … }, { name: "key", … }]
//   ContentParams.object(list) → { doc: "/notes", key: "design/keys" }
//   ContentParams.key(list)    → "doc=%2Fnotes&key=design%2Fkeys": the same for the same params, in any order
//   ContentParams.same(a, b)   → whether two lists are the same params
//
// Pure: no DOM, no clock.
// =============================================================================

class ContentParams {

    static of(params) {
        var p = params || {};
        return Object.keys(p).sort().map(function (name) { return { name: name, value: String(p[name]) }; });
    }

    static object(list) {
        var out = {};
        (list || []).forEach(function (p) { out[p.name] = p.value; });
        return out;
    }

    static key(list) {
        return (list || []).slice().sort(function (a, b) { return a.name < b.name ? -1 : a.name > b.name ? 1 : 0; })
            .map(function (p) { return encodeURIComponent(p.name) + "=" + encodeURIComponent(p.value); }).join("&");
    }

    static same(a, b) { return ContentParams.key(a) === ContentParams.key(b); }
}
