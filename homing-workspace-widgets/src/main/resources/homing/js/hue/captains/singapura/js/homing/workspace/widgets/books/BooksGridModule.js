// =============================================================================
// BooksGrid — the books, in the relation grid: the first widget made to stand
// on its own (the Workspace & Widgets doctrines). It is made with the container
// its page lends it and its params, and nothing else. Its elements are minted
// in a DomOpsParty of its own — a mobile party, from the party of parties —
// which it offers its host as roots.dom, for the host to graft into the page's
// tree where the host decides; and it is the member of a focus party of its own
// — a mobile one too — offered as roots.focus, for the host to graft where the
// keys should reach it. Its data is its own too.
//
// The grid takes the browser's own focus in its cells: a native world inside
// a widget that is only ever logically focused. A press anywhere in it claims
// the keys for it; the keys given to it by a call go on into the grid; the
// browser's focus arriving in a cell is the steward's to note, and the widget
// takes nothing from it; Escape the grid did not take gives the keys back.
//
// Joined to a book selection party (Messaging Parties Are Joined Top-Down),
// the grid's cursor is the choice: a move to another book tells the party it
// is chosen — its id, its title and its author, as the store has them now, and
// again when its title is edited — and what the party says is chosen, the
// cursor moves to. On joining it asks what is chosen already. Not joined, it
// works alone.
//
//   new BooksGrid(container, params)
//     container  where it goes: its page's, lent - filled, never shaped. The
//                host positions it, so the widget's root has a box to fill
//     params     as the page carries them, strings, checked where the address was
//                read (BooksGridDeclaration): columns, "title,rating" - which of
//                title, author, year, rating, each once; all when absent - and
//                numbers, "on" for a gutter of row numbers
//   grid.root     its root, in the container
//   grid.roots    what its host grafts: { dom, focus } - its own DomOps party and focus
//                 party, each a stray until grafted
//   grid.focus    its membership of its own focus party
//   grid.parties  the messaging parties' types it joins for its full function: [BOOK_SELECTION]
//   grid.join(given)  given: { [type name]: party }; a second join without a leave is refused
//   grid.leave()
//   grid.dispose() it leaves; both parties dissolved, each proxy with its party, and nothing left in the container
// =============================================================================

const _booksGridOwner = Object.freeze({ toString: () => "booksGrid" });
var _booksGrids = 0;

class BooksGrid {
    constructor(container, params) {
        if (!container || typeof container.appendChild !== "function") throw new Error("[BooksGrid] a container is required: the one its page lends it");
        var p = params || {};
        var name = "booksGrid-" + (++_booksGrids);
        // ITS OWN DOMOPS PARTY: everything it mints is in it, and goes with it; its host grafts it
        this._dom = domOpsParties.mobile(name);
        this._dom.activate(_booksGridOwner);
        var root = this._dom.createElement("root", "div");
        css.addClass(root, wg_fill);
        var box = this._dom.createElement("box", "div");
        css.addClass(box, wg_scroll);
        root.appendChild(box);
        this.root = root;
        // its own data: the books, and the relation the grid is given over them
        var self = this, columns = BooksGrid._columns(p.columns);
        this._store = new BooksStore();
        this._relation = new BooksRelation(this._store, { branch: this._dom.createBranch("cells") });
        // the book selection party, while joined: its membership, and the book last told or heard
        this.parties = Object.freeze([BOOK_SELECTION]);
        this._selection = null;
        this._chosen = null;
        this._joined = false;
        this._firstColumn = (columns || this._store.columns())[0];
        this._grid = new RelGrid({ container: box, branch: this._dom.createBranch("grid"), relation: this._relation,
                                   header: { show: true, sticky: true }, rowNumbers: p.numbers === "on",
                                   columnView: columns, label: "Books",
                                   onCursorMoved: function (pk) { if (self._selection && pk !== self._chosen) self._tell(pk); } });
        this._offStore = this._store.subscribe(function (pk, col) { if (self._selection && pk === self._chosen && col === "title") self._tell(pk); });
        container.appendChild(root);
        // ITS OWN FOCUS PARTY: it is the member of it; its host grafts it where the keys should reach it
        this._focusParty = focusParties.mobile(name);
        this.focus = this._focusParty.root.join("grid", this);
        this._off = Keys.claimOn(root, this.focus);
        this.roots = Object.freeze({ dom: this._dom, focus: this._focusParty });
    }

    /** The columns the address named, in its order; all of them when it named none. */
    static _columns(text) {
        if (!text) return undefined;
        return String(text).split(",").map(function (c) { return c.trim(); }).filter(function (c) { return c.length > 0; });
    }

    /** Joined to the parties it needs, given by type: the book selection party, which its cursor tells and follows. */
    join(given) {
        if (this._joined) throw new Error("[BooksGrid] joined already: leave first");
        this._joined = true;
        var party = given && given[BOOK_SELECTION.name], self = this;
        if (!party) return;
        this._selection = party.join("booksGrid", {
            Selected: function (m) { self._chosen = m.id; self._follow(m.id); },
            Cleared:  function () { self._chosen = null; }
        });
        this._selection.tell({ kind: "CurrentRequested" });
    }

    leave() {
        if (this._selection) { this._selection.leave(); this._selection = null; }
        this._chosen = null;
        this._joined = false;
    }

    /** The book at the cursor, told as chosen: its id, and its title and author as the store has them now. */
    _tell(pk) {
        this._chosen = pk;
        this._selection.tell({ kind: "Select", id: String(pk),
                               title: String(this._store.get(pk, "title")), author: String(this._store.get(pk, "author")) });
    }

    /** The cursor moved to the book the party says is chosen - in the column it is in, when it is anywhere. */
    _follow(pk) {
        var at = this._grid.cursor();
        if (at && at.pk === pk) return;
        this._grid.selectCell(pk, at ? at.column : this._firstColumn);
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
        this.leave();
        if (this._offStore) { this._offStore(); this._offStore = null; }
        if (this._off) { this._off(); this._off = null; }
        this._focusParty.dissolve();
        this._grid.destroy();
        this._relation.dispose();
        this._dom.dissolve();
    }
}
