package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.workspace.log.store.FileCheckpointStorage;
import hue.captains.singapura.js.homing.workspace.log.store.StoredCheckpointKeeper;
import hue.captains.singapura.js.homing.workspace.shell.server.WorkspaceServer;
import hue.captains.singapura.tao.http.config.HostConfig;
import hue.captains.singapura.tao.http.vertx.VertxActionHost;

import java.nio.file.Path;

/**
 * Serves {@link WorkspaceDemoSite} through its MPA, and the workspace's own
 * routes beside them: the server keeps its pages' states in files.
 * {@code mvn -pl homing-workspace-demo exec:java}, on 8098 unless
 * {@code -Dworkspace.port} says otherwise; checkpoints under
 * {@code target/workspace-checkpoints} of where it is started
 * ({@code -Dworkspace.checkpoints}).
 */
public final class WorkspaceDemoServer {

    private static final int PORT = Integer.getInteger("workspace.port", 8098);

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
