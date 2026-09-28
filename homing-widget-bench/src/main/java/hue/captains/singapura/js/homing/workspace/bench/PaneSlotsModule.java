package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetStyles;

import java.util.List;

/**
 * The bench's register of panes: {@code new PaneSlots(branch)} - the core's
 * port, lending a widget its pane, a slot placed nowhere, when it creates it,
 * and closing it with the widget. Where a slot is shown is the placement's.
 */
public record PaneSlotsModule() implements DomModule<PaneSlotsModule> {

    public record PaneSlots() implements BranchComponent<PaneSlotsModule> {
        @Override public String summary() { return "A register of panes: a widget's slot lent placed nowhere when the core creates it, and closed with the widget - the core's port."; }
    }

    public static final PaneSlotsModule INSTANCE = new PaneSlotsModule();

    @Override
    public ImportsFor<PaneSlotsModule> imports() {
        return ImportsFor.<PaneSlotsModule>builder()
                .add(new ModuleImports<>(List.of(new WidgetStyles.wg_slot()), WidgetStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PaneSlotsModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new PaneSlots())); }
}
