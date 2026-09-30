// =============================================================================
// TreeBenchApp — the tree placement, on the bench: the bench's tree (its
// arrangement generated from BenchTree) laid out by the tree's engine, in the
// box under a bar of its sections. A section's button shows it; the section in
// view is marked on the bar as the reader scrolls.
//
// The page is the tree's host: it lends the box, grafts the layout's roots
// into its own, and holds the one party the tree's widgets are given - the
// book selection, where the books grid and the chosen book meet from
// different sections. Every widget is made from its type and params alone.
// =============================================================================

const _treeBenchOwner = Object.freeze({ toString: () => "treeBench" });

function appMain(el, params) {
    css.addClass(el, tb_page);
    var place = domOpsParty.createBranch("treeBench");
    place.activate(_treeBenchOwner);
    var bar = place.createElement("bar", "nav");
    css.addClass(bar, tb_bar);
    bar.setAttribute("aria-label", "Sections");
    var box = place.createElement("box", "div");
    css.addClass(box, tb_box);
    el.appendChild(bar);
    el.appendChild(box);
    var books = new MessagingParty(BOOK_SELECTION, BookSelectionSecretary), given = {}, buttons = {};
    given[BOOK_SELECTION.name] = books;
    function mark(path) {
        Object.keys(buttons).forEach(function (p) {
            buttons[p].colour(p === path ? "primary" : "plain");
            buttons[p].el.setAttribute("aria-current", p === path ? "location" : "false");
        });
    }
    var layout = new TreeLayout(box, {
        arrangement: BENCH_TREE,
        kinds: { "params-card": ParamsCard, "books-grid": BooksGrid, "book-jumbotron": BookJumbotron },
        given: given,
        onShown: mark
    });
    place.graft("tree", layout.roots.dom);
    focusParty.root.graft("tree", layout.roots.focus);
    layout.paths().forEach(function (path, i) {
        var builder = new ButtonBuilder(), b = place.createElement("section" + i, builder.tag);
        buttons[path] = builder.label(path === "" ? "top" : path).size(-0.5).plain().onClick(function () { layout.show(path); }).build(b);
        bar.appendChild(b);
    });
    mark(layout.shown());
}
