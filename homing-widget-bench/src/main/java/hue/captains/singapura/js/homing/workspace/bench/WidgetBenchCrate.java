package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceWidgetsCrate;

import java.util.List;

/** The bench: its page, the kinds it looks up, and the container it lends. */
public final class WidgetBenchCrate implements Crate {

    public static final WidgetBenchCrate INSTANCE = new WidgetBenchCrate();

    private WidgetBenchCrate() {}

    @Override public String name() { return "homing-widget-bench"; }

    @Override public List<Crate> requires() {
        return List.of(
                // the css manager the page's one class is added by
                ServerCrate.INSTANCE,
                // the widgets it stands up
                WorkspaceWidgetsCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(WidgetBenchApp.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(BenchWidgetsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(WidgetBenchStyles.INSTANCE));
    }
}
