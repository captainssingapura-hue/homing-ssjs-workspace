package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.component.keyboard.FocusPartyModule;
import hue.captains.singapura.js.homing.component.keyboard.focusParty;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParty;
import hue.captains.singapura.js.homing.workspace.content.ContentSecretaryModule;
import hue.captains.singapura.js.homing.workspace.content.FlowContentModule;
import hue.captains.singapura.js.homing.workspace.content.FlowModule;
import hue.captains.singapura.js.homing.workspace.parties.MessagingPartyModule;

import java.util.List;

/**
 * Content parties, on the bench: {@code /content} - two panels, each a scope of its own linked
 * under the page's parties, their cards and flows asking for content with their params; the
 * page's parties hire one steward each, and each item is fetched once. The traffic under them.
 */
public record ContentBenchApp() implements AppModule<ContentBenchApp.Params, ContentBenchApp> {

    public static final ContentBenchApp INSTANCE = new ContentBenchApp();

    /** None: the page shows the one arrangement it has. */
    public record Params() implements AppModule._Param {}

    record appMain() implements AppModule._AppMain<Params, ContentBenchApp> {}

    public static final ParamCodec<Params> CODEC = ParamCodec.ofEmpty(Params::new);

    @Override public String title()      { return "Content bench"; }
    @Override public String simpleName() { return "content-bench"; }
    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public ParamCodec<Params> paramCodec() { return CODEC; }

    @Override
    public ImportsFor<ContentBenchApp> imports() {
        return ImportsFor.<ContentBenchApp>builder()
                // the page's parties, their types, their one secretary, the stewards they hire
                .add(new ModuleImports<>(List.of(new MessagingPartyModule.MessagingParty()), MessagingPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ContentSecretaryModule.ContentSecretary()), ContentSecretaryModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new BenchNoteModule.BENCH_NOTE()), BenchNoteModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new FlowContentModule.FLOW()), FlowContentModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new BenchNoteStewardModule.BenchNoteSteward()), BenchNoteStewardModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new BenchFlowStewardModule.BenchFlowSteward()), BenchFlowStewardModule.INSTANCE))
                // the scopes, the kinds they offer their parts, and the traffic
                .add(new ModuleImports<>(List.of(new ContentPanelModule.ContentPanel()), ContentPanelModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new NoteCardModule.NoteCard()), NoteCardModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new FlowModule.Flow()), FlowModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new PartyTrafficModule.PartyTraffic()), PartyTrafficModule.INSTANCE))
                // the page's own parties, where the panels are grafted
                .add(new ModuleImports<>(List.of(new domOpsParty()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new focusParty()), FocusPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ContentBenchStyles.cb_page(), new ContentBenchStyles.cb_intro(), new ContentBenchStyles.cb_panels()),
                        ContentBenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ContentBenchApp> exports() { return new ExportsOf<>(INSTANCE, List.of(new appMain())); }
}
