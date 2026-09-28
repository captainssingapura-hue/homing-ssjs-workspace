package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.designs.HomingDesigns;
import hue.captains.singapura.js.homing.site.Router;
import hue.captains.singapura.js.homing.site.Site;
import hue.captains.singapura.js.homing.site.mpa.AppPage;
import hue.captains.singapura.js.homing.site.mpa.Brand;
import hue.captains.singapura.js.homing.site.mpa.StandardMpa;
import hue.captains.singapura.js.homing.workspace.shell.WorkspacePageModule;

import java.util.Optional;

/**
 * The demo workspace, standing up on its own: a site whose one page is the
 * workspace, under the framework's standard MPA and nothing else - no studio.
 * The server keeps its states, so the route says so.
 */
public record WorkspaceDemoSite() implements Site {

    public static final WorkspaceDemoSite INSTANCE = new WorkspaceDemoSite();

    /** The one declaration this site makes: the brand, the designs, the crate it serves. */
    public static final StandardMpa MPA = StandardMpa.of(Brand.of("Workspace"), HomingDesigns.REGISTRY, WorkspaceDemoCrate.INSTANCE);

    static final AppPage<?, ?> DEMO = MPA.page(DemoWorkspaceApp.INSTANCE, new WorkspacePageModule.Params(true));

    @Override public String name() { return "workspace"; }

    @Override
    public Router router() { return path -> path.isRoot() ? Optional.of(DEMO) : Optional.empty(); }
}
