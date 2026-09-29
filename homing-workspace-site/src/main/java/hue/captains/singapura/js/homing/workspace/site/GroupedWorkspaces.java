package hue.captains.singapura.js.homing.workspace.site;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.StampedParams;
import hue.captains.singapura.js.homing.server.HtmlPageContent;
import hue.captains.singapura.js.homing.site.Html;
import hue.captains.singapura.js.homing.site.Navigable;
import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Query;
import hue.captains.singapura.js.homing.site.Router;
import hue.captains.singapura.js.homing.site.Trail;
import hue.captains.singapura.js.homing.site.mpa.AppPage;
import hue.captains.singapura.js.homing.site.mpa.StandardMpa;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceGroupsJs;
import hue.captains.singapura.js.homing.workspace.groups.core.models.GroupPath;
import hue.captains.singapura.js.homing.workspace.groups.core.models.SplitGrid;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceGroup;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceGroups;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceArrangements;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceSpec;
import hue.captains.singapura.js.homing.workspace.shell.GridArrangementJs;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceManifest;

import java.util.ArrayList;
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
 * the workspace's.
 *
 * <p>THE AUTHENTIC PATH (RFC 0058): a group is a page of the site, reached from
 * outside at {@code /<group id>}; everything inside it is the group's own - a kind
 * is where the group files it, its {@link GroupPath}, named by the page's anchor
 * {@code #ws/<section>/<kind>}, which never reaches the server; which workspace of
 * the kind is a parameter, {@code ?ws_id=} or {@code ?ws_name=}, never a position.
 * The site's root sends to the first group, so a group has one address.</p>
 *
 * <p>And how each is arranged the first time, engine by engine - its
 * {@link WorkspaceArrangements}, the declarer's, as the workspace is: a workspace
 * with none for an engine starts with nothing open there.</p>
 *
 * <p>What the page has of them is generated here too: the manifests, by kind; the
 * split grid's arrangements, by kind; and the groups, for the page's directory.</p>
 */
public record GroupedWorkspaces(WorkspaceGroups groups, List<WorkspaceDeclaration> workspaces, List<WorkspaceArrangements<?>> arrangements) {

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
        arrangements = List.copyOf(Objects.requireNonNull(arrangements, "GroupedWorkspaces.arrangements"));
        var arranged = new HashSet<String>();
        for (var a : arrangements) {
            String kind = a.workspace().workspaceKind().value();
            if (!workspaces.contains(a.workspace())) throw new IllegalArgumentException("GroupedWorkspaces: arrangements of " + kind + ", which it does not serve");
            if (!arranged.add(kind)) throw new IllegalArgumentException("GroupedWorkspaces: two sets of arrangements of " + kind);
        }
    }

    /** Grouped, and arranged by none: each workspace starts with nothing open. */
    public GroupedWorkspaces(WorkspaceGroups groups, List<WorkspaceDeclaration> workspaces) { this(groups, workspaces, List.of()); }

    public static GroupedWorkspaces of(WorkspaceGroups groups, WorkspaceDeclaration... workspaces) {
        return new GroupedWorkspaces(groups, List.of(workspaces));
    }

    /** The same, with these arrangements besides - each of a workspace it serves, one set a workspace. */
    public GroupedWorkspaces arranged(WorkspaceArrangements<?>... more) {
        var all = new ArrayList<>(arrangements);
        all.addAll(List.of(more));
        return new GroupedWorkspaces(groups, workspaces, all);
    }

    /** The arrangements of the workspace of that kind, if it has any. */
    public Optional<WorkspaceArrangements<?>> arrangements(String kind) {
        return arrangements.stream().filter(a -> a.workspace().workspaceKind().value().equals(kind)).findFirst();
    }

    /** The workspace of that kind, as the site serves it. */
    public Optional<WorkspaceDeclaration> declaration(String kind) {
        return workspaces.stream().filter(w -> w.name().equals(kind)).findFirst();
    }

    /** The group the site's root sends to: the first. */
    public WorkspaceGroup home() { return groups.groups().get(0); }

    /** The group at a path: {@code /<group id>}; anything else, none - a kind is its group's own, in the page's anchor. */
    public Optional<WorkspaceGroup> group(Path path) {
        if (path.depth() != 1) return Optional.empty();
        String id = path.segments().get(0);
        return groups.groups().stream().filter(g -> g.id().value().equals(id)).findFirst();
    }

    /** Where a group is on the site: {@code /<its id>} - the address it is reached by from outside. */
    public static String address(WorkspaceGroup group) { return "/" + group.id().value(); }

    /**
     * The site's router: every group at its address, as a page of the app handed in - the
     * grouped workspace page's params, its group the route's - and the site's root, sent to
     * the first group. The trail the server writes is the group; the page carries it on from
     * its anchor - the section, the workspace - and is titled by the workspace.
     */
    public <M extends AppModule<GroupedWorkspacePageModule.Params, M>> Router router(StandardMpa mpa, M app, boolean server) {
        Map<String, AppPage<GroupedWorkspacePageModule.Params, M>> pages = new LinkedHashMap<>();
        for (var g : groups.groups()) pages.put(g.id().value(), mpa.page(app, new GroupedWorkspacePageModule.Params(g.id().value(), null, null, server)));
        String home = address(home());
        return path -> {
            if (path.isRoot()) return Optional.of(q -> sentTo(home, q));
            return group(path).map(g -> {
                var page = pages.get(g.id().value());
                var trail = Trail.NONE.then(g.title(), address(g));
                return (Navigable) q -> page.html(trail, q);
            });
        };
    }

    /**
     * A page that sends the browser on - the query carried, and the anchor, which only the
     * browser has: the site's root is no group's second address.
     */
    static HtmlPageContent sentTo(String address, Query query) {
        String to = query.isEmpty() ? address : address + "?" + query;
        return new HtmlPageContent("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta http-equiv="refresh" content="0;url=%s">
                    <title>Redirecting…</title>
                </head>
                <body>
                    <script>window.location.replace(%s + window.location.hash);</script>
                </body>
                </html>
                """.formatted(Html.escape(to), StampedParams.jsString(to)));
    }

    /** The manifests, by kind: {@code const <constName> = Object.freeze({ "<kind>": …, … });}. */
    public String manifestsJs(String constName) { return WorkspaceManifest.js(constName, workspaces); }

    /** What the module serving the manifests imports: every workspace's kinds and root parties, each once. */
    public <M extends EsModule> ImportsFor<M> manifestsImports() { return WorkspaceManifest.imports(workspaces); }

    /**
     * The split grid's arrangements, by kind: {@code const <constName> = Object.freeze({ "<kind>": …, … });} -
     * each workspace's that has one, what the grouped page hands the shell for a log that holds nothing yet.
     */
    public String arrangementsJs(String constName) {
        if (!GridArrangementJs.CONST_NAME.matcher(constName).matches()) throw new IllegalArgumentException("not a constant's name: " + constName);
        var entries = new ArrayList<String>();
        for (var a : arrangements) gridEntry(a).ifPresent(entries::add);
        return "const " + constName + " = Object.freeze({" + (entries.isEmpty() ? "" : " " + String.join(", ", entries) + " ") + "});";
    }

    private static <W extends WorkspaceSpec> Optional<String> gridEntry(WorkspaceArrangements<W> a) {
        return a.forEngine(SplitGrid.ENGINE, SplitGrid.class)
                .map(g -> "\"" + a.workspace().workspaceKind().value() + "\": " + GridArrangementJs.expression(g));
    }

    /** The groups, for the page's directory: {@code const <constName> = Object.freeze([...]);}. */
    public String groupsJs(String constName) { return WorkspaceGroupsJs.constant(constName, groups); }
}
