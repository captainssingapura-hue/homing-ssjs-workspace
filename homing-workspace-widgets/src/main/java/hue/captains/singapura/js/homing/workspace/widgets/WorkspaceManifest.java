package hue.captains.singapura.js.homing.workspace.widgets;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.parties.PartyType;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A workspace's manifest, as its page has it: one frozen constant, generated
 * from its {@link WorkspaceDeclaration} by the module that serves it - the
 * kinds, each its class, its title and the types its declaration says it
 * joins - and, a kind a workspace holds one of, {@code single: true}; and the
 * root parties, each its type and the secretary at its root.
 *
 * <pre>
 * const BENCH_WORKSPACE = Object.freeze({ name: "bench-one-pane",
 *     kinds: Object.freeze({ "books-grid": Object.freeze({ Widget: BooksGrid, title: "Books grid", parties: Object.freeze([BOOK_SELECTION]) }), … }),
 *     parties: Object.freeze([Object.freeze({ type: BOOK_SELECTION, secretary: BookSelectionSecretary })]) });
 * </pre>
 *
 * <p>The module that serves it must be a DOM module, though it touches no
 * DOM: it imports the widgets' classes, and a plain module's imports are
 * served without the page's theme - a second copy of every DOM module the
 * widgets import, a second steward (the Keyboard's §15 hazard).</p>
 */
public final class WorkspaceManifest {

    private WorkspaceManifest() {}

    /** What the manifest's module imports: each kind's class, each root type's constant, each root secretary - each once. */
    public static <M extends EsModule> ImportsFor<M> imports(WorkspaceDeclaration d) { return imports(List.of(d)); }

    /** What a module serving several workspaces' manifests imports: every one's, each once. */
    public static <M extends EsModule> ImportsFor<M> imports(List<? extends WorkspaceDeclaration> ds) {
        var all = new LinkedHashSet<ModuleImports<?>>();
        for (WorkspaceDeclaration d : ds) {
            for (WidgetDeclaration<?> k : d.kinds()) all.add(k.constructs());
            for (var r : d.rootParties()) {
                all.add(r.type().constant().orElseThrow());
                all.add(r.secretary());
            }
        }
        var b = ImportsFor.<M>builder();
        for (ModuleImports<?> i : all) b.add(i);
        return b.build();
    }

    /** The manifest: {@code const <constName> = …}, from the declaration. */
    public static String js(String constName, WorkspaceDeclaration d) { return "const " + constName + " = " + expression(d) + ";"; }

    /**
     * Several workspaces' manifests, by their names - a site that serves more than one:
     * {@code const <constName> = Object.freeze({ "<name>": …, … });}. Two of one name are refused.
     */
    public static String js(String constName, List<? extends WorkspaceDeclaration> ds) {
        if (ds.stream().map(WorkspaceDeclaration::name).distinct().count() != ds.size()) throw new IllegalArgumentException("two workspaces of one name");
        return "const " + constName + " = Object.freeze({ " + ds.stream().map(d -> "\"" + d.name() + "\": " + expression(d)).collect(Collectors.joining(", ")) + " });";
    }

    /** One manifest, as an expression. */
    static String expression(WorkspaceDeclaration d) {
        var roots = d.rootParties();
        String kinds = d.kinds().stream().map(k -> "\"" + k.kind() + "\": Object.freeze({ Widget: " + k.className()
                        + ", title: \"" + k.title().replace("\"", "\\\"") + "\", parties: Object.freeze([" + names(k.parties()) + "])"
                        + (k.single() ? ", single: true" : "") + " })")
                .collect(Collectors.joining(", "));
        String parties = roots.stream().map(r -> "Object.freeze({ type: " + r.type().constName() + ", secretary: " + PartyType.exportName(r.secretary()) + " })")
                .collect(Collectors.joining(", "));
        return "Object.freeze({ name: \"" + d.name() + "\", kinds: Object.freeze({ " + kinds + " }), parties: Object.freeze([" + parties + "]) })";
    }

    private static String names(List<PartyType<?>> types) {
        return types.stream().map(PartyType::constName).collect(Collectors.joining(", "));
    }
}
