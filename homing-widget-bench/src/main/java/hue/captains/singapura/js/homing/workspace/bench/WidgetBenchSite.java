package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.designs.HomingDesigns;
import hue.captains.singapura.js.homing.site.Navigable;
import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Router;
import hue.captains.singapura.js.homing.site.Site;
import hue.captains.singapura.js.homing.site.Trail;
import hue.captains.singapura.js.homing.site.mpa.AppPage;
import hue.captains.singapura.js.homing.site.mpa.Brand;
import hue.captains.singapura.js.homing.site.mpa.StandardMpa;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetParams;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetQuery;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The bench as a site: a page per kind, {@code /<kind>}, its params at their
 * defaults and the address's own query read over them - {@code /books-grid?numbers=on}
 * - and the root the first kind's; {@code /workspace}, {@code /tree} and {@code /content} beside them. The MPA's flat {@code /app?app=widget-bench&widget=…}
 * serves any kind the bench knows as well.
 */
public record WidgetBenchSite() implements Site {

    public static final WidgetBenchSite INSTANCE = new WidgetBenchSite();

    /** The one declaration this site makes: the brand, the designs, the crate it serves. */
    public static final StandardMpa MPA = StandardMpa.of(Brand.of("Widget bench"), HomingDesigns.REGISTRY, WidgetBenchCrate.INSTANCE);

    /** A kind's page, its params at their defaults: what the query reads to when it says nothing. */
    static final Map<String, AppPage<?, ?>> PAGES = WidgetBench.KINDS.stream().collect(Collectors.toMap(WidgetDeclaration::kind,
            k -> MPA.page(WidgetBenchApp.INSTANCE, new WidgetBenchApp.Params(k.kind(), defaults(k)))));

    /** A workspace of one pane, beside the kinds' pages: {@code /workspace}. No kind is named so. */
    static final AppPage<?, ?> WORKSPACE = MPA.page(WorkspaceBenchApp.INSTANCE, new WorkspaceBenchApp.Params());

    /** The tree placement, beside the kinds' pages: {@code /tree}. No kind is named so. */
    static final AppPage<?, ?> TREE = MPA.page(TreeBenchApp.INSTANCE, new TreeBenchApp.Params());

    /** Content parties, beside the kinds' pages: {@code /content}. No kind is named so. */
    static final AppPage<?, ?> CONTENT = MPA.page(ContentBenchApp.INSTANCE, new ContentBenchApp.Params());

    @Override public String name() { return "widget-bench"; }

    @Override
    public Router router() {
        return path -> {
            if (path.isRoot()) return Optional.of(placed(WidgetBench.KINDS.get(0).kind(), path));
            if (path.depth() != 1) return Optional.empty();
            if (path.head().filter("workspace"::equals).isPresent()) {
                var trail = Trail.NONE.then("Widget bench", "/").then("workspace", path.toString());
                return Optional.of((Navigable) q -> WORKSPACE.html(trail, q));
            }
            if (path.head().filter("tree"::equals).isPresent()) {
                var trail = Trail.NONE.then("Widget bench", "/").then("tree", path.toString());
                return Optional.of((Navigable) q -> TREE.html(trail, q));
            }
            if (path.head().filter("content"::equals).isPresent()) {
                var trail = Trail.NONE.then("Widget bench", "/").then("content", path.toString());
                return Optional.of((Navigable) q -> CONTENT.html(trail, q));
            }
            return path.head().filter(PAGES::containsKey).map(kind -> placed(kind, path));
        };
    }

    /** A kind's page, told the trail the router knows; the chrome draws it. */
    private static Navigable placed(String kind, Path path) {
        var trail = path.isRoot() ? Trail.NONE.then("Widget bench", "/")
                                  : Trail.NONE.then("Widget bench", "/").then(kind, path.toString());
        return q -> PAGES.get(kind).html(trail, q);
    }

    /** A kind's params at their defaults: what its query reads to from an address that says nothing. */
    private static WidgetParams defaults(WidgetDeclaration<?> k) {
        if (k.query().from(Map.of()) instanceof WidgetQuery.Read.Ok<?> ok) return ok.params();
        throw new IllegalStateException(k.kind() + ": an address that says nothing must read to the kind's defaults");
    }
}
