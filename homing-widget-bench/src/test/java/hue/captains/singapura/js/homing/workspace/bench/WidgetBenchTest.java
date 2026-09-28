package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.core.ParamCodec.Decoded;
import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Query;
import hue.captains.singapura.js.homing.workspace.demowidgets.books.BooksGridDeclaration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The bench's page: a kind named on the address, the rest of it the kind's
 * params read by the kind's own query; a page per kind; and nothing in the
 * page the widget is given but its container and its params.
 */
class WidgetBenchTest {

    private static String pageAt(String path, Query query) {
        var found = WidgetBenchSite.INSTANCE.router().resolve(Path.parse(path));
        assertTrue(found.isPresent(), "no page at " + path);
        return found.get().html(query).body();
    }

    @Test
    void theKindIsRequired_andOneTheBenchKnows() {
        assertInstanceOf(Decoded.Missing.class, WidgetBenchApp.CODEC.from(Map.of()));
        var r = assertInstanceOf(Decoded.Malformed.class, WidgetBenchApp.CODEC.from(Map.of("widget", List.of("nope"))));
        assertTrue(r.describe().contains("books-grid"), r.describe());
    }

    @Test
    void theRestOfTheAddressIsTheKindsToRead() {
        var ok = WidgetBenchApp.CODEC.from(Map.of("widget", List.of("books-grid"), "columns", List.of("title,rating"), "numbers", List.of("on")));
        assertTrue(ok.isOk());
        assertEquals(new BooksGridDeclaration.Params(List.of("title", "rating"), true), ok.orNull().params());
        assertEquals(Map.of("widget", List.of("books-grid"), "columns", List.of("title,rating"), "numbers", List.of("on")),
                WidgetBenchApp.CODEC.to(ok.orNull()), "written back as it was read");
        var bad = assertInstanceOf(Decoded.Malformed.class,
                WidgetBenchApp.CODEC.from(Map.of("widget", List.of("books-grid"), "columns", List.of("price"))));
        assertTrue(bad.describe().contains("columns"), "the kind's refusal names its key: " + bad.describe());
    }

    @Test
    void aKindHasAPage_theRootIsTheFirst_andTheAddressIsReadOverItsDefaults() {
        String root = pageAt("/", Query.NONE);
        assertTrue(root.contains("\"widget\":\"books-grid\""), "the root stands the first kind up");
        assertTrue(root.contains(WidgetBenchApp.class.getCanonicalName()));
        assertFalse(pageAt("/books-grid", Query.NONE).contains("numbers"), "at its defaults, the page says nothing of them");
        assertTrue(pageAt("/books-grid", Query.of("numbers", "on")).contains("\"numbers\":\"on\""),
                "the address's query read over the page's defaults");
        assertTrue(WidgetBenchSite.INSTANCE.router().resolve(Path.parse("/nope")).isEmpty());
    }

    @Test
    void thePageGeneratesTheKindsList() {
        var js = String.join("\n", BenchWidgetsModule.INSTANCE.selfContent(null));
        assertTrue(js.contains("\"books-grid\": BooksGrid"), js);
        assertTrue(js.contains("\"nasty-fixed\": NastyFixed"), js);
        assertTrue(js.contains("const BENCH_PARTIES = Object.freeze({ \"books-grid\": Object.freeze([BOOK_SELECTION]), \"book-jumbotron\": Object.freeze([BOOK_SELECTION]), "
                + "\"book-browser\": Object.freeze([BOOK_SELECTION]), \"focus-tree\": Object.freeze([])"), "each kind's declared types: " + js);
    }

    /** The workspace of one pane, as its declaration has it: its kinds but the nasty ones, and one root party of each type they join. */
    @Test
    void theWorkspacesManifestIsItsDeclarations() {
        var js = String.join("\n", BenchWorkspaceModule.INSTANCE.selfContent(null));
        assertTrue(js.contains("const BENCH_WORKSPACE = Object.freeze({ name: \"bench-one-pane\", kinds: Object.freeze({ \"books-grid\": Object.freeze({ Widget: BooksGrid, title: \"Books grid\", parties: Object.freeze([BOOK_SELECTION]) })"), js);
        assertTrue(!js.contains("nasty"), js);
        assertTrue(js.endsWith("parties: Object.freeze([Object.freeze({ type: BOOK_SELECTION, secretary: BookSelectionSecretary })]) });"), js);
    }

    /** The monitors the bar floats, in its order: each its kind, its title, its toggle's mark, its class - kinds the bench also stands up alone. */
    @Test
    void thePageGeneratesTheMonitorsList_eachAKindOfTheBench() {
        var js = String.join("\n", BenchWidgetsModule.INSTANCE.selfContent(null));
        assertTrue(js.contains("const BENCH_MONITORS = Object.freeze([Object.freeze({ kind: \"focus-tree\", title: \"Focus tree\", mark: \"F\", Widget: FocusTree }), "
                + "Object.freeze({ kind: \"steward-lamp\", title: \"Steward lamp\", mark: \"S\", Widget: StewardLamp }), "
                + "Object.freeze({ kind: \"domops-tree\", title: \"DomOps tree\", mark: \"D\", Widget: DomOpsTree }), "
                + "Object.freeze({ kind: \"party-log\", title: \"Party log\", mark: \"P\", Widget: PartyLog })]);"), js);
        assertTrue(WidgetBench.KINDS.containsAll(WidgetBench.MONITORS));
    }

    /** The nasty widget has a page like any kind, and no params: whatever the address says, it is the same. */
    @Test
    void theNastyWidgetStandsUpLikeAnyKind() {
        assertTrue(pageAt("/nasty-fixed", Query.NONE).contains("\"widget\":\"nasty-fixed\""));
        var ok = WidgetBenchApp.CODEC.from(Map.of("widget", List.of("nasty-fixed"), "columns", List.of("price")));
        assertTrue(ok.isOk(), "its query reads nothing, so refuses nothing");
        assertEquals(Map.of("widget", List.of("nasty-fixed")), WidgetBenchApp.CODEC.to(ok.orNull()));
    }

    /** A workspace of one pane has a page of its own beside the kinds', no kind being named so; the kinds' pages stay as they are. */
    @Test
    void theWorkspaceOfOnePane_hasAPageOfItsOwn() {
        String page = pageAt("/workspace", Query.NONE);
        assertTrue(page.contains(WorkspaceBenchApp.class.getCanonicalName()), "the workspace's page, not a kind's");
        assertTrue(WidgetBench.kind("workspace").isEmpty(), "no kind is named so");
        assertTrue(pageAt("/books-grid", Query.NONE).contains("\"widget\":\"books-grid\""));
    }
}
