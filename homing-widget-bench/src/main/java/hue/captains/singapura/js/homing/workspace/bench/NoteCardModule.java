package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.ui.elements.LookModule;
import hue.captains.singapura.js.homing.ui.elements.CastingsModule;
import hue.captains.singapura.js.homing.component.keyboard.FocusPartyModule;
import hue.captains.singapura.js.homing.component.keyboard.focusParties;
import hue.captains.singapura.js.homing.conformance.rules.ConformanceDebt;
import hue.captains.singapura.js.homing.conformance.rules.WidgetDrawsOnlyComponentsRule;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParties;
import hue.captains.singapura.js.homing.workspace.content.ContentParamsModule;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;

import java.util.List;

/**
 * The content bench's widget: {@code new NoteCard(container, params)} - a note, asked of the
 * note party with the params it was made with, and shown when it comes.
 */
@ConformanceDebt(rule = WidgetDrawsOnlyComponentsRule.class, findings = 18,
        reason = "It makes its own elements, places elements, wears classes of its own, writes attributes and writes text of its own: it waits for the designed containers, the widget slot and the content components to draw only components.")
public record NoteCardModule() implements DomModule<NoteCardModule> {

    public static final NoteCardModule INSTANCE = new NoteCardModule();

    public record NoteCard() implements SelfContainedWidget<NoteCardModule> {
        @Override public String summary() { return "A note of the bench's: asked of the note party with its params, shown when it comes, or said unavailable."; }
    }

    @Override
    public ImportsFor<NoteCardModule> imports() {
        return ImportsFor.<NoteCardModule>builder()
                // its title, the house's heading of its level
                .add(new ModuleImports<>(List.of(new LookModule.Look()), LookModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new CastingsModule.ROLE()), CastingsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new domOpsParties()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new focusParties()), FocusPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new BenchNoteModule.BENCH_NOTE()), BenchNoteModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ContentParamsModule.ContentParams()), ContentParamsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ContentBenchStyles.cb_card(), new ContentBenchStyles.cb_key(), new ContentBenchStyles.cb_title(),
                        new ContentBenchStyles.cb_text(), new ContentBenchStyles.cb_waiting()), ContentBenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<NoteCardModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new NoteCard())); }
}
