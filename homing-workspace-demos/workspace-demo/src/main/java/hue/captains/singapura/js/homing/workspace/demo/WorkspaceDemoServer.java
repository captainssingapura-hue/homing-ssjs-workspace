package hue.captains.singapura.js.homing.workspace.demo;

/**
 * Serves {@link WorkspaceDemoSite} through its MPA: the framework's routes for
 * modules, sheets and themes, then the site's own two.
 *
 * <pre>
 *   mvn -pl homing-workspace-demos/workspace-demo -am compile exec:java \
 *       -Dexec.mainClass=hue.captains.singapura.js.homing.workspace.demo.WorkspaceDemoServer
 * </pre>
 * Listens on 8098 ({@code -Dworkspace.port}).
 */
public final class WorkspaceDemoServer {

    private static final int PORT = Integer.getInteger("workspace.port", 8098);

    private WorkspaceDemoServer() {}

    public static void main(String[] args) {
        WorkspaceDemoSite.MPA.start(WorkspaceDemoSite.INSTANCE, PORT)
                .onFailure(err -> System.exit(1));
    }
}
