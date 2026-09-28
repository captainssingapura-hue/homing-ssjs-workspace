package hue.captains.singapura.js.homing.workspace.core;

import hue.captains.singapura.js.homing.workspace.core.models.WidgetId;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * How a widget is named for the eye - a tab's label, a row in a list of the
 * widgets a workspace holds: from its kind's title, as its declaration says it,
 * and its id. A display rule, and only that: the id is the widget's model and
 * says which widget; what it is called is this rule's, so a page may name
 * widgets otherwise without touching what an id is. Every placement asks the
 * same rule, so a widget is called the same wherever it is shown.
 */
public interface WidgetTitleRule extends StatelessFunctionalObject {

    /** The widget's title: its kind's title, as the rule words it for this id. */
    String title(String kindTitle, WidgetId id);
}
