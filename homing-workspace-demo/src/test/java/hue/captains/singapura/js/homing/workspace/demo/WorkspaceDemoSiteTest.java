package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.core.ParamCodec.Decoded;
import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Query;
import hue.captains.singapura.js.homing.workspace.groups.core.models.SplitGrid;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WidgetRef;
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
 * together, the books, the monitors - one group, one page, each workspace in its
 * anchor; the grouped page handed the demo's manifests and groups. What this checks
 * is the handover; past it, the site's and the shell's.
 */
class WorkspaceDemoSiteTest {

    private static String page(String path, Query query) {
        return WorkspaceDemoSite.INSTANCE.router().resolve(Path.parse(path)).orElseThrow(() -> new AssertionError("nothing at " + path)).html(query).body();
    }

    @Test
    void theGroupIsThePage_itsServerKeepingItsStates() {
        String html = page("/demo", Query.NONE);
        assertTrue(html.contains(DemoWorkspaceApp.class.getCanonicalName()), "the page imports the demo's app and calls its appMain");
        assertTrue(html.contains("appMain(page.main"), "the app is handed the MPA's slot");
        assertTrue(html.contains("trail: page.trail"), "and the trail, to carry on from its anchor");
        assertTrue(html.contains("\"ws_group\":\"demo\""), "the route's group");
        assertTrue(html.contains("\"ws_server\":\"on\""), "the route says the server keeps its states");
        assertFalse(html.contains("ws_id") || html.contains("ws_kind"), "the kind is the anchor's, the workspace the query's");
        assertTrue(html.contains("<title>Workspace demo · Workspace</title>"), "titled by the group, until the page reads its anchor");
    }

    @Test
    void theRootSendsToTheGroup_andAKindHasNoPathOfItsOwn() {
        assertTrue(page("/", Query.NONE).contains("window.location.replace(\"\\/demo\" + window.location.hash)"));
        for (String nowhere : List.of("/elsewhere", "/together/demo", "/one-set-each/books", "/one-set-each", "/demo/together/demo")) {
            assertFalse(WorkspaceDemoSite.INSTANCE.router().resolve(Path.parse(nowhere)).isPresent(), nowhere);
        }
    }

    @Test
    void theQueryNamesOneWorkspaceOfTheKind_byIdOrByName_theRouteItsGroup() {
        String id = "0f1b6c2e-5000-4000-8f1b-6c2e00000001";
        String html = page("/demo", Query.parse("ws_id=" + id + "&ws_group=elsewhere&ws_kind=monitors"));
        assertTrue(html.contains("\"ws_id\":\"" + id + "\""), "which workspace of the kind: the address's");
        assertTrue(html.contains("\"ws_group\":\"demo\""), "which group: the route's, whatever the address says");
        assertFalse(html.contains("ws_kind"), "a kind in the query is nothing");
        assertTrue(page("/demo", Query.parse("ws_name=Reading%20list")).contains("\"ws_name\":\"Reading list\""), "or what it is called");
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

    /** A grouped page's params: the group, then which workspace of the anchor's kind - by the id its log is kept under, or by name - and the server's word, only "on". */
    @Test
    void theParams_theGroupThenOneWorkspace_andTheServersWord() {
        String id = "0f1b6c2e-5000-4000-8f1b-6c2e00000001";
        var codec = GroupedWorkspacePageModule.CODEC;
        assertTrue(codec.from(Map.of("ws_group", List.of("demo"), "ws_id", List.of(id))).isOk());
        assertTrue(codec.from(Map.of("ws_group", List.of("demo"), "ws_name", List.of("Reading list"))).isOk());
        assertTrue(codec.from(Map.of("ws_group", List.of("demo"))).isOk(), "no id, no name: the kind's own");
        assertInstanceOf(Decoded.Missing.class, codec.from(Map.of("ws_id", List.of(id))));
        assertInstanceOf(Decoded.Malformed.class, codec.from(Map.of("ws_group", List.of("demo"), "ws_id", List.of(id.toUpperCase()))));
        assertInstanceOf(Decoded.Malformed.class, codec.from(Map.of("ws_group", List.of("demo"), "ws_server", List.of("https://elsewhere"))));
    }

    /** Each workspace's first state in the split grid: the switcher on the left, its own widgets beside it. */
    @Test
    void eachWorkspaceIsArranged_theSwitcherOnTheLeft() {
        for (String kind : List.of("demo", "books", "monitors")) {
            var grid = DemoGroups.SITE.arrangements(kind).orElseThrow().forEngine(SplitGrid.ENGINE, SplitGrid.class).orElseThrow().placement();
            assertEquals(List.of("switcher"), grid.regions().get(0).tabs().stream().map(WidgetRef::value).toList(), kind);
        }
        var books = DemoArrangements.BOOKS.arrangements().get(0);
        assertEquals(List.of("kinds", "books", "chosen"), ((SplitGrid) books.placement()).regions().stream().map(r -> r.name().value()).toList());
        String js = String.join("\n", DemoArrangementsModule.INSTANCE.selfContent(null));
        assertTrue(js.contains("const DEMO_ARRANGEMENTS = Object.freeze({ \"demo\": Object.freeze({ engine: \"split-grid\", workspace: \"demo\""), js);
        assertTrue(js.contains("\"books\": Object.freeze({ engine: \"split-grid\", workspace: \"books\""), js);
        assertTrue(js.contains("\"monitors\": Object.freeze({ engine: \"split-grid\", workspace: \"monitors\""), js);
    }
}
