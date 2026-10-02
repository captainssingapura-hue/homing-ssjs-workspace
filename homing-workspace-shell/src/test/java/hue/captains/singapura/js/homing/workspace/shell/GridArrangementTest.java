package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.workspace.groups.core.models.ArrangedWidget;
import hue.captains.singapura.js.homing.workspace.groups.core.models.Arrangement;
import hue.captains.singapura.js.homing.workspace.groups.core.models.SplitGrid;
import hue.captains.singapura.js.homing.workspace.groups.core.models.SplitGrid.Part;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WidgetKind;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceKind;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceSpec;
import org.graalvm.polyglot.PolyglotException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A split grid's arrangement, generated from Java and laid out headless: the
 * grid's own algebra (SplitGridTree) behind a stand-in dock grid, and a stand-in
 * workspace that opens what it is asked. The frame parted from the one region -
 * a row to the right, a column below - each split re-shared by its weights
 * unless parting shared it so already; then the widgets, region by region, each
 * region's tabs in order, the one it shows in front.
 */
class GridArrangementTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/";

    record Spec(String name, Set<WidgetKind> widgetKinds) implements WorkspaceSpec {
        @Override public WorkspaceKind workspaceKind() { return WorkspaceKind.of(name); }
    }

    static final Spec VIDEOS = new Spec("demo", Set.of(WidgetKind.of("video")));
    static final Spec BOOKS = new Spec("books", Set.of(WidgetKind.of("workspace-switcher"), WidgetKind.of("books-grid"),
            WidgetKind.of("book-browser"), WidgetKind.of("book-jumbotron")));

    /** Two videos, side by side. */
    static final Arrangement<Spec, SplitGrid> SIDE_BY_SIDE = Arrangement.of(VIDEOS,
            SplitGrid.of(SplitGrid.row(SplitGrid.region("west", "left"), SplitGrid.region("east", "right"))),
            ArrangedWidget.of("left", "video"), ArrangedWidget.of("right", "video"));

    /** The switcher on the left, a third; on the right the books above the chosen one. */
    static final Arrangement<Spec, SplitGrid> BOOKISH = Arrangement.of(BOOKS,
            SplitGrid.of(SplitGrid.row(Part.of(SplitGrid.region("kinds", "switcher"), 1),
                    Part.of(SplitGrid.column(SplitGrid.region("books", "grid", "browser").showing("browser"), SplitGrid.region("chosen", "jumbotron")), 2))),
            ArrangedWidget.of("switcher", "workspace-switcher"), ArrangedWidget.of("grid", "books-grid", Map.of("numbers", "on")),
            ArrangedWidget.of("browser", "book-browser"), ArrangedWidget.of("jumbotron", "book-jumbotron"));

    @BeforeEach
    void load() {
        loadModule(DIR + "ui/splitgrid/SplitGridTreeModule.js");
        loadModule(DIR + "workspace/shell/GridArrangementModule.js");
        js.eval("js", GridArrangementJs.constant("SIDE_BY_SIDE", SIDE_BY_SIDE));
        js.eval("js", GridArrangementJs.constant("BOOKISH", BOOKISH));
        js.eval("js", """
                var tree, made, log, opened;
                function fresh() { tree = { kind: "cell", id: "main" }; made = 0; log = []; opened = []; }
                var docks = {
                    regions: function () { return SplitGridTree.cells(tree).map(function (id) { return { id: id }; }); },
                    part: function (id, side) { var id2 = "r" + (++made); tree = SplitGridTree.subdivide(tree, id, side, id2); log.push("part " + id + " " + side); return { id: id2 }; },
                    grid: {
                        layout: function () { return SplitGridTree.copy(tree); },
                        setRatios: function (path, ratios) {
                            log.push("share '" + path + "' " + ratios.join(":"));
                            var node = path === "" ? tree : path.split("/").reduce(function (n, i) { return n.children[+i].node; }, tree);
                            var sum = ratios.reduce(function (s, r) { return s + r; }, 0);
                            node.children.forEach(function (c, i) { c.ratio = ratios[i] / sum; });
                        }
                    }
                };
                var ws = { docks: docks, core: { entries: function () { return opened; } },
                           open: function (kind, params, at) {
                               var e = { id: kind + "-" + (opened.length + 1) };
                               opened.push(e);
                               log.push("open " + kind + JSON.stringify(params) + " " + at.slotId + " " + at.how);
                               return e;
                           } };
                function shape(n) {
                    return n.kind === "cell" ? n.id
                         : (n.orientation === "horizontal" ? "row" : "column") + "(" + n.children.map(function (c) { return shape(c.node) + "@" + Math.round(c.ratio * 100); }).join(" ") + ")";
                }
                fresh();
                """);
    }

    private String eval(String code) { return js.eval("js", code).toString(); }

    @Test
    void twoVideosSideBySide_oneRegionPartedToTheRight_eachVideoInFront() {
        eval("var r = GridArrangement.apply(ws, SIDE_BY_SIDE)");
        assertEquals("part main right | open video{} main front | open video{} r1 front", eval("log.join(' | ')"),
                "halves already: no re-sharing");
        assertEquals("row(main@50 r1@50)", eval("shape(tree)"));
        assertEquals("main r1", eval("r.regions.west + ' ' + r.regions.east"));
        assertEquals("video-1,video-2", eval("r.opened.join()"));
    }

    @Test
    void aRowOfWeights_aColumnInIt_theTabsInOrder_theOneShownInFront() {
        eval("GridArrangement.apply(ws, BOOKISH)");
        assertEquals("part main right | share '' 1:2 | part r1 bottom"
                + " | open workspace-switcher{} main front"
                + " | open books-grid{\"numbers\":\"on\"} r1 quiet | open book-browser{} r1 front"
                + " | open book-jumbotron{} r2 front", eval("log.join(' | ')"));
        assertEquals("row(main@33 column(r1@50 r2@50)@67)", eval("shape(tree)"));
    }

    @Test
    void onlyAWorkspaceWithNothingYet_andOnlyASplitGridsArrangement() {
        eval("GridArrangement.apply(ws, SIDE_BY_SIDE)");
        assertThrows(PolyglotException.class, () -> eval("GridArrangement.apply(ws, SIDE_BY_SIDE)"), "arranged already: not its first state");
        eval("fresh()");
        assertThrows(PolyglotException.class, () -> eval("GridArrangement.apply(ws, Object.assign({}, SIDE_BY_SIDE, { engine: 'one-pane' }))"));
        assertEquals("", eval("log.join(' | ')"), "refused before anything moved");
    }

    @Test
    void theConstantIsGeneratedFromItsDeclaration_frozenThrough() {
        String js = GridArrangementJs.constant("SIDE_BY_SIDE", SIDE_BY_SIDE);
        assertTrue(js.startsWith("const SIDE_BY_SIDE = Object.freeze({ engine: \"split-grid\", workspace: \"demo\""), js);
        assertEquals("true true true", eval("[Object.isFrozen(BOOKISH.frame), Object.isFrozen(BOOKISH.frame.parts[1].frame.parts[0].frame.tabs), Object.isFrozen(BOOKISH.widgets.grid.params)].join(' ')"));
        assertEquals("browser", eval("BOOKISH.frame.parts[1].frame.parts[0].frame.shown"));
        assertThrows(IllegalArgumentException.class, () -> GridArrangementJs.constant("side", SIDE_BY_SIDE));
    }
}
