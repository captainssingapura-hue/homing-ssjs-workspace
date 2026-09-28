package hue.captains.singapura.js.homing.workspace.demowidgets;

import hue.captains.singapura.js.homing.workspace.log.store.FileCheckpointStorage;
import hue.captains.singapura.js.homing.workspace.log.store.StoredCheckpointKeeper;
import hue.captains.singapura.js.homing.workspace.shell.server.WorkspaceServer;
import hue.captains.singapura.tao.http.config.HostConfig;
import hue.captains.singapura.tao.http.vertx.VertxActionHost;

import java.nio.file.Path;

/**
 * Serves {@link BooksWorkspaceSite} through its MPA, and the workspace's own
 * routes beside them: the server keeps its pages' states in files.
 * {@code mvn -pl homing-workspace-demo-widgets exec:java}, on 8102 unless
 * {@code -Dbooks.port} says otherwise; checkpoints under
 * {@code target/workspace-checkpoints} of where it is started.
 */
public final class BooksWorkspaceServer {

    private static final int PORT = Integer.getInteger("books.port", 8102);

    static final FileCheckpointStorage STORAGE =
            new FileCheckpointStorage(Path.of(System.getProperty("workspace.checkpoints", "target/workspace-checkpoints")));

    private BooksWorkspaceServer() {}

    public static void main(String[] args) {
        var routes = WorkspaceServer.with(BooksWorkspaceSite.MPA.registry(BooksWorkspaceSite.INSTANCE), new StoredCheckpointKeeper(STORAGE));
        new VertxActionHost(routes, HostConfig.http(PORT)).start()
                .onSuccess(s -> System.out.println("[BooksWorkspaceServer] http://localhost:" + s.actualPort() + "/ - checkpoints kept under " + STORAGE.root()))
                .onFailure(err -> { err.printStackTrace(); System.exit(1); });
    }
}
