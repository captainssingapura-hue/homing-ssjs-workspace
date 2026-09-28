package hue.captains.singapura.js.homing.workspace.widgets;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.parties.PartyType;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A workspace, declared: its name, the kinds that can be opened in it, and the
 * secretaries it puts at the root of a party in place of a type's default.
 * A stateless object: everything a page needs to stand the workspace up, known
 * before anything runs.
 *
 * <p>Its ROOT PARTIES are resolved here, at type level (Messaging Parties Are
 * Joined Top-Down): one of each type any of its kinds declares, in the order
 * first declared, each with the secretary at its root - the workspace's own
 * for that type, else the type's default. The workspace hosts them, made when
 * it is made, before any widget opens; no page keeps a root of its own. A type
 * with no secretary for its root, or not served on a page, fails here - the
 * build, never the page - as does a secretary put at the root of a type no
 * kind joins.</p>
 */
public interface WorkspaceDeclaration extends StatelessFunctionalObject {

    /** A workspace's name: a kind of workspace, and of its log - letters, digits, hyphen, underscore. */
    Pattern NAME = Pattern.compile("[A-Za-z0-9_-]+");

    /** The workspace's name: its kind, which its log is kept under. */
    String name();

    /** What can be opened in it, in the order a page offers them. */
    List<WidgetDeclaration<?>> kinds();

    /** The secretaries it puts at the root of a party in place of the type's default: by the type's name. None, unless said. */
    default Map<String, ModuleImports<?>> secretaries() { return Map.of(); }

    /** A root party: its type, and the secretary at its root. */
    record RootParty(PartyType<?> type, ModuleImports<?> secretary) {
        public RootParty {
            Objects.requireNonNull(type, "RootParty.type");
            Objects.requireNonNull(secretary, "RootParty.secretary");
        }
    }

    /** The root parties, resolved from the kinds; refused, saying why, when they do not hold together. */
    default List<RootParty> rootParties() { return resolve(this); }

    /**
     * The root parties of a declaration: one of each type its kinds declare, in the
     * order first declared, each with its secretary. Refused, every reason named:
     * a name not a workspace's; two kinds of one name; two types of one name; a
     * type not served on a page, or with no secretary for its root; a secretary put
     * at the root of a type no kind joins.
     */
    static List<RootParty> resolve(WorkspaceDeclaration d) {
        var problems = new ArrayList<String>();
        if (d.name() == null || !NAME.matcher(d.name()).matches()) problems.add("its name '" + d.name() + "' is not a workspace's: letters, digits, hyphen, underscore");
        var kinds = new HashSet<String>();
        var types = new LinkedHashMap<String, PartyType<?>>();
        for (WidgetDeclaration<?> k : d.kinds()) {
            if (!kinds.add(k.kind())) problems.add("two kinds named " + k.kind());
            for (PartyType<?> t : k.parties()) {
                PartyType<?> was = types.putIfAbsent(t.name(), t);
                if (was != null && !was.equals(t)) problems.add("two types named " + t.name() + ": " + k.kind() + "'s is not the one declared before it");
            }
        }
        var roots = new ArrayList<RootParty>();
        for (PartyType<?> t : types.values()) {
            if (t.constant().isEmpty()) problems.add("the type " + t.name() + " is not served on a page: it has no constant's module");
            ModuleImports<?> secretary = d.secretaries().containsKey(t.name()) ? d.secretaries().get(t.name()) : t.secretary().orElse(null);
            if (secretary == null) { problems.add("the type " + t.name() + " has no secretary for its root: neither its default nor the workspace's"); continue; }
            PartyType.exportName(secretary);
            roots.add(new RootParty(t, secretary));
        }
        for (String name : d.secretaries().keySet()) {
            if (!types.containsKey(name)) problems.add("a secretary put at the root of " + name + ", which no kind joins");
        }
        if (!problems.isEmpty()) throw new IllegalStateException("the workspace '" + d.name() + "' does not hold together:\n  " + String.join("\n  ", problems));
        return List.copyOf(roots);
    }
}
