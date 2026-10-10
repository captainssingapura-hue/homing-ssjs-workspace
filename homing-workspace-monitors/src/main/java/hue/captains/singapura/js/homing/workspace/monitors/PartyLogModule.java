package hue.captains.singapura.js.homing.workspace.monitors;

import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.conformance.rules.ConformanceDebt;
import hue.captains.singapura.js.homing.conformance.rules.WidgetDrawsOnlyComponentsRule;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.component.keyboard.FocusPartyModule;
import hue.captains.singapura.js.homing.component.keyboard.KeyboardStewardModule;
import hue.captains.singapura.js.homing.component.keyboard.focusParty;
import hue.captains.singapura.js.homing.ui.monitors.FocusStyles;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;

import java.util.List;

/** What the page's parties say, a monitor widget: {@code new PartyLog(container, params)}, the last {@code keep} lines. */
@ConformanceDebt(rule = WidgetDrawsOnlyComponentsRule.class,
        reason = "It makes its own elements, places elements, wears classes of its own, writes attributes and writes text of its own: it waits for the designed containers, the widget slot and the content components to draw only components.")
public record PartyLogModule() implements DomModule<PartyLogModule> {

    public record PartyLog() implements SelfContainedWidget<PartyLogModule>, NeedKeyboard {
        @Override public String summary() { return "What the page's parties say, line by line, a widget of its own: the focus party's notices and the steward's events."; }
        @Override public List<KeyBinding> keys() { return MonitorModule.KEYS; }
    }

    public static final PartyLogModule INSTANCE = new PartyLogModule();

    @Override
    public ImportsFor<PartyLogModule> imports() {
        return ImportsFor.<PartyLogModule>builder()
                .add(new ModuleImports<>(List.of(new MonitorModule.Monitor()), MonitorModule.INSTANCE))
                // the page's focus party and its steward, listened to; the lines as the focus tree's rows
                .add(new ModuleImports<>(List.of(new focusParty()), FocusPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeyboardStewardModule.KeyboardStewardInstance()), KeyboardStewardModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new FocusStyles.fm_tree(), new FocusStyles.fm_row(), new FocusStyles.fm_kind(),
                        new FocusStyles.fm_name(), new FocusStyles.fm_component()), FocusStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PartyLogModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new PartyLog())); }
}
