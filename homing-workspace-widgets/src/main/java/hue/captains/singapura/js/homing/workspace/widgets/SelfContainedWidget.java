package hue.captains.singapura.js.homing.workspace.widgets;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.core.DomModule;

/**
 * A self-contained widget, declared as a component: {@code new Widget(container,
 * params)}, its DomOpsParty and FocusParty roots its own. The component model
 * declares an element component and a branch component - one takes the element
 * its caller minted, the other the sub-branch its caller made - and has no shape
 * for a widget that takes neither. Until it has, the declaration stands on the
 * branch component's line, the one open to extension, and says it is a widget.
 *
 * @param <M> the module that exports the class
 */
public interface SelfContainedWidget<M extends DomModule<M>> extends BranchComponent<M> {
    @Override default Shape shape() { return Shape.WIDGET; }
}
