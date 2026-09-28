package hue.captains.singapura.js.homing.workspace.monitors;

import hue.captains.singapura.js.homing.designs.HomingDesigns;
import hue.captains.singapura.js.homing.site.Router;
import hue.captains.singapura.js.homing.site.Site;
import hue.captains.singapura.js.homing.site.mpa.AppPage;
import hue.captains.singapura.js.homing.site.mpa.Brand;
import hue.captains.singapura.js.homing.site.mpa.StandardMpa;
import hue.captains.singapura.js.homing.workspace.shell.WorkspacePageModule;

import java.util.Optional;

/**
 * The monitors, runnable: a site whose one page is the monitors' workspace,
 * under the framework's standard MPA and nothing else. The server keeps its
 * states, so the route says so.
 */
public record MonitorsWorkspaceSite() implements Site {

    public static final MonitorsWorkspaceSite INSTANCE = new MonitorsWorkspaceSite();

    /** The one declaration this site makes: the brand, the designs, the crate it serves. */
    public static final StandardMpa MPA = StandardMpa.of(Brand.of("Monitors"), HomingDesigns.REGISTRY, WorkspaceMonitorsCrate.INSTANCE);

    static final AppPage<?, ?> MONITORS = MPA.page(MonitorsWorkspaceApp.INSTANCE, new WorkspacePageModule.Params(true));

    @Override public String name() { return "monitors"; }

    @Override
    public Router router() { return path -> path.isRoot() ? Optional.of(MONITORS) : Optional.empty(); }
}
