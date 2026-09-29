package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.designs.HomingDesigns;
import hue.captains.singapura.js.homing.site.Router;
import hue.captains.singapura.js.homing.site.Site;
import hue.captains.singapura.js.homing.site.mpa.Brand;
import hue.captains.singapura.js.homing.site.mpa.StandardMpa;

/**
 * The demo's workspaces, standing up on their own: a site of grouped workspaces,
 * each served where its group files it - {@code /<section>/<kind>}, the root the
 * group's default - under the framework's standard MPA and nothing else, no
 * studio. The server keeps their states, so every route says so.
 */
public record WorkspaceDemoSite() implements Site {

    public static final WorkspaceDemoSite INSTANCE = new WorkspaceDemoSite();

    /** The one declaration this site makes: the brand, the designs, the crate it serves. */
    public static final StandardMpa MPA = StandardMpa.of(Brand.of("Workspace"), HomingDesigns.REGISTRY, WorkspaceDemoCrate.INSTANCE);

    private static final Router ROUTER = DemoGroups.SITE.router(MPA, DemoWorkspaceApp.INSTANCE, true);

    @Override public String name() { return "workspace"; }

    @Override public Router router() { return ROUTER; }
}
