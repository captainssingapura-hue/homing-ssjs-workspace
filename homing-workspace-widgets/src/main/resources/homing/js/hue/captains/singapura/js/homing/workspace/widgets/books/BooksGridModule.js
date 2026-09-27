// =============================================================================
// BooksGrid — the books, in the relation grid: the first widget made to stand
// on its own (the Workspace & Widgets doctrines). It is made with the container
// its page lends it and its params, and nothing else. Its elements are minted
// under a DomOpsParty root of its own, and it is a member of the page's focus
// party by a root of its own — both made here, from the parties as they are
// today, until a host grafts them. Its data is its own too.
//
// The grid takes the browser's own focus in its cells: a native world inside
// a widget that is only ever logically focused. A press anywhere in it claims
// the keys for it; the keys given to it by a call go on into the grid; the
// browser's focus arriving in a cell is the steward's to note, and the widget
// takes nothing from it; Escape the grid did not take gives the keys back.
//
//   new BooksGrid(container, params)
//     container  where it goes: its page's, lent - filled, never shaped. The
//                host positions it, so the widget's root has a box to fill
//     params     as the page carries them, strings, checked where the address was
//                read (BooksGridDeclaration): columns, "title,rating" - which of
//                title, author, year, rating, each once; all when absent - and
//                numbers, "on" for a gutter of row numbers
//   grid.root     its root, in the container
//   grid.focus    its membership of the focus party
//   grid.dispose() both roots gone - the elements and the membership - and nothing left in the container
// =============================================================================

const _booksGridOwner = Object.freeze({ toString: () => "booksGrid" });
var _booksGrids = 0;

class BooksGrid {
    constructor(container, params) {
        if (!container || typeof container.appendChild !== "function") throw new Error("[BooksGrid] a container is required: the one its page lends it");
        var p = params || {};
        var name = "booksGrid-" + (++_booksGrids);
        // ITS OWN DOMOPS ROOT: everything it mints is under it, and goes with it
        this._dom = domOpsParty.createBranch(name);
        this._dom.activate(_booksGridOwner);
        var root = this._dom.createElement("root", "div");
        css.addClass(root, wg_fill);
        var box = this._dom.createElement("box", "div");
        css.addClass(box, wg_scroll);
        root.appendChild(box);
        this.root = root;
        // its own data: the books, and the relation the grid is given over them
        this._store = new BooksStore();
        this._relation = new BooksRelation(this._store, { branch: this._dom.createBranch("cells") });
        this._grid = new RelGrid({ container: box, branch: this._dom.createBranch("grid"), relation: this._relation,
                                   header: { show: true, sticky: true }, rowNumbers: p.numbers === "on",
                                   columnView: BooksGrid._columns(p.columns), label: "Books" });
        container.appendChild(root);
        // ITS OWN FOCUS ROOT: a member of the page's focus party, joined by itself
        this.focus = focusParty.root.join(name, this);
        this._off = Keys.claimOn(root, this.focus);
    }

    /** The columns the address named, in its order; all of them when it named none. */
    static _columns(text) {
        if (!text) return undefined;
        return String(text).split(",").map(function (c) { return c.trim(); }).filter(function (c) { return c.length > 0; });
    }

    /** Asked for the keys: they are claimed, and go on into the grid. */
    activate() { Keys.claim(this.focus); }

    /** Given the keys: into the grid - unless the browser's focus arriving in a cell is what gave them. */
    granted(by) { if (by !== "native") this._grid.focus(); }

    /** Escape the grid did not take gives the keys back. */
    keyDown(ev) {
        if (ev.key === "Escape") { Keys.yield(this.focus); return true; }
        return false;
    }

    dispose() {
        if (this._off) { this._off(); this._off = null; }
        if (this.focus && this.focus.in) this.focus.leave();
        this._grid.destroy();
        this._relation.dispose();
        this._dom.dissolve();
    }
}
