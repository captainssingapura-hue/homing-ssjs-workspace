package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The counter, as a kind the workspace can offer. The widget is
 * {@link DemoWidgetsModule.CounterWidget}: native buttons, and arrows the room
 * hands on when its tab holds the keys.
 */
public final class DemoCounterWidget extends WorkspaceWidget<WorkspaceWidget._None, DemoCounterWidget> {

    public static final DemoCounterWidget INSTANCE = new DemoCounterWidget();

    private DemoCounterWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, DemoCounterWidget> {}

    @Override protected _Construct<_None, DemoCounterWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Counter"; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(List.of(new DemoWidgetsModule.CounterWidget()), DemoWidgetsModule.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of("    return new CounterWidget(branch, params, host);");
    }
}
