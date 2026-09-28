package hue.captains.singapura.js.homing.workspace.monitors;

import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Query;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The widget set runs on its own: a site whose one page is the monitors'
 * workspace, the shell's page handed this set's manifest. What this checks is
 * the handover; past it, the shell's.
 */
class MonitorsWorkspaceSiteTest {

    @Test
    void theRootIsTheMonitorsWorkspace_itsServerKeepingItsStates() {
        var found = MonitorsWorkspaceSite.INSTANCE.router().resolve(Path.parse("/"));
        assertTrue(found.isPresent());
        String html = found.get().html(Query.NONE).body();
        assertTrue(html.contains(MonitorsWorkspaceApp.class.getCanonicalName()), "the page imports this set's app and calls its appMain");
        assertTrue(html.contains("\"ws_server\":\"on\""), "the route says the server keeps its states");
        assertFalse(MonitorsWorkspaceSite.INSTANCE.router().resolve(Path.parse("/elsewhere")).isPresent());
    }

    @Test
    void theManifestIsTheDeclarations_everyMonitor() {
        String js = String.join("\n", MonitorsWorkspaceModule.INSTANCE.selfContent(null));
        assertTrue(js.contains("const MONITORS_WORKSPACE = Object.freeze({ name: \"monitors\""), js);
        for (String kind : List.of("\"focus-tree\"", "\"steward-lamp\"", "\"domops-tree\"", "\"party-log\"")) assertTrue(js.contains(kind), kind + " in " + js);
    }
}
