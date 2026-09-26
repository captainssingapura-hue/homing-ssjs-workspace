package hue.captains.singapura.js.homing.workspace.codecs.log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The workspace log's JavaScript modules: one per top-level Java declaration
 * the manifest's types are in — the type and every type nested in it — so a
 * module is to its Java file what the file is to its types, and names itself
 * after it: {@code Layout} is {@code LayoutModule}. The small types are
 * gathered in Java as nested types of a holder ({@code LogIds}), and are one
 * module because they are one file.
 *
 * <p>A module imports, from another, the class and the codec of each type its
 * own types name there; and from {@code LogWireModule} the wire's reader,
 * which every generated class and codec leans on. Imports run one way: a cycle
 * between modules is refused here, as a gap in the manifest is. Within a
 * module a variant extends its family, so a record variant must be declared in
 * its family's file; a family of families is joined to its families by their
 * codecs and {@code instanceof} only, which import downward.</p>
 */
public final class LogModules {

    private LogModules() {}

    /** The Java package — and the JavaScript path — the modules are generated into. */
    public static final String PACKAGE = "hue.captains.singapura.js.homing.workspace.log.js";

    /** The wire's reader: a class every module imports, in a module of its own. */
    public static final String WIRE = "LogWire";

    /** The module the wire's reader is served from. */
    public static final String WIRE_MODULE = WIRE + "Module";

    /**
     * One module: the top-level declaration it is named after, the manifest's
     * types declared in it in the manifest's order, and what it imports from each
     * other module — the modules in import order, the names in the order met.
     */
    public record Module(Class<?> top, List<LogCodecEntry<?>> entries, Map<String, List<String>> imports) {

        public String name() { return top.getSimpleName() + "Module"; }

        /** Each type's class and codec, in the manifest's order. */
        public List<String> exports() {
            var out = new ArrayList<String>();
            for (var e : entries) {
                out.add(LogShapes.simple(e.type()));
                out.add(LogShapes.codec(e.type()));
            }
            return out;
        }
    }

    /** The top-level declaration a type is in: itself, or the class it is nested in, outermost. */
    static Class<?> top(Class<?> t) {
        Class<?> c = t;
        while (c.getEnclosingClass() != null) c = c.getEnclosingClass();
        return c;
    }

    /**
     * The manifest's types the code generated for this one names: a record's
     * components' types and — a variant — its family; a sealed type's members.
     */
    static List<Class<?>> refs(Class<?> t) {
        var out = new ArrayList<Class<?>>();
        if (t.isRecord()) {
            for (var c : LogShapes.components(t)) LogSlot.typesIn(c.slot(), out);
            Class<?> family = LogShapes.sealedParent(t);
            if (family != null) out.add(family);
        } else if (SumCodeGen.isFamily(t)) {
            out.addAll(SumCodeGen.variants(t));
        }
        return out;
    }

    /**
     * The modules of these entries, each after every module it imports; the wire's
     * reader's is not among them, and comes before all. Refused, naming why: a
     * record variant declared outside its family's file, or modules that would
     * import each other.
     */
    public static List<Module> of(List<LogCodecEntry<?>> entries) {
        var byTop = new LinkedHashMap<Class<?>, List<LogCodecEntry<?>>>();
        for (var e : entries) byTop.computeIfAbsent(top(e.type()), k -> new ArrayList<>()).add(e);

        var problems = new ArrayList<String>();
        var deps = new LinkedHashMap<Class<?>, Map<Class<?>, Set<String>>>();
        for (var g : byTop.entrySet()) {
            var from = new LinkedHashMap<Class<?>, Set<String>>();
            for (var e : g.getValue()) {
                Class<?> t = e.type();
                for (Class<?> r : refs(t)) {
                    Class<?> rt = top(r);
                    if (rt == g.getKey()) continue;
                    if (t.isRecord() && r == LogShapes.sealedParent(t)) {
                        problems.add(t.getName() + " is a variant of " + r.getName() + " but is not declared in its file:"
                                + " a JavaScript class extends only what is in its own module");
                        continue;
                    }
                    var names = from.computeIfAbsent(rt, k -> new LinkedHashSet<>());
                    names.add(LogShapes.simple(r));
                    names.add(LogShapes.codec(r));
                }
            }
            deps.put(g.getKey(), from);
        }
        if (!problems.isEmpty()) throw new IllegalStateException("the workspace log's modules do not hold together:\n  " + String.join("\n  ", problems));

        // each module after all it imports; among those free to come next, the manifest's first
        var order = new ArrayList<Class<?>>();
        var placed = new HashSet<Class<?>>();
        while (order.size() < byTop.size()) {
            Class<?> next = null;
            for (Class<?> g : byTop.keySet()) {
                if (!placed.contains(g) && placed.containsAll(deps.get(g).keySet())) { next = g; break; }
            }
            if (next == null) throw new IllegalStateException("the workspace log's modules would import each other: " + cycle(byTop.keySet(), placed, deps));
            order.add(next);
            placed.add(next);
        }

        var out = new ArrayList<Module>();
        for (Class<?> g : order) {
            var imports = new LinkedHashMap<String, List<String>>();
            for (Class<?> d : order) {
                var names = deps.get(g).get(d);
                if (names != null) imports.put(d.getSimpleName() + "Module", List.copyOf(names));
            }
            out.add(new Module(g, List.copyOf(byTop.get(g)), imports));
        }
        return out;
    }

    /** One cycle among the modules not yet placed, named module by module. */
    private static String cycle(Set<Class<?>> all, Set<Class<?>> placed, Map<Class<?>, Map<Class<?>, Set<String>>> deps) {
        Class<?> at = null;
        for (Class<?> g : all) if (!placed.contains(g)) { at = g; break; }
        var path = new ArrayList<Class<?>>();
        while (!path.contains(at)) {
            path.add(at);
            for (Class<?> d : deps.get(at).keySet()) if (!placed.contains(d)) { at = d; break; }
        }
        var names = new ArrayList<String>();
        for (Class<?> g : path.subList(path.indexOf(at), path.size())) names.add(g.getSimpleName() + "Module");
        names.add(at.getSimpleName() + "Module");
        return String.join(" -> ", names);
    }
}
