package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.widgets.HostedWidgetModule;

import java.util.List;

/**
 * A split-grid workspace's register of panes, the core's port over its desk's
 * register: a widget's pane is a tab-pane opened under the widget's id, in no
 * host, holding a {@code HostedWidget} lent empty - the container the core
 * makes the widget in. A close asked for on the tab is said, for the page to
 * ask the core. A DOM module, though it touches no DOM: it imports one.
 */
public record WidgetTabsModule() implements DomModule<WidgetTabsModule> {

    public record WidgetTabs() implements Exportable._Class<WidgetTabsModule> {}

    public static final WidgetTabsModule INSTANCE = new WidgetTabsModule();

    @Override
    public ImportsFor<WidgetTabsModule> imports() {
        return ImportsFor.<WidgetTabsModule>builder()
                .add(new ModuleImports<>(List.of(new HostedWidgetModule.HostedWidget()), HostedWidgetModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WidgetTabsModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WidgetTabs())); }
}
