// =============================================================================
// Books — the books widget's own domain: a small store of books, and the
// relation the grid is given over it. The gallery's (GalleryRelations), taken
// for the widget to hold as its own: a widget that stands on its own brings its
// data with it. DOMAIN CODE: the word "grid" names no component in it; the
// relation answers the grid's questions and owns every cell it hands out.
//
//   new BooksStore()                       twelve books, in memory
//   new BooksRelation(store, { branch })   the grid's relation: one root view, four
//                                          columns, title and rating editable
//
// The relation takes a branch that is ITS OWN, unactivated when handed; it
// activates it, mints every cell on a sub-branch of it, and dispose()
// dissolves it. A cell is a noun the domain owns and the grid only places.
// =============================================================================

var BOOKS = [
    ["middlemarch",  "Middlemarch",                 "George Eliot",      1871, 5, "Fiction"],
    ["solaris",      "Solaris",                     "Stanislaw Lem",     1961, 4, "Fiction"],
    ["left-hand",    "The Left Hand of Darkness",   "Ursula K. Le Guin", 1969, 5, "Fiction"],
    ["hard-times",   "Hard Times",                  "Charles Dickens",   1854, 3, "Fiction"],
    ["origin",       "On the Origin of Species",    "Charles Darwin",    1859, 5, "Science"],
    ["qed",          "QED",                         "Richard Feynman",   1985, 4, "Science"],
    ["cosmos",       "Cosmos",                      "Carl Sagan",        1980, 4, "Science"],
    ["silent-spring","Silent Spring",               "Rachel Carson",     1962, 4, "Science"],
    ["peloponnesian","The Peloponnesian War",       "Thucydides",        -411, 4, "History"],
    ["guns-germs",   "Guns, Germs, and Steel",      "Jared Diamond",     1997, 3, "History"],
    ["sapiens",      "Sapiens",                     "Yuval Noah Harari", 2011, 3, "History"],
    ["roman-empire", "The Decline and Fall",        "Edward Gibbon",     1776, 4, "History"]
];
var COLUMNS  = ["title", "author", "year", "rating"];
var WRITABLE = ["title", "rating"];

class BooksStore {
    constructor() {
        this._rows = new Map();
        this._order = [];
        this._listeners = new Set();
        var self = this;
        BOOKS.forEach(function (b) {
            self._rows.set(b[0], { title: b[1], author: b[2], year: b[3], rating: b[4], shelf: b[5] });
            self._order.push(b[0]);
        });
    }
    pks()     { return this._order.slice(); }
    columns() { return COLUMNS.slice(); }
    writableColumns() { return WRITABLE.slice(); }
    get(pk, col) { var r = this._rows.get(pk); return r ? r[col] : undefined; }
    commit(pk, col, v) {
        if (WRITABLE.indexOf(col) < 0 || !this._rows.has(pk)) return;
        if (col === "rating") { var n = Number(v); v = (v !== "" && isFinite(n)) ? Math.max(0, Math.min(5, Math.round(n))) : this._rows.get(pk).rating; }
        this._rows.get(pk)[col] = v;
        this._listeners.forEach(function (fn) { fn(pk, col, v); });
    }
    subscribe(fn) { var l = this._listeners; l.add(fn); return function () { l.delete(fn); }; }
}

class BooksRelation {
    constructor(store, opts) {
        opts = opts || {};
        if (!opts.branch) throw new Error("[BooksRelation] opts.branch is required: the relation's own");
        var self = this;
        this._store = store;
        this._branch = opts.branch;
        this._cellSeq = 0;
        this._cells = new Map();
        this._branch.activate({ toString: function () { return "BooksRelation"; } });
        this._unsubscribe = store.subscribe(function (pk, col, v) {
            var c = self._cells.get(pk + " " + col);
            if (c) c.set(v);
        });
    }
    view(intent) { return intent ? null : this._store.pks(); }
    columns() { return this._store.columns(); }
    readOnlyColumns() {
        var store = this._store;
        return store.columns().filter(function (c) { return store.writableColumns().indexOf(c) < 0; });
    }
    cellFor(pk, col) {
        var store = this._store;
        if (store.pks().indexOf(pk) < 0) throw new Error("[BooksRelation] no such book: " + pk);
        var k = pk + " " + col, c = this._cells.get(k);
        if (!c) {
            var commit = store.writableColumns().indexOf(col) >= 0
                ? function (text) { store.commit(pk, col, text); }
                : undefined;
            c = new RelGridTextCell({ branch: this._branch.createBranch("c" + (++this._cellSeq)), value: store.get(pk, col), onCommit: commit });
            this._cells.set(k, c);
        }
        return c;
    }
    cellCount() { return this._cells.size; }
    dispose() {
        this._unsubscribe();
        this._cells.forEach(function (c) { c.dispose(); });
        this._cells.clear();
        this._branch.dissolve();
    }
}
