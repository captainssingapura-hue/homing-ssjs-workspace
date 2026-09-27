package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.workspace.log.store.InMemoryCheckpointKeeper;
import hue.captains.singapura.js.homing.workspace.shell.server.WorkspaceServer;
import hue.captains.singapura.tao.http.config.HostConfig;
import hue.captains.singapura.tao.http.vertx.VertxActionHost;

/**
 * Serves {@link WorkspaceDemoSite} through its MPA - the framework's routes for
 * modules, sheets and themes, then the site's own - and the workspace's own
 * beside them: the server keeps its pages' states, the checkpoints they post,
 * in memory for as long as it runs ({@link WorkspaceServer}).
 *
 * <pre>
 *   mvn -pl homing-workspace-demos/workspace-demo -am compile exec:java \
 *       -Dexec.mainClass=hue.captains.singapura.js.homing.workspace.demo.WorkspaceDemoServer
 * </pre>
 * Listens on 8098 ({@code -Dworkspace.port}).
 */
public final class WorkspaceDemoServer {

    private static final int PORT = Integer.getInteger("workspace.port", 8098);

    /** What the server keeps of its pages' states. */
    static final InMemoryCheckpointKeeper KEEPER = new InMemoryCheckpointKeeper();

    private WorkspaceDemoServer() {}

    public static void main(String[] args) {
        var routes = WorkspaceServer.with(WorkspaceDemoSite.MPA.registry(WorkspaceDemoSite.INSTANCE), KEEPER);
        new VertxActionHost(routes, HostConfig.http(PORT)).start()
                .onSuccess(s -> System.out.println("[WorkspaceDemoServer] http://localhost:" + s.actualPort() + "/ - checkpoints kept at " + WorkspaceServer.CHECKPOINTS))
                .onFailure(err -> { err.printStackTrace(); System.exit(1); });
    }
}
