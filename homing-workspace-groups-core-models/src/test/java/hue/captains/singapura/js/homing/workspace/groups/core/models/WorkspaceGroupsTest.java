package hue.captains.singapura.js.homing.workspace.groups.core.models;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceGroupTest.ANIMALS;
import static hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceGroupTest.TABLES;
import static hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceGroupTest.VIDEO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A site's groups: no two share an id, and on one site a workspace is filed once - across sites, nothing is shared. */
class WorkspaceGroupsTest {

    static final WorkspaceGroup APPS = WorkspaceGroup.of("apps", "Apps").section("Media", VIDEO).section("Games", ANIMALS).build();
    static final WorkspaceGroup DATA = WorkspaceGroup.of("data", "Data").section("Tables", TABLES).build();

    @Test
    void aSitesGroupsAreFoundByIdAndByTheWorkspacesTheyFile() {
        var site = WorkspaceGroups.of(APPS, DATA);
        assertEquals(Optional.of(DATA), site.group(GroupId.of("data")));
        assertEquals(Optional.of(APPS), site.groupOf(ANIMALS.kind()));
        assertEquals(Optional.empty(), site.groupOf(WorkspaceKind.of("elsewhere")));
    }

    @Test
    void onOneSiteAWorkspaceIsFiledOnce_andAnIdIsOneGroup() {
        var again = WorkspaceGroup.of("more", "More").section("Also", VIDEO).build();
        var e = assertThrows(IllegalArgumentException.class, () -> WorkspaceGroups.of(APPS, again));
        assertTrue(e.getMessage().contains("filed in the groups 'apps' and 'more'"), e.getMessage());
        var sameId = WorkspaceGroup.of("apps", "Apps again").section("Tables", TABLES).build();
        assertThrows(IllegalArgumentException.class, () -> WorkspaceGroups.of(APPS, sameId));
    }

    @Test
    void acrossSitesNothingIsShared() {
        var elsewhere = WorkspaceGroups.of(WorkspaceGroup.of("examples", "Examples").section("Examples", VIDEO).build());
        assertEquals("examples/video-room", elsewhere.groupOf(VIDEO.kind()).orElseThrow().path(VIDEO.kind()).toString());
        assertEquals("media/video-room", WorkspaceGroups.of(APPS).groupOf(VIDEO.kind()).orElseThrow().path(VIDEO.kind()).toString());
    }
}
