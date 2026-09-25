package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.site.catalogue.Catalogue;
import hue.captains.singapura.js.homing.site.catalogue.L0_Catalogue;
import hue.captains.singapura.js.homing.site.catalogue.Leaf;
import hue.captains.singapura.js.homing.site.mpa.AppPage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * RFC 0058 — law 4, the half that needs the catalogue tree: <b>every group is
 * placed exactly once, and every kind is positioned exactly once</b>. The
 * registry already refuses a kind in two groups; this adds what only the tree
 * can show — a group placed twice or nowhere, an unregistered group placed, and
 * a kind that is <em>both</em> held by a group and placed as a flat kind
 * leaf, which is one kind at two positions.
 *
 * <p>A flat kind leaf on its own is not refused: it is the address every
 * pre-0058 link carries, and a site that has not placed groups is
 * unpositioned, not wrong. The law bites only when the two
 * addresses overlap.</p>
 *
 * <p>A static walk from a site's L0 root through {@code subCatalogues()} and
 * {@code leaves()} — the catalogue tree of core's {@code homing-site-catalogue},
 * the generic one, where a leaf is a slug over a page. A site calls it once its
 * groups are registered and its catalogues declared, so the boot fails with the
 * names rather than a placement going missing silently.</p>
 *
 * <p><b>What a leaf is read for.</b> A page is opaque to a catalogue; the ones
 * this law reads are an app bound to its params — a {@link AppPage}, as a
 * standard MPA makes it: {@code mpa.page(app, params)}. Its params say what it
 * places: {@link WorkspaceGroupApp.Params} a group, {@link WorkspaceApp.Params}
 * or {@link GenericWorkspace.Params} one kind, flat. Any other page is not a
 * workspace's, and is passed over.</p>
 */
public final class WorkspaceGroups {

    private WorkspaceGroups() {}

    /**
     * @throws IllegalStateException naming the group and both positions, the
     *         unplaced group, the unknown group, or the kind positioned twice
     */
    public static void assertPlacedOnce(L0_Catalogue<?> root, WorkspaceGroupRegistry registry) {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(registry, "registry");
        var placements = new LinkedHashMap<String, List<String>>();   // group id → where placed
        var kindLeaves = new LinkedHashMap<String, List<String>>();   // kind → where placed as a legacy leaf
        var unknown    = new ArrayList<String>();
        walk(root, new HashSet<>(), placements, kindLeaves, unknown, registry);

        var failures = new ArrayList<String>();
        for (WorkspaceGroup g : registry.all()) {
            List<String> at = placements.getOrDefault(g.id(), List.of());
            if (at.isEmpty()) {
                failures.add("group '" + g.id() + "' (" + g.kinds() + ") is placed nowhere under "
                        + root.getClass().getName() + " — place it as a leaf: Leaf.of(host, name, summary, "
                        + "mpa.page(WorkspaceGroupApp.INSTANCE, WorkspaceGroupApp.of(group)))");
            } else if (at.size() > 1) {
                failures.add("group '" + g.id() + "' is placed " + at.size() + " times — at " + at
                        + "; a group is placed exactly once");
            }
        }
        kindLeaves.forEach((kind, at) -> registry.groupOf(kind).ifPresent(g ->
                failures.add("kind '" + kind + "' is positioned twice: held by group '" + g.id()
                        + "' (#ws/…/" + kind + ") and placed as a legacy leaf at " + at
                        + " — a kind a group holds is a path inside it, not a leaf; retire the leaf")));
        for (String u : unknown) {
            failures.add("leaf places an unregistered group: " + u
                    + " — register it in WorkspaceGroupRegistry before the catalogue places it");
        }
        if (!failures.isEmpty()) {
            throw new IllegalStateException("RFC 0058 law 4 — workspace groups are not positioned exactly once ("
                    + failures.size() + "):\n  " + String.join("\n  ", failures));
        }
    }

    private static void walk(Catalogue<?> node, Set<Class<?>> seen,
                             Map<String, List<String>> placements, Map<String, List<String>> kindLeaves,
                             List<String> unknown, WorkspaceGroupRegistry registry) {
        if (!seen.add(node.getClass())) return;   // a catalogue is a singleton, known by its class
        for (Leaf<?> leaf : node.leaves()) {
            if (!(leaf.page() instanceof AppPage<?, ?> page)) continue;   // not an app's page: not a workspace's
            String where = node.getClass().getName() + " → " + leaf.slug().value();
            switch (page.params()) {
                case WorkspaceGroupApp.Params p -> {
                    if (registry.get(p.ws_group()).isEmpty()) unknown.add("'" + p.ws_group() + "' at " + where);
                    else placements.computeIfAbsent(p.ws_group(), k -> new ArrayList<>()).add(where);
                }
                case WorkspaceApp.Params p ->
                        kindLeaves.computeIfAbsent(p.ws_kind(), k -> new ArrayList<>()).add(where);
                case GenericWorkspace.Params p ->
                        kindLeaves.computeIfAbsent(p.ws_kind(), k -> new ArrayList<>()).add(where);
                default -> { }
            }
        }
        for (Catalogue<?> child : node.subCatalogues()) {
            walk(child, seen, placements, kindLeaves, unknown, registry);
        }
    }
}
