// =============================================================================
// BookJumbotron — the chosen book, big: its title, and its author under it,
// sized by the box it is lent, whatever its size; with no book chosen, it says
// so, quietly. It holds no books of its own. What it shows is what the book
// selection party says, and on joining it asks what is chosen already, so it
// is right however late it joins. Not joined, it shows that nothing is chosen,
// and works alone.
//
// A self-contained widget (the Workspace & Widgets doctrines): made with the
// container its page lends it and its params, and nothing else; its DomOps and
// focus parties its own, offered as roots for its host to graft; the messaging
// parties it needs declared by type, and joined after it is made, explicitly
// (Messaging Parties Are Joined Top-Down).
//
//   new BookJumbotron(container, params)   params: none
//   jumbo.root      its root, in the container
//   jumbo.roots     { dom, focus }: what its host grafts
//   jumbo.parties   the types it joins for its full function: [BOOK_SELECTION]
//   jumbo.join(given)   given: { [type name]: party }; a second join without a leave is refused
//   jumbo.leave()
//   jumbo.shown()   the book it shows, { id, title, author }, or null
//   jumbo.dispose()
// =============================================================================

const _jumbotronOwner = Object.freeze({ toString: () => "bookJumbotron" });
var _jumbotrons = 0;

class BookJumbotron {
    constructor(container, params) {
        if (!container || typeof container.appendChild !== "function") throw new Error("[BookJumbotron] a container is required: the one its page lends it");
        var name = "bookJumbotron-" + (++_jumbotrons);
        this._dom = domOpsParties.mobile(name);
        this._dom.activate(_jumbotronOwner);
        var root = this._dom.createElement("root", "div");
        css.addClass(root, wg_fill);
        css.addClass(root, bj_frame);
        root.setAttribute("role", "region");
        root.setAttribute("aria-label", "The chosen book");
        var stage = this._dom.createElement("stage", "div");
        css.addClass(stage, bj_stage);
        stage.setAttribute("aria-live", "polite");
        this._title = this._dom.createElement("title", "div");
        css.addClass(this._title, bj_title);
        this._author = this._dom.createElement("author", "div");
        css.addClass(this._author, bj_author);
        this._none = this._dom.createElement("none", "div");
        css.addClass(this._none, bj_none);
        this._none.textContent = "No book chosen";
        stage.appendChild(this._title);
        stage.appendChild(this._author);
        stage.appendChild(this._none);
        root.appendChild(stage);
        container.appendChild(root);
        this.root = root;
        this._focusParty = focusParties.mobile(name);
        this.focus = this._focusParty.root.join("jumbotron", this);
        this._off = Keys.claimOn(root, this.focus);
        this.roots = Object.freeze({ dom: this._dom, focus: this._focusParty });
        this.parties = Object.freeze([BOOK_SELECTION]);
        this._selection = null;
        this._joined = false;
        this._show(null);
    }

    join(given) {
        if (this._joined) throw new Error("[BookJumbotron] joined already: leave first");
        this._joined = true;
        var party = given && given[BOOK_SELECTION.name], self = this;
        if (!party) return;
        this._selection = party.join("bookJumbotron", {
            Selected: function (m) { self._show({ id: m.id, title: m.title, author: m.author }); },
            Cleared:  function () { self._show(null); }
        });
        this._selection.tell({ kind: "CurrentRequested" });
    }

    leave() {
        if (this._selection) { this._selection.leave(); this._selection = null; }
        this._joined = false;
    }

    shown() { return this._book; }

    activate() { Keys.claim(this.focus); }

    keyDown(ev) {
        if (ev.key === "Escape") { Keys.yield(this.focus); return true; }
        return false;
    }

    _show(book) {
        this._book = book ? Object.freeze(book) : null;
        this._title.textContent = book ? book.title : "";
        this._author.textContent = book ? book.author : "";
        css.toggleClass(this._none, bj_hidden, !!book);
    }

    dispose() {
        this.leave();
        if (this._off) { this._off(); this._off = null; }
        this._focusParty.dissolve();
        this._dom.dissolve();
    }
}
