package hue.captains.singapura.js.homing.workspace.monitors;

import hue.captains.singapura.js.homing.core.AppLink;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.workspace.shell.WorkspacePageModule;

import java.util.List;

/**
 * The monitors' workspace as a page of any standard MPA: the shell's workspace
 * page handed this set's manifest. Its params are a workspace page's.
 */
public record MonitorsWorkspaceApp() implements AppModule<WorkspacePageModule.Params, MonitorsWorkspaceApp> {

    public static final MonitorsWorkspaceApp INSTANCE = new MonitorsWorkspaceApp();

    /** A page's way to a monitors workspace: {@code nav.MonitorsWorkspaceApp({ ws_id, ws_server })} - its own log bar's, for a new one. */
    public record link() implements AppLink<MonitorsWorkspaceApp> {}

    record appMain() implements AppModule._AppMain<WorkspacePageModule.Params, MonitorsWorkspaceApp> {}

    @Override public String title()      { return "Monitors"; }
    @Override public String simpleName() { return "monitors-workspace"; }
    @Override public Class<WorkspacePageModule.Params> paramsType() { return WorkspacePageModule.Params.class; }
    @Override public ParamCodec<WorkspacePageModule.Params> paramCodec() { return WorkspacePageModule.CODEC; }

    @Override
    public ImportsFor<MonitorsWorkspaceApp> imports() {
        return ImportsFor.<MonitorsWorkspaceApp>builder()
                .add(new ModuleImports<>(List.of(new WorkspacePageModule.WorkspacePage()), WorkspacePageModule.INSTANCE))
                // its own address, for the log bar's new workspace of the kind
                .add(new ModuleImports<>(List.of(new link()), INSTANCE))
                .add(new ModuleImports<>(List.of(new MonitorsWorkspaceModule.MONITORS_WORKSPACE()), MonitorsWorkspaceModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<MonitorsWorkspaceApp> exports() { return new ExportsOf<>(INSTANCE, List.of(new appMain())); }
}
