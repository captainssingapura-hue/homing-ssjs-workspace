package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.elements.Elements;

import java.util.List;

/**
 * Two widgets for the demo workspace, written to the new contract: handed a
 * branch, they give back {@code { root }}, and touch neither the tab, the dock
 * nor the focus party. The room a tab gives them is the party's member.
 */
public record DemoWidgetsModule() implements DomModule<DemoWidgetsModule> {

    public static final DemoWidgetsModule INSTANCE = new DemoWidgetsModule();

    /** A paragraph; no keys at all. */
    public record NoteWidget() implements Exportable._Class<DemoWidgetsModule> {}

    /** Native buttons, and arrows handed on by the room. */
    public record CounterWidget() implements Exportable._Class<DemoWidgetsModule> {}

    @Override
    public ImportsFor<DemoWidgetsModule> imports() {
        return ImportsFor.<DemoWidgetsModule>builder()
                .add(new ModuleImports<>(List.of(new Elements.ButtonBuilder()), Elements.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new DemoWidgetStyles.dw_note(), new DemoWidgetStyles.dw_counter(),
                        new DemoWidgetStyles.dw_count(), new DemoWidgetStyles.dw_row(),
                        new DemoWidgetStyles.dw_hint()), DemoWidgetStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<DemoWidgetsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new NoteWidget(), new CounterWidget()));
    }
}
