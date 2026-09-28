package hue.captains.singapura.js.homing.workspace.demowidgets;

import hue.captains.singapura.js.homing.designs.HomingDesigns;
import hue.captains.singapura.js.homing.site.Router;
import hue.captains.singapura.js.homing.site.Site;
import hue.captains.singapura.js.homing.site.mpa.AppPage;
import hue.captains.singapura.js.homing.site.mpa.Brand;
import hue.captains.singapura.js.homing.site.mpa.StandardMpa;
import hue.captains.singapura.js.homing.workspace.shell.WorkspacePageModule;

import java.util.Optional;

/**
 * The demo's widgets, runnable: a site whose one page is the books workspace,
 * under the framework's standard MPA and nothing else. The server keeps its
 * states, so the route says so.
 */
public record BooksWorkspaceSite() implements Site {

    public static final BooksWorkspaceSite INSTANCE = new BooksWorkspaceSite();

    /** The one declaration this site makes: the brand, the designs, the crate it serves. */
    public static final StandardMpa MPA = StandardMpa.of(Brand.of("Books"), HomingDesigns.REGISTRY, WorkspaceDemoWidgetsCrate.INSTANCE);

    static final AppPage<?, ?> BOOKS = MPA.page(BooksWorkspaceApp.INSTANCE, new WorkspacePageModule.Params(true));

    @Override public String name() { return "books"; }

    @Override
    public Router router() { return path -> path.isRoot() ? Optional.of(BOOKS) : Optional.empty(); }
}
