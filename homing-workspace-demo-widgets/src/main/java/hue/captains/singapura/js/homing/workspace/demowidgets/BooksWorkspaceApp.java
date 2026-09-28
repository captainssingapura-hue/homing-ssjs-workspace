package hue.captains.singapura.js.homing.workspace.demowidgets;

import hue.captains.singapura.js.homing.core.AppLink;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.workspace.shell.WorkspacePageModule;

import java.util.List;

/**
 * The books workspace as a page of any standard MPA: the shell's workspace page
 * handed this set's manifest. Its params are a workspace page's - which
 * workspace of the kind, and whether the server keeps its states.
 */
public record BooksWorkspaceApp() implements AppModule<WorkspacePageModule.Params, BooksWorkspaceApp> {

    public static final BooksWorkspaceApp INSTANCE = new BooksWorkspaceApp();

    /** A page's way to a books workspace: {@code nav.BooksWorkspaceApp({ ws_id, ws_server })} - its own log bar's, for a new one. */
    public record link() implements AppLink<BooksWorkspaceApp> {}

    record appMain() implements AppModule._AppMain<WorkspacePageModule.Params, BooksWorkspaceApp> {}

    @Override public String title()      { return "Books"; }
    @Override public String simpleName() { return "books-workspace"; }
    @Override public Class<WorkspacePageModule.Params> paramsType() { return WorkspacePageModule.Params.class; }
    @Override public ParamCodec<WorkspacePageModule.Params> paramCodec() { return WorkspacePageModule.CODEC; }

    @Override
    public ImportsFor<BooksWorkspaceApp> imports() {
        return ImportsFor.<BooksWorkspaceApp>builder()
                .add(new ModuleImports<>(List.of(new WorkspacePageModule.WorkspacePage()), WorkspacePageModule.INSTANCE))
                // its own address, for the log bar's new workspace of the kind
                .add(new ModuleImports<>(List.of(new link()), INSTANCE))
                .add(new ModuleImports<>(List.of(new BooksWorkspaceModule.BOOKS_WORKSPACE()), BooksWorkspaceModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<BooksWorkspaceApp> exports() { return new ExportsOf<>(INSTANCE, List.of(new appMain())); }
}
