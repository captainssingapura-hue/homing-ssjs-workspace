// =============================================================================
// TreeBenchApp — the tree placement, on the bench: the bench's tree (its
// arrangement generated from BenchTree) laid out by the tree's engine, its
// table of contents beside it - a split grid of two cells, arranged once, the
// divider dragging: the contents on the left, the tree on the right.
//
// The page is the host of both, and where they meet: a section picked in the
// contents is shown in the tree; the section in view in the tree is followed
// in the contents, which tells nothing back. It lends each a cell's box,
// grafts their roots into its own, and holds the one party the tree's
// widgets are given - the book selection, where the books grid and the chosen
// book meet from different sections. Every widget is made from its type and
// params alone.
// =============================================================================

const _treeBenchOwner = Object.freeze({ toString: () => "treeBench" });

function appMain(el, params) {
    css.addClass(el, tb_page);
    var place = domOpsParty.createBranch("treeBench");
    place.activate(_treeBenchOwner);
    var shell = place.createElement("shell", "div");
    css.addClass(shell, tb_shell);
    el.appendChild(shell);
    var grid = new SplitGrid(place.createBranch("grid"), {
        host: shell, minCellPx: 160,
        layout: { kind: "split", orientation: "horizontal", children: [
            { node: { kind: "cell", id: "toc" }, ratio: 1 },
            { node: { kind: "cell", id: "tree" }, ratio: 3 } ] }
    });
    var tocBox = place.createElement("tocBox", "div");
    css.addClass(tocBox, tb_cell);
    grid.cell("toc").appendChild(tocBox);
    var treeBox = place.createElement("treeBox", "div");
    css.addClass(treeBox, tb_cell);
    grid.cell("tree").appendChild(treeBox);

    var books = new MessagingParty(BOOK_SELECTION, BookSelectionSecretary), given = {};
    given[BOOK_SELECTION.name] = books;
    var toc = new TreeToc(tocBox, { arrangement: BENCH_TREE, label: "Contents" });
    var layout = new TreeLayout(treeBox, {
        arrangement: BENCH_TREE,
        kinds: { "params-card": ParamsCard, "books-grid": BooksGrid, "book-jumbotron": BookJumbotron },
        given: given,
        onShown: function (path) { toc.follow(path); }
    });
    toc.onPick(function (path) { layout.show(path); });
    toc.follow(layout.shown());
    place.graft("toc", toc.roots.dom);
    place.graft("tree", layout.roots.dom);
    focusParty.root.graft("toc", toc.roots.focus);
    focusParty.root.graft("tree", layout.roots.focus);
}
