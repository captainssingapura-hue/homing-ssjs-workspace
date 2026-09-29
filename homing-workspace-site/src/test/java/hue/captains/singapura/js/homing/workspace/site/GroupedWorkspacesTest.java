package hue.captains.singapura.js.homing.workspace.site;

import hue.captains.singapura.js.homing.core.ParamCodec.Decoded;
import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Query;
import hue.captains.singapura.js.homing.workspace.groups.core.models.Arrangement;
import hue.captains.singapura.js.homing.workspace.groups.core.models.GroupedWorkspace;
import hue.captains.singapura.js.homing.workspace.groups.core.models.SplitGrid;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceGroup;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceGroups;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceArrangements;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceKind;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceDeclaration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A site's workspaces, grouped: each group a page at its address, the root sending
 * to the first, and nothing else - a kind is its group's own, in the page's anchor;
 * every one served filed, every one filed served; and the page's params - the
 * route's group, then which workspace, by id or by name, then a workspace page's.
 */
class GroupedWorkspacesTest {

    record Named(String name) implements WorkspaceDeclaration {
        @Override public List<WidgetDeclaration<?>> kinds() { return List.of(); }
    }

    static final WorkspaceGroups GROUPS = WorkspaceGroups.of(
            WorkspaceGroup.of("demo", "Workspace demo")
                    .section("Together", GroupedWorkspace.of("demo", "Everything"))
                    .section("One set each", GroupedWorkspace.of("books", "Books"), GroupedWorkspace.of("monitors", "Monitors"))
                    .defaultTo(WorkspaceKind.of("demo"))
                    .build(),
            WorkspaceGroup.of("labs", "Labs")
                    .section("Trials", GroupedWorkspace.of("trial", "Trial"))
                    .build());

    static final GroupedWorkspaces SITE = GroupedWorkspaces.of(GROUPS, new Named("demo"), new Named("books"), new Named("monitors"), new Named("trial"));

    private static String at(String path) {
        return SITE.group(Path.parse(path)).map(g -> g.id().value() + " " + GroupedWorkspaces.address(g)).orElse("nowhere");
    }

    @Test
    void eachGroupIsAPageAtItsAddress() {
        assertEquals("demo /demo", at("/demo"));
        assertEquals("labs /labs", at("/labs"));
        assertEquals("demo", SITE.home().id().value(), "the root's: the first group");
    }

    @Test
    void anythingElseIsNowhere_aKindIsItsGroupsOwn() {
        for (String p : List.of("/", "/together/demo", "/one-set-each/books", "/demo/together/demo", "/Demo", "/nope", "/trials/trial")) {
            assertEquals("nowhere", at(p), p);
        }
    }

    @Test
    void theRootSendsToTheFirstGroup_theQueryAndTheAnchorCarried() {
        String sent = GroupedWorkspaces.sentTo("/demo", Query.parse("ws_name=Reading list&theme=editorial")).body();
        assertTrue(sent.contains("window.location.replace(\"\\/demo?ws_name=Reading+list\\u0026theme=editorial\" + window.location.hash)"), sent);
        assertTrue(sent.contains("content=\"0;url=/demo?ws_name=Reading+list&amp;theme=editorial\""), sent);
        assertTrue(GroupedWorkspaces.sentTo("/demo", Query.NONE).body().contains("window.location.replace(\"\\/demo\" + window.location.hash)"));
    }

    @Test
    void everyOneServedIsFiled_andEveryOneFiledServed() {
        var unfiled = assertThrows(IllegalArgumentException.class, () -> GroupedWorkspaces.of(GROUPS, new Named("demo"), new Named("books"), new Named("monitors"), new Named("trial"), new Named("stray")));
        assertTrue(unfiled.getMessage().contains("[stray]"), unfiled.getMessage());
        var unserved = assertThrows(IllegalArgumentException.class, () -> GroupedWorkspaces.of(GROUPS, new Named("demo"), new Named("books")));
        assertTrue(unserved.getMessage().contains("[monitors, trial]"), unserved.getMessage());
        assertThrows(IllegalArgumentException.class, () -> GroupedWorkspaces.of(GROUPS, new Named("demo"), new Named("demo"), new Named("books"), new Named("monitors"), new Named("trial")));
    }

