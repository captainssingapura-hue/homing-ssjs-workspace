package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.elements.Elements;

import java.util.List;

/**
 * A transient pane its host owns: {@code new PanePicker(branch, { kinds, onPick,
 * onCancel })} - the kinds the workspace can open, a button each, and Cancel.
 * Picked, it asks and acts on nothing itself: the pick becomes a request the
 * core executes, and the host lets its picker go.
 */
public record PanePickerModule() implements DomModule<PanePickerModule> {

    public record PanePicker() implements BranchComponent<PanePickerModule> {
        @Override public String summary() { return "A transient pane its host owns: the kinds that can be opened and Cancel - picked, it asks, and acts on nothing itself."; }
    }

    public static final PanePickerModule INSTANCE = new PanePickerModule();

    @Override
    public ImportsFor<PanePickerModule> imports() {
        return ImportsFor.<PanePickerModule>builder()
                .add(new ModuleImports<>(List.of(new Elements.ButtonBuilder()), Elements.INSTANCE))
                .add(new ModuleImports<>(List.of(new WidgetBenchStyles.wb_picker(), new WidgetBenchStyles.wb_ws_label()), WidgetBenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PanePickerModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new PanePicker())); }
}
