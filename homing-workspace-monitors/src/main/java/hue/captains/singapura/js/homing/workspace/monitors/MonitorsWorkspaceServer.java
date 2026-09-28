package hue.captains.singapura.js.homing.workspace.monitors;

import hue.captains.singapura.js.homing.workspace.log.store.FileCheckpointStorage;
import hue.captains.singapura.js.homing.workspace.log.store.StoredCheckpointKeeper;
import hue.captains.singapura.js.homing.workspace.shell.server.WorkspaceServer;
import hue.captains.singapura.tao.http.config.HostConfig;
import hue.captains.singapura.tao.http.vertx.VertxActionHost;

import java.nio.file.Path;

/**
 * Serves {@link MonitorsWorkspaceSite} through its MPA, and the workspace's own
 * routes beside them: the server keeps its pages' states in files.
 * {@code mvn -pl homing-workspace-monitors exec:java}, on 8103 unless
 * {@code -Dmonitors.port} says otherwise; checkpoints under
 * {@code target/workspace-checkpoints} of where it is started.
 */
public final class MonitorsWorkspaceServer {

    private static final int PORT = Integer.getInteger("monitors.port", 8103);

    static final FileCheckpointStorage STORAGE =
            new FileCheckpointStorage(Path.of(System.getProperty("workspace.checkpoints", "target/workspace-checkpoints")));

    private MonitorsWorkspaceServer() {}

    public static void main(String[] args) {
        var routes = WorkspaceServer.with(MonitorsWorkspaceSite.MPA.registry(MonitorsWorkspaceSite.INSTANCE), new StoredCheckpointKeeper(STORAGE));
        new VertxActionHost(routes, HostConfig.http(PORT)).start()
                .onSuccess(s -> System.out.println("[MonitorsWorkspaceServer] http://localhost:" + s.actualPort() + "/ - checkpoints kept under " + STORAGE.root()))
                .onFailure(err -> { err.printStackTrace(); System.exit(1); });
    }
}
