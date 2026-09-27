package hue.captains.singapura.js.homing.workspace.shell.server;

import hue.captains.singapura.js.homing.workspace.log.store.CheckpointKeeper;
import hue.captains.singapura.tao.http.action.ActionRegistry;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.PostAction;
import io.vertx.ext.web.RoutingContext;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The workspace's own routes on a site's server, for a site that keeps its
 * workspaces' states: {@value #CHECKPOINTS} takes the checkpoints its pages
 * post, and answers a log's latest back. A site hosting workspace pages
 * without it keeps them in the browser only - its pages post nowhere unless
 * their route says the server takes them.
 *
 * <pre>
 *   var keeper = new InMemoryCheckpointKeeper();   // or the site's own CheckpointKeeper
 *   new VertxActionHost(WorkspaceServer.with(MPA.registry(site), keeper), HostConfig.http(port)).start();
 * </pre>
 */
public final class WorkspaceServer {

    private WorkspaceServer() {}

    /** Where a page posts its checkpoints, and reads a log's latest back from. */
    public static final String CHECKPOINTS = "/workspace/checkpoints";

    /**
     * The site's routes, with the workspace's beside them. The host mounts routes
     * in the order given and a site ends with its catch-all, so the workspace's
     * come before it.
     */
    public static ActionRegistry<RoutingContext> with(ActionRegistry<RoutingContext> site, CheckpointKeeper keeper) {
        Objects.requireNonNull(site, "WorkspaceServer.site");
        Objects.requireNonNull(keeper, "WorkspaceServer.keeper");
        var gets = new LinkedHashMap<String, GetAction<RoutingContext, ?, ?, ?>>();
        var ours = new CheckpointGetAction(keeper);
        boolean placed = false;
        for (var e : site.getActions().entrySet()) {
            if (!placed && e.getKey().equals("/*")) { gets.put(CHECKPOINTS, ours); placed = true; }
            gets.put(e.getKey(), e.getValue());
        }
        if (!placed) gets.put(CHECKPOINTS, ours);
        var posts = new LinkedHashMap<String, PostAction<RoutingContext, ?, ?, ?>>(site.postActions());
        posts.put(CHECKPOINTS, new CheckpointPostAction(keeper));
        var getsView = Collections.unmodifiableMap(gets);
        var postsView = Collections.unmodifiableMap(posts);
        return new ActionRegistry<>() {
            @Override public Map<String, GetAction<RoutingContext, ?, ?, ?>> getActions() { return getsView; }
            @Override public Map<String, PostAction<RoutingContext, ?, ?, ?>> postActions() { return postsView; }
        };
    }
}
