package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The same books as shelves, in a relation tree, as a kind the workspace can offer. The widget is
 * {@link DemoViewsModule.ShelvesWidget}; this only declares it.
 */
public final class DemoShelvesWidget extends WorkspaceWidget<WorkspaceWidget._None, DemoShelvesWidget> {

    public static final DemoShelvesWidget INSTANCE = new DemoShelvesWidget();

    private DemoShelvesWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, DemoShelvesWidget> {}

    @Override protected _Construct<_None, DemoShelvesWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Shelves"; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(List.of(new DemoViewsModule.ShelvesWidget()), DemoViewsModule.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of("    return new ShelvesWidget(branch, params, host);");
    }
}
