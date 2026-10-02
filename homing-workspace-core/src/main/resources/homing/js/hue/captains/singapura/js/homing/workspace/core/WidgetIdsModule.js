// =============================================================================
// WidgetIds — how a widget's id is made: its PREFIX — the kind, and a concise
// form of its params when it has any — and a SEQUENCE, climbing for each
// prefix, never reused. The dual of WidgetIds.java, rule for rule; the two
// agree (WidgetIdsParityTest).
//
// The concise form: the params' values in the order of their keys, each with
// every run of what is not a letter or a digit made one hyphen and none at its
// ends, joined by underscores — columns=title,rating is title-rating. A value
// with nothing left in it is kept as its hash; a form longer than CONCISE_MAX is
// the hash of the params whole: eight hex digits of FNV-1a over the params
// written key=value&… in the order of their keys, over their UTF-16 code units.
//
//   WidgetIds.prefix(kind, params) → "books-grid", "books-grid_title-rating"
//   WidgetIds.concise(params)      → "" for none
//   WidgetIds.of(prefix, n)        → "books-grid-3"
//   WidgetIds.split(id)            → { prefix, n }, or null when it is not one `of` makes
//   WidgetIds.conciseOf(id)        → the params' concise form in its prefix, "" for none: prefix's inverse
//   WidgetIds.GRAMMAR              the log's WidgetId: a prefix of letters, digits, hyphens and
//                                  underscores, a hyphen, a sequence from 1 of at most nine digits
// =============================================================================

class WidgetIds {

    static prefix(kind, params) {
        if (typeof kind !== "string" || !/^[a-z][a-z0-9-]*$/.test(kind)) throw new Error("a kind is lowercase letters, digits and hyphens, a letter first: '" + kind + "'");
        var concise = WidgetIds.concise(params);
        return concise ? kind + "_" + concise : kind;
    }

    static concise(params) {
        var keys = Object.keys(params || {}).sort();
        if (!keys.length) return "";
        var out = keys.map(function (k) {
            var raw = params[k] == null ? "" : String(params[k]);
            var v = raw.replace(/[^A-Za-z0-9]+/g, "-").replace(/^-+|-+$/g, "");
            return v || WidgetIds.hash(raw);
        }).join("_");
        return out.length > WidgetIds.CONCISE_MAX ? WidgetIds.hash(WidgetIds.written(params)) : out;
    }

    /** The params as one string, key=value&… in the order of their keys: what the hash is taken over. */
    static written(params) {
        return Object.keys(params || {}).sort().map(function (k) { return k + "=" + (params[k] == null ? "" : String(params[k])); }).join("&");
    }

    /** Eight hex digits of FNV-1a (32 bits) over a string's UTF-16 code units. */
    static hash(s) {
        var h = 0x811c9dc5;
        for (var i = 0; i < s.length; i++) {
            h ^= s.charCodeAt(i);
            h = Math.imul(h, 0x01000193);
        }
        return (h >>> 0).toString(16).padStart(8, "0");
    }

    static of(prefix, n) {
        if (!(n >= 1) || Math.floor(n) !== n) throw new Error("a sequence starts at 1: " + n);
        var id = prefix + "-" + n;
        if (!WidgetIds.GRAMMAR.test(id)) throw new Error("'" + id + "' - a prefix of letters, digits, hyphen, underscore; a sequence of at most nine digits");
        return id;
    }

    static conciseOf(id) {
        var s = WidgetIds.split(id);
        if (!s) throw new Error("'" + id + "' is not an id WidgetIds makes");
        var at = s.prefix.indexOf("_");
        return at < 0 ? "" : s.prefix.slice(at + 1);
    }

    static split(id) {
        var m = /^(.+)-([1-9][0-9]{0,8})$/.exec(String(id));
        return m ? Object.freeze({ prefix: m[1], n: Number(m[2]) }) : null;
    }
}

/** The longest concise form kept as it is; a longer one is the params' hash. */
WidgetIds.CONCISE_MAX = 24;

/** The grammar of an id: the log's ids'. */
WidgetIds.GRAMMAR = /^[A-Za-z0-9_-]+-[1-9][0-9]{0,8}$/;
