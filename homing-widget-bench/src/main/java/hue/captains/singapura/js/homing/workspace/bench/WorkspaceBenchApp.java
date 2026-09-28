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
import hue.captains.singapura.js.homing.workspace.log.js.LogKeyModule;
import hue.captains.singapura.js.homing.workspace.log.store.IndexedDbLogModule;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLoadModule;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLogIdentityModule;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLogStoreModule;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceWriteLockModule;

import java.util.List;

/**
 * A workspace of one pane, on the bench: {@code /workspace} - the workspace
 * self-contained, made first and mounted last; its widgets its headless
 * core's, any kind the bench knows but its nasty ones, one shown at a time;
 * the bench's tools over it, a simulator of each root party among them.
 */
public record WorkspaceBenchApp() implements AppModule<WorkspaceBenchApp.Params, WorkspaceBenchApp> {

    public static final WorkspaceBenchApp INSTANCE = new WorkspaceBenchApp();

    /** None: the workspace opens its first widgets itself. */
    public record Params() implements AppModule._Param {}

    record appMain() implements AppModule._AppMain<Params, WorkspaceBenchApp> {}

    public static final ParamCodec<Params> CODEC = ParamCodec.ofEmpty(Params::new);

    @Override public String title()      { return "Workspace bench"; }
    @Override public String simpleName() { return "workspace-bench"; }
    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public ParamCodec<Params> paramCodec() { return CODEC; }

    @Override
    public ImportsFor<WorkspaceBenchApp> imports() {
        return ImportsFor.<WorkspaceBenchApp>builder()
                .add(new ModuleImports<>(List.of(new BenchWidgetsModule.BENCH_MONITORS()), BenchWidgetsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new BenchMonitorsModule.BenchMonitors()), BenchMonitorsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new PartySimulatorModule.PartySimulator()), PartySimulatorModule.INSTANCE))
                // the workspace, and what it is: its manifest, from its declaration
                .add(new ModuleImports<>(List.of(new SinglePaneWorkspaceModule.SinglePaneWorkspace()), SinglePaneWorkspaceModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new BenchWorkspaceModule.BENCH_WORKSPACE()), BenchWorkspaceModule.INSTANCE))
                // its log: the store, where it is kept, whose it is, one writer at a time, and the load
                .add(new ModuleImports<>(List.of(new WorkspaceLogStoreModule.WorkspaceLogStore()), WorkspaceLogStoreModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new IndexedDbLogModule.IndexedDbLog()), IndexedDbLogModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceLogIdentityModule.WorkspaceLogIdentity()), WorkspaceLogIdentityModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceWriteLockModule.WorkspaceWriteLock()), WorkspaceWriteLockModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LogKeyModule.LogKey()), LogKeyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceLoadModule.WorkspaceLoad()), WorkspaceLoadModule.INSTANCE))
                // the page's parties, where the workspace is mounted
                .add(new ModuleImports<>(List.of(new domOpsParty()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new focusParty()), FocusPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WidgetBenchStyles.wb_page()), WidgetBenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceBenchApp> exports() { return new ExportsOf<>(INSTANCE, List.of(new appMain())); }
}
