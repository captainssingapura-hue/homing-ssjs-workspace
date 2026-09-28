package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.core.ParamCodec.Decoded;
import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Query;
import hue.captains.singapura.js.homing.workspace.shell.WorkspacePageModule;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The demo runs on its own: a site whose one page is the demo workspace, the
 * shell's page handed the demo's manifest - the two widget sets put together.
 * What this checks is the handover; past it, the shell's.
 */
class WorkspaceDemoSiteTest {

    @Test
    void theRootIsTheDemoWorkspace_itsServerKeepingItsStates() {
        var found = WorkspaceDemoSite.INSTANCE.router().resolve(Path.parse("/"));
        assertTrue(found.isPresent());
        String html = found.get().html(Query.NONE).body();
        assertTrue(html.contains(DemoWorkspaceApp.class.getCanonicalName()), "the page imports the demo's app and calls its appMain");
        assertTrue(html.contains("appMain(page.main"), "the app is handed the MPA's slot");
        assertTrue(html.contains("\"ws_server\":\"on\""), "the route says the server keeps its states");
        assertFalse(html.contains("ws_id"), "the route keeps the kind's own workspace");
        assertFalse(WorkspaceDemoSite.INSTANCE.router().resolve(Path.parse("/elsewhere")).isPresent());
    }

    /** Both sets' kinds, in one manifest; the one root party is the books'. */
    @Test
    void theManifestPutsBothWidgetSetsTogether() {
        String js = String.join("\n", DemoWorkspaceModule.INSTANCE.selfContent(null));
        assertTrue(js.contains("const DEMO_WORKSPACE = Object.freeze({ name: \"demo\""), js);
        for (String kind : List.of("\"books-grid\"", "\"book-jumbotron\"", "\"book-browser\"",
                                   "\"focus-tree\"", "\"steward-lamp\"", "\"domops-tree\"", "\"party-log\"")) {
            assertTrue(js.contains(kind), kind + " in " + js);
        }
        assertEquals(1, DemoWorkspace.INSTANCE.rootParties().size(), "the book selection; the monitors join none");
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
