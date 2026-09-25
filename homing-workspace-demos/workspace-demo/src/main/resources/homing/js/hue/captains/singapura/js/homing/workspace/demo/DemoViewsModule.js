// =============================================================================
// DemoViewsModule — three more widgets for the demo workspace, each a view that
// is not a paragraph: the books as a relation grid, the same books as shelves
// in a relation tree, and a picture.
//
//   new BooksWidget(branch, params, host)     -> { root, activate, dispose }
//   new ShelvesWidget(branch, params, host)   -> { root, activate, dispose }
//   new PictureWidget(branch, params, host)   -> { root, keyDown }
//
// The gallery's docking page shows the same three as a dock's tabs, where each
// was a member of the focus party itself. Here each is a WIDGET and nothing
// more: it is handed a branch, gives back { root }, and the room it runs in is
// the member. None of them touches the tab, the dock or the party.
//
// The grid and the tree are the NATIVE world: their host takes the browser's
// focus and the keys are their own. When the room comes to hold the keys it
// says so by activate(), and the widget puts the focus in its host. Getting
// back OUT is the room's: an Escape the grid did not want leaves the grid, and
// the next gives the keys back to the dock. The picture is the other kind: no
// native control at all, and keys the room hands on - + − 0 zoom it.
//
// The two relation views look at one store (BooksStore.shared()), so two of
// them open side by side are the same books: a rating edited in the grid is
// the line in the tree, at once.
// =============================================================================

const _viewsOwner = Object.freeze({ toString: () => "demoView" });

/** A relation view: a box that fills the room, the component in it, and the domain's relation over the page's store. */
class _RelView {
    constructor(branch) {
        branch.activate(_viewsOwner);
        var root = branch.createElement("view", "div");
        css.addClass(root, dw_fill);
        var box = branch.createElement("box", "div");
        css.addClass(box, dw_host);
        root.appendChild(box);
        this.root = root;
        this._box = box;
        this._view = null;
        this._relation = null;
    }
    /** The room holds the keys: they are the component's own, so the focus goes where they work. */
    activate() { if (this._view && typeof this._view.focus === "function") this._view.focus(); }
    dispose() {
        if (this._view && this._view.destroy) { try { this._view.destroy(); } catch (e) {} }
        if (this._relation && this._relation.dispose) { try { this._relation.dispose(); } catch (e) {} }
    }
}

/** The books, as the relation grid: titles and ratings edit, the arrows walk the cells. */
class BooksWidget extends _RelView {
    constructor(branch, params) {
        super(branch);
        this._relation = new BooksRelation(BooksStore.shared(), { branch: branch.createBranch("relation") });
        this._view = new RelGrid({ container: this._box, branch: branch.createBranch("grid"), relation: this._relation,
                                   header: { show: true, sticky: true }, label: "Books" });
        this._view.setColumnWidths({ title: 170, author: 120, year: 56, rating: 56 });
    }
}

/** The same books as shelf → book, in the relation tree: an unfold is a question the relation answers. */
class ShelvesWidget extends _RelView {
    constructor(branch, params) {
        super(branch);
        var relation = new ShelfTreeRelation(BooksStore.shared(), { branch: branch.createBranch("relation") });
        this._relation = relation;
        this._view = new RelTree({ container: this._box, branch: branch.createBranch("tree"), relation: relation, label: "Shelves",
                                   folder: true, ask: function (question, mask) { return relation.answer(question, mask); } });
    }
}

/** A picture, and the room it is given: it fits the room, and the keys zoom it — a view that is not text. */
class PictureWidget {
    constructor(branch, params, host) {
        branch.activate(_viewsOwner);
        var p = params || {};
        this._host = host || null;
        this._zoom = 1;
        this._title = p.title || "A plate";
        var root = branch.createElement("view", "div");
        css.addClass(root, dw_fill);
        var box = branch.createElement("box", "div");
        css.addClass(box, dw_picture);
        // drawn with the design's words - a sky, a sun, two hills, the ground - so it is themed and carries no colour of its own
        var plate = branch.createElement("plate", "div");
        css.addClass(plate, dw_plate);
        plate.setAttribute("role", "img");
        plate.setAttribute("aria-label", this._title + ": a sun over two hills");
        var parts = [dw_plate_sun, dw_plate_far, dw_plate_near, dw_plate_ground];
        ["sun", "far", "near", "ground"].forEach(function (part, i) {
            var e = branch.createElement(part, "div");
            css.addClass(e, parts[i]);
            plate.appendChild(e);
        });
        box.appendChild(plate);
        root.appendChild(box);
        var note = branch.createElement("note", "p");
        css.addClass(note, dw_picture_note);
        root.appendChild(note);
        this.root = root;
        this._plate = plate;
        this._note = note;
        this._say();
    }
    /** The line under the plate, and the tab's name: both say how far it is zoomed. */
    _say() {
        var pct = Math.round(this._zoom * 100) + "%";
        this._note.textContent = this._title + " — " + pct + "   (with the tab holding the keys: + − to zoom, 0 to fit)";
        if (this._host) this._host.title("Picture · " + pct);
    }
    zoom(z) {
        this._zoom = Math.max(0.25, Math.min(4, z));
        this._plate.style.setProperty("--dw-zoom", String(this._zoom));
        this._say();
        return this;
    }
    /** Offered, not claimed: the room hands on a key it holds. */
    keyDown(ev) {
        if (ev.altKey || ev.ctrlKey || ev.metaKey) return false;
        if (ev.key === "+" || ev.key === "=") { this.zoom(this._zoom * 1.25); return true; }
        if (ev.key === "-") { this.zoom(this._zoom / 1.25); return true; }
        if (ev.key === "0") { this.zoom(1); return true; }
        return false;
    }
}
