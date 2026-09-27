package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.workspace.log.store.FileCheckpointStorage;
import hue.captains.singapura.js.homing.workspace.log.store.StoredCheckpointKeeper;
import hue.captains.singapura.js.homing.workspace.shell.server.WorkspaceServer;
import hue.captains.singapura.tao.http.config.HostConfig;
import hue.captains.singapura.tao.http.vertx.VertxActionHost;

import java.nio.file.Path;

/**
 * Serves {@link WorkspaceDemoSite} through its MPA - the framework's routes for
 * modules, sheets and themes, then the site's own - and the workspace's own
 * beside them: the server keeps its pages' states, the checkpoints they post,
 * in files ({@link WorkspaceServer}, {@link FileCheckpointStorage}).
 *
 * <pre>
 *   mvn -pl homing-workspace-demos/workspace-demo -am compile exec:java \
 *       -Dexec.mainClass=hue.captains.singapura.js.homing.workspace.demo.WorkspaceDemoServer
 * </pre>
 * Listens on 8098 ({@code -Dworkspace.port}); keeps checkpoints under
 * {@code target/workspace-checkpoints} of where it is started
 * ({@code -Dworkspace.checkpoints}).
 */
public final class WorkspaceDemoServer {

    private static final int PORT = Integer.getInteger("workspace.port", 8098);

    /** Where the server keeps its pages' states. */
    static final FileCheckpointStorage STORAGE =
            new FileCheckpointStorage(Path.of(System.getProperty("workspace.checkpoints", "target/workspace-checkpoints")));

    private WorkspaceDemoServer() {}

    public static void main(String[] args) {
        var routes = WorkspaceServer.with(WorkspaceDemoSite.MPA.registry(WorkspaceDemoSite.INSTANCE), new StoredCheckpointKeeper(STORAGE));
        new VertxActionHost(routes, HostConfig.http(PORT)).start()
                .onSuccess(s -> System.out.println("[WorkspaceDemoServer] http://localhost:" + s.actualPort() + "/ - checkpoints kept under " + STORAGE.root()))
                .onFailure(err -> { err.printStackTrace(); System.exit(1); });
    }
}
