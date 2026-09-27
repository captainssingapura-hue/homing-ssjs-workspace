package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.core.ParamCodec.Decoded;
import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Query;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceApp;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    /** A kind is a name, not a spec: the address may name any, but only a kind-shaped one. */
    @Test
    void theAddressNamesAKind_andOnlyAKindShapedOne() {
        assertTrue(WorkspaceApp.CODEC.from(Map.of("ws_kind", List.of("scratch-2"))).isOk());
        assertInstanceOf(Decoded.Missing.class, WorkspaceApp.CODEC.from(Map.of()));
        assertInstanceOf(Decoded.Malformed.class, WorkspaceApp.CODEC.from(Map.of("ws_kind", List.of("no spaces"))));
    }

    /** The demo's server keeps its pages' states, so its routes say so; an address may say it too, but only "on". */
    @Test
    void theDemosPagesPostTheirCheckpointsToTheServer() {
        assertTrue(pageAt("/").contains("\"ws_server\":\"on\""), "the route says the server keeps its states");
        var on = WorkspaceApp.CODEC.from(Map.of("ws_kind", List.of("demo"), "ws_server", List.of("on")));
        assertTrue(on.isOk() && on.orNull().ws_server());
        assertTrue(!WorkspaceApp.CODEC.from(Map.of("ws_kind", List.of("demo"))).orNull().ws_server(), "off unless said");
        assertInstanceOf(Decoded.Malformed.class, WorkspaceApp.CODEC.from(Map.of("ws_kind", List.of("demo"), "ws_server", List.of("https://elsewhere"))));
    }

    /** A route's page keeps its kind's own workspace; an address may name another of the kind, by the id its log is kept under, written as the log writes it. */
    @Test
    void theAddressNamesOneWorkspaceOfTheKind_orNoneForTheKindsOwn() {
        assertFalse(pageAt("/notes").contains("ws_id"), "the route keeps the kind's own");
        String id = "0f1b6c2e-5000-4000-8f1b-6c2e00000001";
        var named = WorkspaceApp.CODEC.from(Map.of("ws_kind", List.of("notes"), "ws_id", List.of(id)));
        assertTrue(named.isOk() && id.equals(named.orNull().ws_id()));
        assertEquals(List.of(id), WorkspaceApp.CODEC.to(named.orNull()).get("ws_id"), "and written back as it was read");
        assertNull(WorkspaceApp.CODEC.from(Map.of("ws_kind", List.of("notes"))).orNull().ws_id(), "absent, the kind's own");
        for (String not : List.of("nope", id.toUpperCase(), "0f1b6c2e50004000-8f1b-6c2e00000001", "")) {
            assertInstanceOf(Decoded.Malformed.class, WorkspaceApp.CODEC.from(Map.of("ws_kind", List.of("notes"), "ws_id", List.of(not))), not);
        }
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
        assertTrue(WorkspaceDemoSite.INSTANCE.router().resolve(Path.parse("/diagnostics")).isEmpty(),
                "the old kinds' routes left with the widgets they offered");
    }
}
