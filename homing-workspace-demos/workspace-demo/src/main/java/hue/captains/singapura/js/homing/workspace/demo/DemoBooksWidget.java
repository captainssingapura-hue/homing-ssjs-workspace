package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The books, as a relation grid, as a kind the workspace can offer. The widget is
 * {@link DemoViewsModule.BooksWidget}; this only declares it.
 */
public final class DemoBooksWidget extends WorkspaceWidget<WorkspaceWidget._None, DemoBooksWidget> {

    public static final DemoBooksWidget INSTANCE = new DemoBooksWidget();

    private DemoBooksWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, DemoBooksWidget> {}

    @Override protected _Construct<_None, DemoBooksWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Books"; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(List.of(new DemoViewsModule.BooksWidget()), DemoViewsModule.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of("    return new BooksWidget(branch, params);");
    }
}
