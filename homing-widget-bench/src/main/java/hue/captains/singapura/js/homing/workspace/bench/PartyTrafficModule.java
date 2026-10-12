package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.ui.elements.LookModule;
import hue.captains.singapura.js.homing.ui.elements.CastingsModule;
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
 * Bench tooling: {@code new PartyTraffic(container, { parties })} - what passes between a page's
 * content parties, line by line, and a summary of each: its steward, what it holds, waits for and
 * failed, and what each steward fetched and how often the server was asked for it.
 */
@ConformanceDebt(rule = WidgetDrawsOnlyComponentsRule.class, findings = 19,
        reason = "It makes its own elements, places elements, wears classes of its own, writes attributes and writes text of its own: it waits for the designed containers, the widget slot and the content components to draw only components.")
public record PartyTrafficModule() implements DomModule<PartyTrafficModule> {

    public static final PartyTrafficModule INSTANCE = new PartyTrafficModule();

    public record PartyTraffic() implements SelfContainedWidget<PartyTrafficModule> {
        @Override public String summary() { return "What passes between a page's content parties, line by line, over a summary of each and of its steward's fetches."; }
    }

    @Override
    public ImportsFor<PartyTrafficModule> imports() {
        return ImportsFor.<PartyTrafficModule>builder()
                // its title, the house's heading of its level
                .add(new ModuleImports<>(List.of(new LookModule.Look()), LookModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new CastingsModule.ROLE()), CastingsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new domOpsParties()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ContentParamsModule.ContentParams()), ContentParamsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ContentBenchStyles.cb_traffic(), new ContentBenchStyles.cb_panel_title(), new ContentBenchStyles.cb_lines(),
                        new ContentBenchStyles.cb_log()), ContentBenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PartyTrafficModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new PartyTraffic())); }
}
