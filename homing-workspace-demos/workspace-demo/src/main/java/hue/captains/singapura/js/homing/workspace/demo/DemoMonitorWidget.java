package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The Focus lab's Focus monitor, as a kind the workspace can offer. The widget is
 * {@link DemoFocusLabModule.MonitorWidget}; this only declares it.
 */
public final class DemoMonitorWidget extends WorkspaceWidget<WorkspaceWidget._None, DemoMonitorWidget> {

    public static final DemoMonitorWidget INSTANCE = new DemoMonitorWidget();

    private DemoMonitorWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, DemoMonitorWidget> {}

    @Override protected _Construct<_None, DemoMonitorWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Focus monitor"; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(List.of(new DemoFocusLabModule.MonitorWidget()), DemoFocusLabModule.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of("    return new MonitorWidget(branch, params, host);");
    }
}
