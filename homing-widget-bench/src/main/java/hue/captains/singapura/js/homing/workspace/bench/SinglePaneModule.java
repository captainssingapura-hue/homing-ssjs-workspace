package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.layers.PanePlacementModule;

import java.util.List;

/**
 * A placement of one pane: {@code new SinglePane(branch, { host, focus, panes, entry })} -
 * it mounts and unmounts the widgets' panes, one shown at a time, the others kept whole
 * and unshown, and never creates or closes one; with a transient picker of its own. The
 * core's placement port, called only in executing a request.
 */
public record SinglePaneModule() implements DomModule<SinglePaneModule> {

    public record SinglePane() implements BranchComponent<SinglePaneModule> {
        @Override public String summary() { return "A placement of one pane: widgets' panes mounted and unmounted, one shown at a time - its slot in the pane, its focus root grafted - the others kept whole; a transient picker of its own."; }
    }

    public static final SinglePaneModule INSTANCE = new SinglePaneModule();

    @Override
    public ImportsFor<SinglePaneModule> imports() {
        return ImportsFor.<SinglePaneModule>builder()
                // its transient picker pane
                .add(new ModuleImports<>(List.of(new PanePickerModule.PanePicker()), PanePickerModule.INSTANCE))
                // what it shows, headless
                .add(new ModuleImports<>(List.of(new PanePlacementModule.PanePlacement()), PanePlacementModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<SinglePaneModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new SinglePane())); }
}
