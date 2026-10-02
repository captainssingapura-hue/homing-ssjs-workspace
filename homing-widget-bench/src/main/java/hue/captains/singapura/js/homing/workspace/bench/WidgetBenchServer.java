package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.tao.http.action.ActionRegistry;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.PostAction;
import hue.captains.singapura.tao.http.config.HostConfig;
import hue.captains.singapura.tao.http.vertx.VertxActionHost;
import io.vertx.ext.web.RoutingContext;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The bench's server: {@code mvn -pl homing-widget-bench exec:java}, on 8101 unless {@code -Dbench.port} says otherwise.
 * The site's routes, and the content bench's items beside them ({@link BenchContentGetAction}).
 */
public final class WidgetBenchServer {

    private static final int PORT = Integer.getInteger("bench.port", 8101);

    private WidgetBenchServer() {}

    public static void main(String[] args) {
        new VertxActionHost(registry(), HostConfig.http(PORT)).start()
                .onSuccess(s -> System.out.println("[WidgetBenchServer] http://localhost:" + s.actualPort() + "/"))
                .onFailure(err -> { err.printStackTrace(); System.exit(1); });
    }

    /**
     * The site's routes with the bench's own among them. The host mounts routes in the order given and a site ends
     * with its catch-all, so the bench's come before it.
     */
    static ActionRegistry<RoutingContext> registry() {
        var site = WidgetBenchSite.MPA.registry(WidgetBenchSite.INSTANCE);
        var gets = new LinkedHashMap<String, GetAction<RoutingContext, ?, ?, ?>>();
        var content = new BenchContentGetAction();
        boolean placed = false;
        for (var e : site.getActions().entrySet()) {
            if (!placed && e.getKey().equals("/*")) { gets.put(BenchContentGetAction.ROUTE, content); placed = true; }
            gets.put(e.getKey(), e.getValue());
        }
        if (!placed) gets.put(BenchContentGetAction.ROUTE, content);
        var getsView = Collections.unmodifiableMap(gets);
        var posts = site.postActions();
        return new ActionRegistry<>() {
            @Override public Map<String, GetAction<RoutingContext, ?, ?, ?>> getActions() { return getsView; }
            @Override public Map<String, PostAction<RoutingContext, ?, ?, ?>> postActions() { return posts; }
        };
    }
}
