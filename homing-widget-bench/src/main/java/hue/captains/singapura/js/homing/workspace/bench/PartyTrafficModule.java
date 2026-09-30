package hue.captains.singapura.js.homing.workspace.bench;

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
public record PartyTrafficModule() implements DomModule<PartyTrafficModule> {

    public static final PartyTrafficModule INSTANCE = new PartyTrafficModule();

    public record PartyTraffic() implements SelfContainedWidget<PartyTrafficModule> {
        @Override public String summary() { return "What passes between a page's content parties, line by line, over a summary of each and of its steward's fetches."; }
    }

    @Override
    public ImportsFor<PartyTrafficModule> imports() {
        return ImportsFor.<PartyTrafficModule>builder()
                .add(new ModuleImports<>(List.of(new domOpsParties()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ContentParamsModule.ContentParams()), ContentParamsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ContentBenchStyles.cb_traffic(), new ContentBenchStyles.cb_panel_title(), new ContentBenchStyles.cb_lines(),
                        new ContentBenchStyles.cb_log()), ContentBenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PartyTrafficModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new PartyTraffic())); }
}
