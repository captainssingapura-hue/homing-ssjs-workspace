package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.site.Navigable;
import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Router;
import hue.captains.singapura.js.homing.site.Site;
import hue.captains.singapura.js.homing.site.Trail;
import hue.captains.singapura.js.homing.site.mpa.AppPage;
import hue.captains.singapura.js.homing.site.mpa.Brand;
import hue.captains.singapura.js.homing.site.mpa.StandardMpa;
import hue.captains.singapura.js.homing.studio.themes.StudioThemeRegistry;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceApp;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceShellCrate;

import java.util.Optional;

/**
 * The workspace standing up on its own: a site whose every page is a
 * workspace, under the framework's standard MPA and nothing else.
 *
 * <p>This is the whole claim of the stage, in one file. There is no studio
 * here — no catalogue, no doc kinds, no studio shell — and the workspace still
 * mounts, because {@link WorkspaceApp} is an {@code AppModule} like any other
 * and {@code StandardMpa.page} is all a page needs. The site declares a brand,
 * the themes it wears and the crate whose modules it serves, and routes two
 * paths at two kinds.</p>
 *
 * <p>The MPA also serves the flat address {@code /app?app=workspace&ws_kind=…}
 * for free, which is where a change of kind inside the workspace navigates —
 * so cross-kind switching works without the site routing for it.</p>
 */
public record WorkspaceDemoSite() implements Site {

    public static final WorkspaceDemoSite INSTANCE = new WorkspaceDemoSite();

    static {
        // The registry is read when the specs module is served, so it must be
        // filled before anything is answered — and the site is the earliest
        // thing every host of it touches.
        WorkspaceDemoSpecs.register();
    }

    /** The one declaration this site makes: the brand, the designs, the crate it serves. */
    public static final StandardMpa MPA = StandardMpa.of(
            Brand.of("Workspace"), StudioThemeRegistry.INSTANCE, WorkspaceShellCrate.INSTANCE);

    static final AppPage<?, ?> MONITORS =
            MPA.page(WorkspaceApp.INSTANCE, new WorkspaceApp.Params(WorkspaceDemoSpecs.MONITORS.kind()));

    static final AppPage<?, ?> GRAPH =
            MPA.page(WorkspaceApp.INSTANCE, new WorkspaceApp.Params(WorkspaceDemoSpecs.GRAPH.kind()));

    @Override public String name() { return "workspace"; }

    @Override
    public Router router() {
        return path -> switch (path.head().orElse("")) {
            case ""      -> path.isRoot() ? Optional.of(MONITORS) : Optional.empty();
            case "graph" -> path.depth() == 1 ? Optional.of(placed(GRAPH, "Design graph", path)) : Optional.empty();
            default      -> Optional.empty();
        };
    }

    /** A page one below the root, told the trail the router knows; the chrome draws it. */
    private static Navigable placed(AppPage<?, ?> page, String name, Path path) {
        var trail = Trail.NONE.then("Workspace", "/").then(name, path.toString());
        return q -> page.html(trail, q);
    }
}
