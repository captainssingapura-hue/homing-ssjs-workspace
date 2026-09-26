package hue.captains.singapura.js.homing.workspace.conformance.studio;

import hue.captains.singapura.js.homing.conformance.studio.ConformanceStudio;
import hue.captains.singapura.js.homing.conformance.studio.ConformanceStudioFixtures;
import hue.captains.singapura.js.homing.studio.base.Bootstrap;
import hue.captains.singapura.js.homing.studio.base.DefaultRuntimeParams;
import hue.captains.singapura.js.homing.studio.base.Umbrella;

import java.util.List;

/**
 * The workspace's conformance studio, report only: the framework's studio,
 * verbatim, on core's stack, with no top-level crates of its own — this repo's
 * crates share their names with the copy of the workspace core's stack
 * carries, so they are never on this classpath. What it shows is the report
 * {@code WorkspaceConformanceExport} wrote on the conformance module's build,
 * read from that module's jar. Local, for development and review.
 *
 * <p>Landing: {@code /}. Workspace: {@code /cat/conformance-workspace}. Port
 * {@code 8099} by default ({@code -Dconformance.port}), beside the components'
 * 8097 and the workspace demo's 8098.</p>
 */
public final class WorkspaceConformanceStudioServer {

    private WorkspaceConformanceStudioServer() {}

    public static void main(String[] args) {
        if (WorkspaceConformanceStudioServer.class.getResource("/conformance-report/report.json") == null) {
            System.err.println("[workspace-crate-studio] no report on the classpath: build homing-workspace-conformance first");
        }
        var umbrella = new Umbrella.Solo<>(ConformanceStudio.INSTANCE);
        int port = Integer.getInteger("conformance.port", 8099);
        System.out.println("[workspace-crate-studio] the workspace's conformance report, on " + port);
        new Bootstrap<>(new ConformanceStudioFixtures(umbrella, List.of()), new DefaultRuntimeParams(port)).start();
    }
}
