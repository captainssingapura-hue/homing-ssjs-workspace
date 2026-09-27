package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.tao.http.config.HostConfig;
import hue.captains.singapura.tao.http.vertx.VertxActionHost;

/** The bench's server: {@code mvn -pl homing-widget-bench exec:java}, on 8101 unless {@code -Dbench.port} says otherwise. */
public final class WidgetBenchServer {

    private static final int PORT = Integer.getInteger("bench.port", 8101);

    private WidgetBenchServer() {}

    public static void main(String[] args) {
        new VertxActionHost(WidgetBenchSite.MPA.registry(WidgetBenchSite.INSTANCE), HostConfig.http(PORT)).start()
                .onSuccess(s -> System.out.println("[WidgetBenchServer] http://localhost:" + s.actualPort() + "/"))
                .onFailure(err -> { err.printStackTrace(); System.exit(1); });
    }
}
