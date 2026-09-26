package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The Focus lab's Form, as a kind the workspace can offer. The widget is
 * {@link DemoFocusLabModule.FormWidget}; this only declares it.
 */
public final class DemoFormWidget extends WorkspaceWidget<WorkspaceWidget._None, DemoFormWidget> {

    public static final DemoFormWidget INSTANCE = new DemoFormWidget();

    private DemoFormWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, DemoFormWidget> {}

    @Override protected _Construct<_None, DemoFormWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Form"; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(List.of(new DemoFocusLabModule.FormWidget()), DemoFocusLabModule.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of("    return new FormWidget(branch, params, host);");
    }
}
