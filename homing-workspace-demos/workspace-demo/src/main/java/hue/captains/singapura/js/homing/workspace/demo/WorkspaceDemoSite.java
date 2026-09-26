package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.site.Navigable;
import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Router;
import hue.captains.singapura.js.homing.site.Site;
import hue.captains.singapura.js.homing.site.Trail;
import hue.captains.singapura.js.homing.site.mpa.AppPage;
import hue.captains.singapura.js.homing.site.mpa.Brand;
import hue.captains.singapura.js.homing.site.mpa.StandardMpa;
import hue.captains.singapura.js.homing.designs.HomingDesigns;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceApp;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceShellCrate;

import java.util.Optional;

/**
 * The workspace standing up on its own: a site whose every page is a
 * workspace, under the framework's standard MPA and nothing else.
 *
 * <p>There is no studio here — no catalogue, no doc kinds, no studio shell —
 * and the workspace still mounts, because {@link WorkspaceApp} is an
 * {@code AppModule} like any other and {@code StandardMpa.page} is all a page
 * needs. The site declares a brand, the designs it wears and the crate whose
 * modules it serves, and routes two paths at two kinds: each kind a workspace
 * of its own, with a log of its own.</p>
 *
 * <p>The MPA also serves the flat address {@code /app?app=workspace&ws_kind=…}
 * for free, so any other kind-shaped kind is a workspace too, without the site
 * routing for it.</p>
 */
public record WorkspaceDemoSite() implements Site {

    public static final WorkspaceDemoSite INSTANCE = new WorkspaceDemoSite();

    /** The one declaration this site makes: the brand, the designs, the crate it serves. */
    public static final StandardMpa MPA = StandardMpa.of(
            Brand.of("Workspace"), HomingDesigns.REGISTRY, WorkspaceShellCrate.INSTANCE);

    static final AppPage<?, ?> DEMO  = MPA.page(WorkspaceApp.INSTANCE, new WorkspaceApp.Params("demo"));
    static final AppPage<?, ?> NOTES = MPA.page(WorkspaceApp.INSTANCE, new WorkspaceApp.Params("notes"));

    @Override public String name() { return "workspace"; }

    @Override
    public Router router() {
        return path -> switch (path.head().orElse("")) {
            case ""      -> path.isRoot() ? Optional.of(DEMO) : Optional.empty();
            case "notes" -> path.depth() == 1 ? Optional.of(placed(NOTES, "Notes", path)) : Optional.empty();
            default      -> Optional.empty();
        };
    }

    /** A page one below the root, told the trail the router knows; the chrome draws it. */
    private static Navigable placed(AppPage<?, ?> page, String name, Path path) {
        var trail = Trail.NONE.then("Workspace", "/").then(name, path.toString());
        return q -> page.html(trail, q);
    }
}
