package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.component.keyboard.KeysModule;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.elements.Elements;
import hue.captains.singapura.js.homing.ui.focus.FocusMonitorModule;
import hue.captains.singapura.js.homing.ui.focus.StewardMonitorModule;

import java.util.List;

/**
 * The Focus lab's widgets (RFC 0066 E3, keyboard §17.5): a form of native
 * fields in a group of its own and a list — two logical members under the
 * room — a summoner that focuses a form's field or claims its list by
 * script, now or after a delay, and a monitor: the steward's lamp and the
 * focus tree. Nothing here marks where the focus is; the steward does.
 */
public record DemoFocusLabModule() implements DomModule<DemoFocusLabModule> {

    public static final DemoFocusLabModule INSTANCE = new DemoFocusLabModule();

    /** Native fields in a logical group, and a logical list, under the room. */
    public record FormWidget() implements Exportable._Class<DemoFocusLabModule> {}

    /** Every form open, with its field to focus and its list to claim, by script. */
    public record SummonerWidget() implements Exportable._Class<DemoFocusLabModule> {}

    /** The steward's lamp over the focus tree. */
    public record MonitorWidget() implements Exportable._Class<DemoFocusLabModule> {}

    @Override
    public ImportsFor<DemoFocusLabModule> imports() {
        return ImportsFor.<DemoFocusLabModule>builder()
                .add(new ModuleImports<>(List.of(new KeysModule.Keys()), KeysModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new Elements.ButtonBuilder()), Elements.INSTANCE))
                .add(new ModuleImports<>(List.of(new StewardMonitorModule.StewardMonitor()), StewardMonitorModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new FocusMonitorModule.FocusMonitor()), FocusMonitorModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new DemoWidgetStyles.dw_lab(), new DemoWidgetStyles.dw_lab_group(),
                        new DemoWidgetStyles.dw_lab_row(), new DemoWidgetStyles.dw_lab_item(),
                        new DemoWidgetStyles.dw_lab_item_on(), new DemoWidgetStyles.dw_hint()), DemoWidgetStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<DemoFocusLabModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new FormWidget(), new SummonerWidget(), new MonitorWidget()));
    }
}
