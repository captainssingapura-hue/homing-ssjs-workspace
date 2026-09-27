package hue.captains.singapura.js.homing.workspace.monitors;

import hue.captains.singapura.js.homing.component.keyboard.FocusPartyModule;
import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.KeysModule;
import hue.captains.singapura.js.homing.component.keyboard.focusParties;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParties;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetStyles;

import java.util.List;

/**
 * What every monitor widget is before what it watches: {@code class X extends
 * Monitor}. A widget that stands on its own - its DomOps party and focus
 * party its own, offered as roots for its host to graft - whose root fills
 * its container and whose box scrolls, by the keys while it holds them.
 */
public record MonitorModule() implements DomModule<MonitorModule> {

    /** The base class; not a widget by itself - each monitor is. */
    public record Monitor() implements Exportable._Class<MonitorModule> {}

    /** The keys every monitor takes while it holds them: its box scrolled, and the keys given back. */
    public static final List<KeyBinding> KEYS = List.of(
            KeyBinding.of(Key.ARROW_DOWN, "a line down"),
            KeyBinding.of(Key.ARROW_UP, "a line up"),
            KeyBinding.of(Key.PAGE_DOWN, "a page down"),
            KeyBinding.of(Key.PAGE_UP, "a page up"),
            KeyBinding.of(Key.HOME, "to the top"),
            KeyBinding.of(Key.END, "to the end"),
            KeyBinding.of(Key.ESCAPE, "the keys given back"));

    public static final MonitorModule INSTANCE = new MonitorModule();

    @Override
    public ImportsFor<MonitorModule> imports() {
        return ImportsFor.<MonitorModule>builder()
                // its own DomOps party and focus party, each from its party of parties
                .add(new ModuleImports<>(List.of(new domOpsParties()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new focusParties()), FocusPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeysModule.Keys()), KeysModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WidgetStyles.wg_fill(), new WidgetStyles.wg_scroll()), WidgetStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new MonitorStyles.mn_pane()), MonitorStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<MonitorModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new Monitor())); }
}
