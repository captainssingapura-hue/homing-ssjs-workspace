package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetStyles;

import java.util.List;

/**
 * A placement of one pane: {@code new SinglePane(branch, { host, focus })} - every
 * widget of the workspace lent a slot of its own, one shown at a time, the others
 * kept whole and unshown. The port the headless core lends and takes back through.
 */
public record SinglePaneModule() implements DomModule<SinglePaneModule> {

    public record SinglePane() implements BranchComponent<SinglePaneModule> {
        @Override public String summary() { return "A placement of one pane: each widget lent a slot, one shown at a time - its slot in the pane, its focus root grafted - the others kept whole."; }
    }

    public static final SinglePaneModule INSTANCE = new SinglePaneModule();

    @Override
    public ImportsFor<SinglePaneModule> imports() {
        return ImportsFor.<SinglePaneModule>builder()
                .add(new ModuleImports<>(List.of(new WidgetStyles.wg_slot()), WidgetStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<SinglePaneModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new SinglePane())); }
}
