package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.docking.DeskModule;
import hue.captains.singapura.js.homing.workspace.widgets.HostedWidgetModule;

import java.util.List;

/**
 * The bench's monitors, afloat: {@code new BenchMonitors(branch, { host,
 * monitors })} - a bar of square toggles, one per monitor, and a desk whose
 * floats lie over the page, each monitor a tab-pane in a float of its own,
 * hosted as a self-contained widget with its parties grafted where the tab
 * puts it.
 */
public record BenchMonitorsModule() implements DomModule<BenchMonitorsModule> {

    public record BenchMonitors() implements BranchComponent<BenchMonitorsModule> {
        @Override public String summary() { return "A bar of toggles that float the monitors over the page, each a hosted widget in a tab-pane of its own."; }
    }

    public static final BenchMonitorsModule INSTANCE = new BenchMonitorsModule();

    @Override
    public ImportsFor<BenchMonitorsModule> imports() {
        return ImportsFor.<BenchMonitorsModule>builder()
                .add(new ModuleImports<>(List.of(new DeskModule.Desk()), DeskModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new HostedWidgetModule.HostedWidget()), HostedWidgetModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WidgetBenchStyles.wb_bar(), new WidgetBenchStyles.wb_toggle(), new WidgetBenchStyles.wb_toggle_on(), new WidgetBenchStyles.wb_bar_gap()), WidgetBenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<BenchMonitorsModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new BenchMonitors())); }
}
