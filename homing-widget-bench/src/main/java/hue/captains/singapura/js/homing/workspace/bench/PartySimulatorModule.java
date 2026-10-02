package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.KeysModule;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.elements.Elements;
import hue.captains.singapura.js.homing.ui.focus.FocusStyles;
import hue.captains.singapura.js.homing.workspace.parties.MessagingPartyModule;

import java.util.List;

/**
 * A messaging party on the bench, simulated by hand: {@code new
 * PartySimulator(branch, tab, party)} - what passes in the party logged, and
 * any message typed as JSON sent down to every member once it reads as a kind
 * of the party's type. A tab's widget by the pane's law; bench tooling.
 */
public record PartySimulatorModule() implements DomModule<PartySimulatorModule> {

    public record PartySimulator() implements BranchComponent<PartySimulatorModule>, NeedKeyboard {
        @Override public String summary() { return "A messaging party simulated by hand: what passes in it logged, and free-form JSON sent down to its members."; }
        @Override public List<KeyBinding> keys() { return List.of(KeyBinding.of(Key.ESCAPE, "the keys given back")); }
    }

    public static final PartySimulatorModule INSTANCE = new PartySimulatorModule();

    @Override
    public ImportsFor<PartySimulatorModule> imports() {
        return ImportsFor.<PartySimulatorModule>builder()
                // the check a typed message passes before it goes down
                .add(new ModuleImports<>(List.of(new MessagingPartyModule.MessagingParty()), MessagingPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeysModule.Keys()), KeysModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new Elements.ButtonBuilder()), Elements.INSTANCE))
                // the log's lines, as the monitors' rows
                .add(new ModuleImports<>(List.of(new FocusStyles.fm_row(), new FocusStyles.fm_kind(), new FocusStyles.fm_name(), new FocusStyles.fm_component()), FocusStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new WidgetBenchStyles.wb_sim(), new WidgetBenchStyles.wb_sim_note(), new WidgetBenchStyles.wb_sim_log(),
                        new WidgetBenchStyles.wb_sim_input(), new WidgetBenchStyles.wb_sim_bar(), new WidgetBenchStyles.wb_sim_pick(),
                        new WidgetBenchStyles.wb_sim_said(), new WidgetBenchStyles.wb_sim_error()), WidgetBenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PartySimulatorModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new PartySimulator())); }
}
