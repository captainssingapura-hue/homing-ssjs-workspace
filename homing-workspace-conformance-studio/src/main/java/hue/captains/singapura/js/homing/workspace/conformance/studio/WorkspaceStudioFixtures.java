package hue.captains.singapura.js.homing.workspace.conformance.studio;

import hue.captains.singapura.js.homing.conformance.studio.ConformanceStudio;
import hue.captains.singapura.js.homing.conformance.studio.ConformanceStudioFixtures;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.server.ThemeRegistry;
import hue.captains.singapura.js.homing.studio.base.Fixtures;
import hue.captains.singapura.js.homing.studio.base.Umbrella;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.PostAction;
import io.vertx.ext.web.RoutingContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The framework's conformance-studio fixtures, as they are - but the crate
 * views ({@code /crate-tree}, {@code /crate-graph}, {@code /crate-conformance})
 * answered with what the export wrote for them, run over this repo's crates
 * at build time: this studio cannot load those crates, and its own views over
 * none would be empty. A view is the file under {@code live/} its path names.
 *
 * @param inner the framework's fixtures
 */
public record WorkspaceStudioFixtures(ConformanceStudioFixtures inner) implements Fixtures<ConformanceStudio> {

    /** The crate views the export writes, by the file its path names. */
    static final Map<String, WrittenViewGetAction> WRITTEN = Map.of(
            "/crate-tree", new WrittenViewGetAction("crate-tree.json", "application/json; charset=utf-8"),
            "/crate-graph", new WrittenViewGetAction("crate-graph.txt", "text/plain; charset=utf-8"),
            "/crate-conformance", new WrittenViewGetAction("crate-conformance.json", "application/json; charset=utf-8"));

    public WorkspaceStudioFixtures { Objects.requireNonNull(inner, "inner"); }

    @Override public Umbrella<ConformanceStudio> umbrella() { return inner.umbrella(); }
    @Override public List<AppModule<?, ?>> harnessApps() { return inner.harnessApps(); }
    @Override public Map<String, PostAction<RoutingContext, ?, ?, ?>> harnessPostActions() { return inner.harnessPostActions(); }
    @Override public ThemeRegistry themeRegistry() { return inner.themeRegistry(); }
    @Override public Theme defaultTheme() { return inner.defaultTheme(); }
    @Override public List<Crate> crates() { return inner.crates(); }
    @Override public NodeChrome chromeFor(Umbrella<ConformanceStudio> node) { return inner.chromeFor(node); }

    @Override
    public Map<String, GetAction<RoutingContext, ?, ?, ?>> harnessGetActions() {
        var actions = new LinkedHashMap<>(inner.harnessGetActions());
        actions.putAll(WRITTEN);
        return Map.copyOf(actions);
    }
}
