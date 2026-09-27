package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.workspace.bench.nasty.NastyFixedModule;
import hue.captains.singapura.js.homing.workspace.bench.nasty.NastyStyles;
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
                // the DomOpsParty a nasty widget mints its root from, as every widget does
                CoreJsCrate.INSTANCE,
                // the widgets it stands up
                WorkspaceWidgetsCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(WidgetBenchApp.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(BenchWidgetsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(WidgetBenchStyles.INSTANCE),
                // the nasty widgets, which misbehave on purpose for the bench to catch
                CrateEntry.of(NastyFixedModule.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(NastyStyles.INSTANCE));
    }
}
