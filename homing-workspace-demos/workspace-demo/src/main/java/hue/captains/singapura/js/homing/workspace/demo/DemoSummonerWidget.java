package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The Focus lab's Summoner, as a kind the workspace can offer. The widget is
 * {@link DemoFocusLabModule.SummonerWidget}; this only declares it.
 */
public final class DemoSummonerWidget extends WorkspaceWidget<WorkspaceWidget._None, DemoSummonerWidget> {

    public static final DemoSummonerWidget INSTANCE = new DemoSummonerWidget();

    private DemoSummonerWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, DemoSummonerWidget> {}

    @Override protected _Construct<_None, DemoSummonerWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Summoner"; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(List.of(new DemoFocusLabModule.SummonerWidget()), DemoFocusLabModule.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of("    return new SummonerWidget(branch, params, host);");
    }
}
