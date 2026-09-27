package hue.captains.singapura.js.homing.workspace.monitors;

import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.focus.StewardMonitorModule;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;

import java.util.List;

/** Where the keys are, a monitor widget: {@code new StewardLamp(container, params)}, the StewardMonitor component in a widget of its own. */
public record StewardLampModule() implements DomModule<StewardLampModule> {

    public record StewardLamp() implements SelfContainedWidget<StewardLampModule>, NeedKeyboard {
        @Override public String summary() { return "Where the keys are, as one lamp, a widget of its own: held, lent or away, and the first invariant broken."; }
        @Override public List<KeyBinding> keys() { return MonitorModule.KEYS; }
    }

    public static final StewardLampModule INSTANCE = new StewardLampModule();

    @Override
    public ImportsFor<StewardLampModule> imports() {
        return ImportsFor.<StewardLampModule>builder()
                .add(new ModuleImports<>(List.of(new MonitorModule.Monitor()), MonitorModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new StewardMonitorModule.StewardMonitor()), StewardMonitorModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<StewardLampModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new StewardLamp())); }
}
