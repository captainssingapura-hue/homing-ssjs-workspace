package hue.captains.singapura.js.homing.workspace.core;

import hue.captains.singapura.js.homing.workspace.core.models.WidgetId;
import hue.captains.singapura.js.homing.workspace.core.models.WidgetName;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.Optional;

/**
 * How a widget is named for the eye - a tab's label, a row in a list of the
 * widgets a workspace holds: from its kind's title, as its declaration says it,
 * and its id. A display rule, and only that: the id is the widget's model and
 * says which widget; what it is called is this rule's, so a page may name
 * widgets otherwise without touching what an id is. Every placement asks the
 * same rule, so a widget is called the same wherever it is shown.
 */
public interface WidgetTitleRule extends StatelessFunctionalObject {

    /** The widget's title as it opens: its kind's title, as the rule words it for this id. */
    String title(String kindTitle, WidgetId id);

    /**
     * The widget's title now: the name a user gave it, when one is - else as it
     * opened, by its identity. The derived title is only ever the first name.
     */
    default String titleOf(String kindTitle, WidgetId id, Optional<WidgetName> name) {
        return name.map(WidgetName::value).orElseGet(() -> title(kindTitle, id));
    }
}
