package hue.captains.singapura.js.homing.workspace.site;

import hue.captains.singapura.js.homing.core.ParamCodec.Decoded;
import hue.captains.singapura.js.homing.site.Path;
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
 * A site's workspaces, grouped: each served where its group files it, the root at
 * the first group's default; every one served filed, every one filed served; and
 * the page's params - the route's kind, then a workspace page's.
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
        return SITE.place(Path.parse(path)).map(p -> p.group().id().value() + " " + p.section().slug().value() + " " + p.workspace().kind().value() + " " + p.path()).orElse("nowhere");
    }

    @Test
    void eachIsServedWhereItsGroupFilesIt_theRootAtTheFirstGroupsDefault() {
        assertEquals("demo together demo together/demo", at("/"));
        assertEquals("demo together demo together/demo", at("/together/demo"));
        assertEquals("demo one-set-each books one-set-each/books", at("/one-set-each/books"));
        assertEquals("labs trials trial trials/trial", at("/trials/trial"));
    }

    @Test
    void anythingElseIsNowhere() {
        for (String p : List.of("/together", "/together/books", "/trials/demo", "/together/demo/more", "/nope/nope", "/together/Demo")) {
            assertEquals("nowhere", at(p), p);
        }
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
    void thePagesParams_theRoutesKindThenAWorkspacePages() {
        var codec = GroupedWorkspacePageModule.CODEC;
        String id = "0f1b6c2e-5000-4000-8f1b-6c2e00000001";
        var p = codec.from(Map.of("ws_kind", List.of("books"), "ws_id", List.of(id), "ws_server", List.of("on"))).orNull();
        assertEquals(new GroupedWorkspacePageModule.Params("books", id, true), p);
        assertEquals(p, codec.from(codec.to(p)).orNull(), "what it writes it reads back");
        assertInstanceOf(Decoded.Missing.class, codec.from(Map.of()));
        assertInstanceOf(Decoded.Malformed.class, codec.from(Map.of("ws_kind", List.of("no such/kind"))));
        assertInstanceOf(Decoded.Malformed.class, codec.from(Map.of("ws_kind", List.of("books"), "ws_id", List.of(id.toUpperCase()))));
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
