package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.core.ParamCodec.Decoded;
import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Query;
import hue.captains.singapura.js.homing.workspace.site.GroupedWorkspacePageModule;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The demo runs on its own: a site of grouped workspaces - the three widget sets
 * together, the books, the monitors - each served where its group files it, the
 * grouped page handed the demo's manifests and groups. What this checks is the
 * handover; past it, the site's and the shell's.
 */
class WorkspaceDemoSiteTest {

    private static String page(String path, Query query) {
        return WorkspaceDemoSite.INSTANCE.router().resolve(Path.parse(path)).orElseThrow(() -> new AssertionError("nothing at " + path)).html(query).body();
    }

    @Test
    void theRootIsTheGroupsDefault_theThreeTogether_itsServerKeepingItsStates() {
        String html = page("/", Query.NONE);
        assertTrue(html.contains(DemoWorkspaceApp.class.getCanonicalName()), "the page imports the demo's app and calls its appMain");
        assertTrue(html.contains("appMain(page.main"), "the app is handed the MPA's slot");
        assertTrue(html.contains("\"ws_kind\":\"demo\""), "the root's kind is the group's default");
        assertTrue(html.contains("\"ws_server\":\"on\""), "the route says the server keeps its states");
        assertFalse(html.contains("ws_id"), "the route keeps the kind's own workspace");
    }

    @Test
    void eachWorkspaceIsServedWhereItsGroupFilesIt() {
        assertTrue(page("/together/demo", Query.NONE).contains("\"ws_kind\":\"demo\""));
        assertTrue(page("/one-set-each/books", Query.NONE).contains("\"ws_kind\":\"books\""));
        String monitors = page("/one-set-each/monitors", Query.NONE);
        assertTrue(monitors.contains("\"ws_kind\":\"monitors\""));
        assertTrue(monitors.contains("<title>Monitors · Workspace</title>"), "titled by the workspace");
        for (String nowhere : List.of("/elsewhere", "/together/books", "/one-set-each", "/together/demo/more")) {
            assertFalse(WorkspaceDemoSite.INSTANCE.router().resolve(Path.parse(nowhere)).isPresent(), nowhere);
        }
    }

    @Test
    void theAddressNamesOneWorkspaceOfTheKind_theRouteItsKind() {
        String id = "0f1b6c2e-5000-4000-8f1b-6c2e00000001";
        String html = page("/one-set-each/books", Query.parse("ws_id=" + id + "&ws_kind=monitors"));
        assertTrue(html.contains("\"ws_id\":\"" + id + "\""), "which workspace of the kind: the address's");
        assertTrue(html.contains("\"ws_kind\":\"books\""), "which kind: the route's, whatever the address says");
    }

    /** The three sets' kinds, together; each set on its own with the switcher; the root parties as their kinds join. */
    @Test
    void theManifestsAreTheirDeclarations() {
        String js = String.join("\n", DemoWorkspacesModule.INSTANCE.selfContent(null));
        assertTrue(js.contains("const DEMO_WORKSPACES = Object.freeze({ \"demo\": Object.freeze({ name: \"demo\""), js);
        assertTrue(js.contains("\"books\": Object.freeze({ name: \"books\""), js);
        assertTrue(js.contains("\"monitors\": Object.freeze({ name: \"monitors\""), js);
        for (String kind : List.of("\"books-grid\"", "\"book-jumbotron\"", "\"book-browser\"",
                                   "\"workspace-kinds\"", "\"workspace-instances\"", "\"workspace-switcher\"",
                                   "\"focus-tree\"", "\"steward-lamp\"", "\"domops-tree\"", "\"party-log\"")) {
            assertTrue(DemoWorkspace.INSTANCE.kinds().stream().anyMatch(k -> ("\"" + k.kind() + "\"").equals(kind)), kind + " in the demo");
        }
        assertEquals(2, DemoWorkspace.INSTANCE.rootParties().size(), "the book selection and the workspace choice; the monitors join none");
        assertEquals(2, BooksWorkspace.INSTANCE.rootParties().size(), "the book selection and the workspace choice");
        assertEquals(1, MonitorsWorkspace.INSTANCE.rootParties().size(), "the workspace choice alone");
    }

    /** A grouped page's params: the kind, then which workspace of it by the id its log is kept under, and the server's word - only "on". */
    @Test
    void theParams_theKindThenOneWorkspaceOfIt_andTheServersWord() {
        String id = "0f1b6c2e-5000-4000-8f1b-6c2e00000001";
        var codec = GroupedWorkspacePageModule.CODEC;
        assertTrue(codec.from(Map.of("ws_kind", List.of("books"), "ws_id", List.of(id))).isOk());
        assertTrue(codec.from(Map.of("ws_kind", List.of("books"))).isOk(), "no id: the kind's own");
        assertInstanceOf(Decoded.Missing.class, codec.from(Map.of("ws_id", List.of(id))));
        assertInstanceOf(Decoded.Malformed.class, codec.from(Map.of("ws_kind", List.of("books"), "ws_id", List.of(id.toUpperCase()))));
        assertInstanceOf(Decoded.Malformed.class, codec.from(Map.of("ws_kind", List.of("books"), "ws_server", List.of("https://elsewhere"))));
    }
}
