// =============================================================================
// BookBrowser — the books and the chosen one: a composed widget, an umbrella
// over two of its own — the books in the grid (BooksGrid), and the chosen
// book, big (BookJumbotron) — side by side, or one above the other when its
// box is narrow.
//
// It is their host (A Widget Is an Operational Unit: host is a role). Each is
// lent a box of the browser's own, and grafted: its DomOps party into the
// browser's, its focus party into the browser's — which the browser offers its
// own host, whole.
//
// Where they meet is a book selection party of the browser's own: a scope
// (Messaging Parties Are Joined Top-Down). The grid tells it a book is chosen,
// and the jumbotron shows it. The scope is linked to the party the browser is
// given, and its secretary (BookBrowserSecretary) keeps the edge: a choice
// made in the scope goes up, as the browser's own word there; a question is
// answered in the scope; and what the party above says is chosen, both are
// told. Joining is top-down: the browser finishes its own — the scope made and
// linked, and the party above asked what is chosen — before its subordinates
// join, the grid first; leaving is the other way round. Joined with nothing
// given, the scope stands alone, and the grid and the jumbotron still meet in
// it; not joined at all, each works alone.
//
//   new BookBrowser(container, params)   params: none
//   browser.root  browser.roots   { dom, focus }: its own, its subordinates' grafted in them
//   (its kind declares, in Java, the union of its subordinates' types: book-selection)
//   browser.join(given)   given: { [type name]: party }; a second join without a leave is refused
//   browser.leave()
//   browser.scope()   its own book selection party, once joined: kept across a leave
//   browser.activate()   the grid is asked for the keys
//   browser.dispose()
// =============================================================================

const _bookBrowserOwner = Object.freeze({ toString: () => "bookBrowser" });
var _bookBrowsers = 0;

class BookBrowser {
    constructor(container, params) {
        if (!container || typeof container.appendChild !== "function") throw new Error("[BookBrowser] a container is required: the one its page lends it");
        var name = "bookBrowser-" + (++_bookBrowsers);
        this._dom = domOpsParties.mobile(name);
        this._dom.activate(_bookBrowserOwner);
        var root = this._dom.createElement("root", "div");
        css.addClass(root, bb_split);
        root.setAttribute("role", "region");
        root.setAttribute("aria-label", "Books, and the chosen one");
        var gridBox = this._dom.createElement("gridBox", "div");
        css.addClass(gridBox, bb_grid);
        var chosenBox = this._dom.createElement("chosenBox", "div");
        css.addClass(chosenBox, bb_chosen);
        root.appendChild(gridBox);
        root.appendChild(chosenBox);
        container.appendChild(root);
        this.root = root;
        // its subordinates, each lent a box of its own, their roots grafted into the browser's
        this._focusParty = focusParties.mobile(name);
        this._grid = new BooksGrid(gridBox, {});
        this._jumbotron = new BookJumbotron(chosenBox, {});
        this._dom.graft("grid", this._grid.roots.dom);
        this._dom.graft("chosen", this._jumbotron.roots.dom);
        this._focusParty.root.graft("grid", this._grid.roots.focus);
        this._focusParty.root.graft("chosen", this._jumbotron.roots.focus);
        this.roots = Object.freeze({ dom: this._dom, focus: this._focusParty });
        this._scope = null;
        this._joined = false;
    }

    /** Joined: its own first - the scope made, linked above, and the party above asked - then its subordinates, in order. */
    join(given) {
        if (this._joined) throw new Error("[BookBrowser] joined already: leave first");
        this._joined = true;
        if (!this._scope) this._scope = new MessagingParty(BOOK_SELECTION, BookBrowserSecretary);
        var above = given && given[BOOK_SELECTION.name];
        if (above) this._scope.link(above, "bookBrowser").tell({ kind: "CurrentRequested" });
        var mine = {};
        mine[BOOK_SELECTION.name] = this._scope;
        this._grid.join(mine);
        this._jumbotron.join(mine);
    }

    /** Left the other way round: its subordinates, the last first, then its own link above. The scope's memory is kept. */
    leave() {
        if (!this._joined) return;
        this._jumbotron.leave();
        this._grid.leave();
        this._scope.unlink();
        this._joined = false;
    }

    scope() { return this._scope; }

    /** Asked for the keys: its grid is asked. A host never claims for what it holds. */
    activate() { this._grid.activate(); }

    dispose() {
        this.leave();
        this._jumbotron.dispose();
        this._grid.dispose();
        this._focusParty.dissolve();
        this._dom.dissolve();
    }
}