    @Test
    void whatThePageHasOfThem_isGenerated() {
        String manifests = SITE.manifestsJs("SITE_WORKSPACES");
        assertTrue(manifests.startsWith("const SITE_WORKSPACES = Object.freeze({ \"demo\": Object.freeze({ name: \"demo\""), manifests);
        assertTrue(manifests.contains("\"trial\": Object.freeze({ name: \"trial\""), manifests);
        assertTrue(SITE.groupsJs("SITE_GROUPS").contains("path: \"one-set-each/monitors\""));
        assertEquals("demo", SITE.declaration("demo").orElseThrow().name());
    }

    @Test
    void thePagesParams_theRoutesGroup_andTheServersWord_nothingOfWhatIsInsideTheGroup() {
        var codec = GroupedWorkspacePageModule.CODEC;
        var p = codec.from(Map.of("ws_group", List.of("demo"), "ws_server", List.of("on"))).orNull();
        assertEquals(new GroupedWorkspacePageModule.Params("demo", true), p);
        assertEquals(p, codec.from(codec.to(p)).orNull(), "what it writes it reads back");
        assertEquals(new GroupedWorkspacePageModule.Params("demo", false),
                codec.from(Map.of("ws_group", List.of("demo"), "ws_id", List.of("not read"), "ws_name", List.of("nor this"), "ws_kind", List.of("x"))).orNull(),
                "which kind, and which workspace of it, are the anchor's: never read from the query");
        assertInstanceOf(Decoded.Missing.class, codec.from(Map.of()));
        assertInstanceOf(Decoded.Malformed.class, codec.from(Map.of("ws_group", List.of("no such/group"))));
        assertInstanceOf(Decoded.Malformed.class, codec.from(Map.of("ws_group", List.of("demo"), "ws_server", List.of("https://elsewhere"))));
    }

    /** Two regions waiting, side by side: an arrangement of a workspace that opens nothing. */
    private static WorkspaceArrangements<Named> waiting(String kind) {
        var w = new Named(kind);
        return WorkspaceArrangements.of(w, Arrangement.of(w, SplitGrid.of(SplitGrid.row(SplitGrid.region("a"), SplitGrid.region("b")))));
    }

    @Test
    void eachWorkspaceMayBeArranged_onceAndOnlyIfServed() {
        var arranged = SITE.arranged(waiting("books"), waiting("trial"));
        assertTrue(arranged.arrangements("books").isPresent());
        assertTrue(arranged.arrangements("demo").isEmpty(), "arranged by none: it starts with nothing open");
        assertThrows(IllegalArgumentException.class, () -> SITE.arranged(waiting("stray")), "a workspace it does not serve");
        assertThrows(IllegalArgumentException.class, () -> SITE.arranged(waiting("books"), waiting("books")), "two sets for one workspace");
    }

    @Test
    void theSplitGridsArrangementsAreGenerated_byKind() {
        String js = SITE.arranged(waiting("books"), waiting("trial")).arrangementsJs("SITE_ARRANGEMENTS");
        assertTrue(js.startsWith("const SITE_ARRANGEMENTS = Object.freeze({ \"books\": Object.freeze({ engine: \"split-grid\", workspace: \"books\""), js);
        assertTrue(js.contains("\"trial\": Object.freeze({ engine: \"split-grid\""), js);
        assertEquals("const NONE = Object.freeze({});", SITE.arrangementsJs("NONE"));
        assertThrows(IllegalArgumentException.class, () -> SITE.arrangementsJs("lower"));
    }
}
