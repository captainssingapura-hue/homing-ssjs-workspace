package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Query;
import hue.captains.singapura.js.homing.workspace.shell.CssGraphWorkbenchWidget;
import hue.captains.singapura.js.homing.workspace.shell.DomOpsPartyMonitorWidget;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceApp;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpecRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The stage's claim, asserted: the workspace is a page of a standard MPA, with
 * no studio anywhere.
 *
 * <p>What this can check without a browser is exactly the seam the stage
 * moved — the document the site serves. A workspace that mounts is the
 * server's job up to the point where the page names the app's module and
 * stamps the kind; everything past that is the shell's, and the shell's own
 * tests have it. So these assert the handover and stop.</p>
 */
class WorkspaceDemoSiteTest {

    private static String pageAt(String path) {
        var found = WorkspaceDemoSite.INSTANCE.router().resolve(Path.parse(path));
        assertTrue(found.isPresent(), "no page at " + path);
        return found.get().html(Query.NONE).body();
    }

    @Test
    void theKindsAreRegistered() {
        // The site's static block fills the registry; touching INSTANCE is what runs it.
        assertEquals(WorkspaceDemoSite.INSTANCE, WorkspaceDemoSite.INSTANCE);
        assertTrue(WorkspaceSpecRegistry.INSTANCE.get("demo").isPresent());
        assertTrue(WorkspaceSpecRegistry.INSTANCE.get("notes").isPresent());
        assertTrue(WorkspaceSpecRegistry.INSTANCE.get("diagnostics").isPresent());
    }

    /** The diagnostics kind offers the shell's two instruments, and nothing of the demo's. */
    @Test
    void theDiagnosticsKindOffersTheShellsTwoInstruments() {
        String html = pageAt("/diagnostics");   // the site first: its static block fills the registry
        var offered = WorkspaceSpecRegistry.INSTANCE.get("diagnostics").orElseThrow().widgetEntries().stream()
                .map(e -> e.widgetClass()).toList();
        assertEquals(List.of(DomOpsPartyMonitorWidget.class, CssGraphWorkbenchWidget.class), offered);
        assertTrue(html.contains("\"ws_kind\":\"diagnostics\""), "a route of its own");
    }

    /** The demo kind offers every widget the demo has, in the picker's order. */
    @Test
    void theDemoKindOffersEveryDemoWidget() {
        var offered = WorkspaceSpecRegistry.INSTANCE.get("demo").orElseThrow().widgetEntries().stream()
                .map(e -> e.widgetClass()).toList();
        assertEquals(List.of(DemoNoteWidget.class, DemoCounterWidget.class,
                             DemoBooksWidget.class, DemoShelvesWidget.class, DemoPictureWidget.class), offered);
    }

    @Test
    void theRootIsTheDemoWorkspace() {
        String html = pageAt("/");
        assertTrue(html.contains("\"ws_kind\":\"demo\""), "the kind is stamped into the page");
        assertTrue(html.contains(WorkspaceApp.class.getCanonicalName()),
                "the page imports the app's module and calls its appMain");
        assertTrue(html.contains("appMain(page.main"), "the app is handed the MPA's slot");
    }

    @Test
    void aSecondKindIsASecondRoute() {
        String html = pageAt("/notes");
        assertTrue(html.contains("\"ws_kind\":\"notes\""));
        assertTrue(html.contains("Notes"), "the router told the page its trail");
    }

    /**
     * The negative half of the claim, and the reason the stage exists: nothing
     * the site serves comes from the studio. A page that named a studio class
     * would still work — and would mean the workspace had not left.
     */
    @Test
    void noStudioInThePageTheSiteServes() {
        String html = pageAt("/");
        assertFalse(html.contains("studio.base"), "a studio-base module reached the page");
        assertFalse(html.contains("GenericWorkspace"), "the studio's mounting reached the page");
    }

    @Test
    void anUnknownPathIsNotAPage() {
        assertTrue(WorkspaceDemoSite.INSTANCE.router().resolve(Path.parse("/nope")).isEmpty());
    }
}
