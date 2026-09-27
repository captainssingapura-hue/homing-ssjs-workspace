package hue.captains.singapura.js.homing.workspace.widgets;

import hue.captains.singapura.js.homing.core.ModuleImports;

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
 * and its params, as the page carries them. It makes its own roots in the
 * DomOpsParty and the FocusParty, and leaves nothing when disposed (A Widget
 * Is an Operational Unit; Widgets Are Independent).</p>
 *
 * @param <P> the widget's params
 */
public interface WidgetDeclaration<P extends WidgetParams> {

    /** A kind's name: lowercase letters, digits and hyphens, a letter first. */
    Pattern KIND = Pattern.compile("[a-z][a-z0-9-]*");

    /** The kind's name on an address: {@code books-grid}. */
    String kind();

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
}
