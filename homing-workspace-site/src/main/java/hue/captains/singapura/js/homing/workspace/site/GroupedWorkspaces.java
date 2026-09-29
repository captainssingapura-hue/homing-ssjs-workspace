package hue.captains.singapura.js.homing.workspace.site;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.site.Navigable;
import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Router;
import hue.captains.singapura.js.homing.site.Trail;
import hue.captains.singapura.js.homing.site.mpa.AppPage;
import hue.captains.singapura.js.homing.site.mpa.StandardMpa;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceGroupsJs;
import hue.captains.singapura.js.homing.workspace.groups.core.models.GroupPath;
import hue.captains.singapura.js.homing.workspace.groups.core.models.GroupedWorkspace;
import hue.captains.singapura.js.homing.workspace.groups.core.models.Section;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceGroup;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceGroups;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceKind;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceManifest;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A site's workspaces, grouped: every workspace it serves - each declared in Java,
 * a {@link WorkspaceDeclaration} - filed in its groups, and every kind its groups
 * file one it serves. Where a workspace is filed is the group's decision, never
 * the workspace's; and where it is filed is where it is served: {@code /<section>/<kind>},
 * its {@link GroupPath}. The site's root serves the first group's default.
 *
 * <p>What the page has of them is generated here too: the manifests, by kind, and
 * the groups, for the page's directory.</p>
 */
public record GroupedWorkspaces(WorkspaceGroups groups, List<WorkspaceDeclaration> workspaces) {

    /** Where a path lands: the group, the section and the workspace filed there, and its path. */
    public record Place(WorkspaceGroup group, Section section, GroupedWorkspace workspace, GroupPath path) {}

    public GroupedWorkspaces {
        Objects.requireNonNull(groups, "GroupedWorkspaces.groups");
        workspaces = List.copyOf(Objects.requireNonNull(workspaces, "GroupedWorkspaces.workspaces"));
        if (groups.groups().isEmpty()) throw new IllegalArgumentException("GroupedWorkspaces: at least one group");
        Set<String> served = new HashSet<>();
        for (var w : workspaces) {
            if (!served.add(w.name())) throw new IllegalArgumentException("GroupedWorkspaces: two workspaces named " + w.name());
        }
        Set<String> filed = groups.groups().stream().flatMap(g -> g.workspaces().stream()).map(w -> w.kind().value()).collect(Collectors.toSet());
        var unfiled = served.stream().filter(n -> !filed.contains(n)).sorted().toList();
        if (!unfiled.isEmpty()) throw new IllegalArgumentException("GroupedWorkspaces: served but filed in no group: " + unfiled);
        var unserved = filed.stream().filter(n -> !served.contains(n)).sorted().toList();
        if (!unserved.isEmpty()) throw new IllegalArgumentException("GroupedWorkspaces: filed but not served: " + unserved);
    }

    public static GroupedWorkspaces of(WorkspaceGroups groups, WorkspaceDeclaration... workspaces) {
        return new GroupedWorkspaces(groups, List.of(workspaces));
    }

    /** The workspace of that kind, as the site serves it. */
    public Optional<WorkspaceDeclaration> declaration(String kind) {
        return workspaces.stream().filter(w -> w.name().equals(kind)).findFirst();
    }

    /** Where the site's root lands: the first group's default. */
    public Place home() {
        var g = groups.groups().get(0);
        return place(g, g.defaultKind());
    }

    /** Where a path lands: the root, home; {@code /<section>/<kind>}, the workspace filed there; anything else, nowhere. */
    public Optional<Place> place(Path path) {
        if (path.isRoot()) return Optional.of(home());
        if (path.depth() != 2) return Optional.empty();
        String section = path.segments().get(0), kind = path.segments().get(1);
        for (var g : groups.groups()) {
            var filed = g.at(section, kind);
            if (filed.isPresent()) return Optional.of(place(g, filed.get().kind()));
        }
        return Optional.empty();
    }

    private static Place place(WorkspaceGroup g, WorkspaceKind kind) {
        return new Place(g, g.sectionOf(kind).orElseThrow(), g.workspace(kind).orElseThrow(), g.path(kind));
    }

    /**
     * The site's router: every workspace at its place, as a page of the app handed in - the
     * grouped workspace page's params, its kind the place's - and the site's root, home. The
     * trail is the group, then the workspace; the page is titled by the workspace.
     */
    public <M extends AppModule<GroupedWorkspacePageModule.Params, M>> Router router(StandardMpa mpa, M app, boolean server) {
        Map<String, AppPage<GroupedWorkspacePageModule.Params, M>> pages = new LinkedHashMap<>();
        for (var w : workspaces) pages.put(w.name(), mpa.page(app, new GroupedWorkspacePageModule.Params(w.name(), null, server)));
        return path -> place(path).map(p -> {
            var page = pages.get(p.workspace().kind().value());
            var trail = Trail.NONE.then(p.group().title(), "/");
            if (!path.isRoot()) trail = trail.then(p.workspace().title(), "/" + p.path());
            var t = trail;
            return (Navigable) q -> page.html(t, q);
        });
    }

    /** The manifests, by kind: {@code const <constName> = Object.freeze({ "<kind>": …, … });}. */
    public String manifestsJs(String constName) { return WorkspaceManifest.js(constName, workspaces); }

    /** What the module serving the manifests imports: every workspace's kinds and root parties, each once. */
    public <M extends EsModule> ImportsFor<M> manifestsImports() { return WorkspaceManifest.imports(workspaces); }

    /** The groups, for the page's directory: {@code const <constName> = Object.freeze([...]);}. */
    public String groupsJs(String constName) { return WorkspaceGroupsJs.constant(constName, groups); }
}
