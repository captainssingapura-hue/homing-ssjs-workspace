package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * A picture, zoomed by the keys the room hands on, as a kind the workspace can offer. The widget is
 * {@link DemoViewsModule.PictureWidget}; this only declares it.
 */
public final class DemoPictureWidget extends WorkspaceWidget<WorkspaceWidget._None, DemoPictureWidget> {

    public static final DemoPictureWidget INSTANCE = new DemoPictureWidget();

    private DemoPictureWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, DemoPictureWidget> {}

    @Override protected _Construct<_None, DemoPictureWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Picture"; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(List.of(new DemoViewsModule.PictureWidget()), DemoViewsModule.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of("    return new PictureWidget(branch, params);");
    }
}
