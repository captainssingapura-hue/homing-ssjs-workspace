package hue.captains.singapura.js.homing.workspace.widgets;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.parties.PartyType;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A widget kind, declared: its name, the class a page constructs, and how its
 * params are read off an address. Everything a page needs to stand a widget
 * up from a kind and a query, and nothing of the page's.
 *
 * <p>The class is made {@code new Widget(container, params)}: the container
 * the page lends it - the page's, which the widget fills and never shapes -
 * and its params, as the page carries them. It makes its own roots: its
 * DomOpsParty a mobile party from the party of parties, offered to its host as
 * {@code widget.roots.dom} for the host to graft into the page's tree; its
 * FocusParty root as the focus party is today. It leaves nothing when disposed
 * (A Widget Is an Operational Unit; Widgets Are Independent; Parties Are
 * Grafted Hierarchies).</p>
 *
 * <p>A stateless object: what it says is the kind's, the same for every
 * widget of it, and a page, a bench or a workspace reads it before any widget
 * is made.</p>
 *
 * @param <P> the widget's params
 */
public interface WidgetDeclaration<P extends WidgetParams> extends StatelessFunctionalObject {

    /** A kind's name: lowercase letters, digits and hyphens, a letter first. */
    Pattern KIND = Pattern.compile("[a-z][a-z0-9-]*");

    /** The kind's name on an address: {@code books-grid}. */
    String kind();

    /** The kind's name for the eye - a tab's title, a toggle's label: its name, spaced and capitalised, unless said. */
    default String title() {
        String spaced = kind().replace('-', ' ');
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }

    /** The widget's params record. */
    Class<P> paramsType();

    /** Its params on an address, both ways. */
    WidgetQuery<P> query();

    /** The import of the widget's class from the module that exports it: what a page constructs. */
    ModuleImports<?> constructs();

    /** The class's name in JavaScript: the one export {@link #constructs()} imports. */
    default String className() {
        List<?> exports = constructs().allImports();
        if (exports.size() != 1) throw new IllegalStateException(kind() + ": a widget kind constructs one class, not " + exports.size());
        return exports.get(0).getClass().getSimpleName();
    }

    /** Params held as the interface, written as this kind writes them. */
    default Map<String, List<String>> toQuery(WidgetParams params) { return query().to(paramsType().cast(params)); }

    /**
     * The messaging parties' types a widget of this kind joins for its full
     * function (Messaging Parties Are Joined Top-Down): a function of the kind
     * alone - the same for every widget of it, whatever its params - declared
     * here, in Java, so what a host must give is known before anything runs. A
     * kind whose needs would vary with its params declares all it may need. A
     * widget works alone without them; each joined adds what that type is for.
     * An umbrella's are its subordinates' union, derived from their
     * declarations, less a type it keeps wholly to itself. None, unless said.
     * The widget is given these, and only these: it never says them itself.
     */
    default List<PartyType<?>> parties() { return List.of(); }

    /**
     * Whether a workspace holds at most one widget of this kind - one that is
     * the authority for what it does, say: a game others watch, whose stream
     * two of would tangle. Opened again, the one there is shown instead of a
     * second made. A function of the kind alone, as its parties are. Many,
     * unless said.
     */
    default boolean single() { return false; }
}
