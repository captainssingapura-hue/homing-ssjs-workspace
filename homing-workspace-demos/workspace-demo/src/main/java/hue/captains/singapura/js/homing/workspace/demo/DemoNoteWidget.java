package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The note, as a kind the workspace can offer: its title and its params for
 * the picker, and a construct that builds the widget and hands it back. The
 * widget is {@link DemoWidgetsModule.NoteWidget}; this only declares it.
 */
public final class DemoNoteWidget extends WorkspaceWidget<WorkspaceWidget._None, DemoNoteWidget> {

    public static final DemoNoteWidget INSTANCE = new DemoNoteWidget();

    private DemoNoteWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, DemoNoteWidget> {}

    @Override protected _Construct<_None, DemoNoteWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Note"; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(List.of(new DemoWidgetsModule.NoteWidget()), DemoWidgetsModule.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of("    return new NoteWidget(branch, params, host);");
    }
}
