package hue.captains.singapura.js.homing.workspace.demowidgets;

import hue.captains.singapura.js.homing.core.ParamCodec.Decoded;
import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Query;
import hue.captains.singapura.js.homing.workspace.shell.WorkspacePageModule;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The widget set runs on its own: a site whose one page is the books
 * workspace, the shell's page handed this set's manifest. What this checks is
 * the handover - the page names this set's app, which hands the manifest on;
 * past that it is the shell's.
 */
class BooksWorkspaceSiteTest {

    private static String pageAt(String path) {
        var found = BooksWorkspaceSite.INSTANCE.router().resolve(Path.parse(path));
        assertTrue(found.isPresent(), "no page at " + path);
        return found.get().html(Query.NONE).body();
    }

    @Test
    void theRootIsTheBooksWorkspace_itsServerKeepingItsStates() {
        String html = pageAt("/");
        assertTrue(html.contains(BooksWorkspaceApp.class.getCanonicalName()), "the page imports this set's app and calls its appMain");
        assertTrue(html.contains("appMain(page.main"), "the app is handed the MPA's slot");
        assertTrue(html.contains("\"ws_server\":\"on\""), "the route says the server keeps its states");
        assertFalse(html.contains("ws_id"), "the route keeps the kind's own workspace");
        assertFalse(BooksWorkspaceSite.INSTANCE.router().resolve(Path.parse("/elsewhere")).isPresent());
    }

    @Test
    void theManifestIsTheDeclarations() {
        String js = String.join("\n", BooksWorkspaceModule.INSTANCE.selfContent(null));
        assertTrue(js.contains("const BOOKS_WORKSPACE = Object.freeze({ name: \"books\""), js);
        for (String kind : List.of("\"books-grid\"", "\"book-jumbotron\"", "\"book-browser\"")) assertTrue(js.contains(kind), kind + " in " + js);
    }

    /** A workspace page's params: which workspace of the kind, by the id its log is kept under, and the server's word - only "on". */
    @Test
    void theAddressNamesOneWorkspace_andTheServersWord() {
        String id = "0f1b6c2e-5000-4000-8f1b-6c2e00000001";
        assertTrue(WorkspacePageModule.CODEC.from(Map.of("ws_id", List.of(id))).isOk());
        assertTrue(WorkspacePageModule.CODEC.from(Map.of()).isOk(), "none: the kind's own");
        assertInstanceOf(Decoded.Malformed.class, WorkspacePageModule.CODEC.from(Map.of("ws_id", List.of(id.toUpperCase()))));
        assertInstanceOf(Decoded.Malformed.class, WorkspacePageModule.CODEC.from(Map.of("ws_server", List.of("https://elsewhere"))));
    }
}
