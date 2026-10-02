package hue.captains.singapura.js.homing.workspace.groups.core.models;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A group owns its tree - its headings, their order, which workspace is under
 * which, the default - and holds together when it is made, or is not made; each
 * filed workspace has one path, its section's slug then its name.
 */
class WorkspaceGroupTest {

    static final GroupedWorkspace VIDEO = GroupedWorkspace.of("video-room", "Video Room");
    static final GroupedWorkspace TABLES = GroupedWorkspace.of("tables", "Tables Playground");
    static final GroupedWorkspace ANIMALS = GroupedWorkspace.of("animals", "Animals Playground");

    static WorkspaceGroup apps() {
        return WorkspaceGroup.of("apps", "Apps")
                .section("Media", VIDEO)
                .section("Data", TABLES)
                .section("Games", ANIMALS)
                .build();
    }

    @Test
    void aGroupFilesItsWorkspacesUnderItsSections_inOrder() {
        var g = apps();
        assertEquals(List.of("Media", "Data", "Games"), g.sections().stream().map(Section::title).toList());
        assertEquals(List.of(VIDEO, TABLES, ANIMALS), g.workspaces());
        assertEquals(Optional.of("Data"), g.sectionOf(TABLES.kind()).map(Section::title));
        assertTrue(g.files(ANIMALS.kind()));
        assertFalse(g.files(WorkspaceKind.of("elsewhere")));
    }

    @Test
    void eachFiledWorkspaceHasOnePath_itsSectionThenItsName() {
        var g = apps();
        assertEquals("media/video-room", g.path(VIDEO.kind()).toString());
        assertEquals(List.of("games", "animals"), g.path(ANIMALS.kind()).segments());
        assertEquals(List.of("media/video-room", "data/tables", "games/animals"), g.paths().keySet().stream().map(GroupPath::toString).toList());
        assertEquals(Optional.of(TABLES), g.at("data", "tables"), "an address's two segments, read back");
        assertEquals(Optional.empty(), g.at("media", "tables"), "a workspace under a section it is not filed under is nowhere");
        assertThrows(IllegalArgumentException.class, () -> g.path(WorkspaceKind.of("elsewhere")));
    }

    @Test
    void theDefaultIsTheFirstFiledUnlessSaid_andAlwaysFiledInTheGroup() {
        assertEquals(VIDEO.kind(), apps().defaultKind());
        assertEquals("media/video-room", apps().defaultPath().toString());
        var g = WorkspaceGroup.of("apps", "Apps").section("Media", VIDEO).section("Games", ANIMALS).defaultTo(ANIMALS.kind()).build();
        assertEquals("games/animals", g.defaultPath().toString());
        var e = assertThrows(IllegalArgumentException.class,
                () -> WorkspaceGroup.of("apps", "Apps").section("Media", VIDEO).defaultTo(TABLES.kind()).build());
        assertTrue(e.getMessage().contains("the default 'tables' is not filed in it"), e.getMessage());
    }

    /** Re-filing changes the path, never the kind: the kind is its log's. */
    @Test
    void reFilingAKindChangesItsPath_notItsKind() {
        var elsewhere = WorkspaceGroup.of("examples", "Examples").section("Examples", VIDEO).build();
        assertEquals("examples/video-room", elsewhere.path(VIDEO.kind()).toString());
        assertEquals(apps().path(VIDEO.kind()).kind(), elsewhere.path(VIDEO.kind()).kind());
    }

    @Test
    void aGroupThatDoesNotHoldTogetherIsNotMade() {
        var twice = assertThrows(IllegalArgumentException.class,
                () -> WorkspaceGroup.of("apps", "Apps").section("Media", VIDEO).section("Games", VIDEO).build());
        assertTrue(twice.getMessage().contains("filed twice - under 'Media' and under 'Games'"), twice.getMessage());
        var slug = assertThrows(IllegalArgumentException.class,
                () -> WorkspaceGroup.of("apps", "Apps").section("Games & Toys", VIDEO).section("games-toys", ANIMALS).build());
        assertTrue(slug.getMessage().contains("share the slug 'games-toys'"), slug.getMessage());
        assertThrows(IllegalArgumentException.class, () -> WorkspaceGroup.of("apps", "Apps").build(), "no section");
        assertThrows(IllegalArgumentException.class, () -> WorkspaceGroup.of("apps", "Apps").section("Media").build(), "a section filing nothing");
        assertThrows(IllegalArgumentException.class, () -> WorkspaceGroup.of("apps", " ").section("Media", VIDEO).build(), "a blank title");
        assertThrows(IllegalArgumentException.class, () -> WorkspaceGroup.of("the apps", "Apps"), "an id no address can carry");
    }

    @Test
    void aSectionsSlugIsDerivedFromItsTitle_orGiven() {
        assertEquals("media", SectionSlug.from("Media").value());
        assertEquals("games-and-toys", SectionSlug.from("  Games and Toys! ").value());
        assertEquals("games-toys", SectionSlug.from("Games & Toys").value());
        assertEquals("3d-scenes", SectionSlug.from("3D Scenes").value());
        var none = assertThrows(IllegalArgumentException.class, () -> SectionSlug.from("游戏"));
        assertTrue(none.getMessage().contains("give it one"), none.getMessage());
        var given = WorkspaceGroup.of("apps", "Apps").section("游戏", SectionSlug.of("games"), ANIMALS).build();
        assertEquals("games/animals", given.path(ANIMALS.kind()).toString());
        assertThrows(IllegalArgumentException.class, () -> SectionSlug.of("Games"), "a given slug is lowercase");
        assertThrows(IllegalArgumentException.class, () -> SectionSlug.of("games--toys"), "and its words are joined by single hyphens");
    }

    @Test
    void aGroupedWorkspaceIsAKindAndATitle_theKindTheLogsGrammar() {
        assertThrows(IllegalArgumentException.class, () -> GroupedWorkspace.of("video room", "Video Room"));
        assertThrows(IllegalArgumentException.class, () -> GroupedWorkspace.of("video-room", " "));
        assertEquals("Video_Room-2", WorkspaceKind.of("Video_Room-2").value(), "letters, digits, hyphen, underscore - the log's kind's grammar");
    }
}
