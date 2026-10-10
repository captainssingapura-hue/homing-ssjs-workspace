package hue.captains.singapura.js.homing.workspace.monitors;

import hue.captains.singapura.js.homing.component.WidgetSlotModule;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.monitors.FocusMonitorModule;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;

import java.util.List;

/** The page's logical-focus tree, a monitor widget: {@code new FocusTree(container, params)}, the FocusMonitor widget shown in a slot over its box. */
public record FocusTreeModule() implements DomModule<FocusTreeModule> {

    public record FocusTree() implements SelfContainedWidget<FocusTreeModule>, NeedKeyboard {
        @Override public String summary() { return "The page's logical-focus tree, a widget of its own: the holder of the keys lit, a grafted party under its proxy."; }
        @Override public List<KeyBinding> keys() { return MonitorModule.KEYS; }
    }

    public static final FocusTreeModule INSTANCE = new FocusTreeModule();

    @Override
    public ImportsFor<FocusTreeModule> imports() {
        return ImportsFor.<FocusTreeModule>builder()
                .add(new ModuleImports<>(List.of(new MonitorModule.Monitor()), MonitorModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new FocusMonitorModule.FocusMonitor()), FocusMonitorModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WidgetSlotModule.WidgetSlot()), WidgetSlotModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<FocusTreeModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new FocusTree())); }
}
