package hue.captains.singapura.js.homing.workspace.monitors;

import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParties;
import hue.captains.singapura.js.homing.core.js.domOpsParty;
import hue.captains.singapura.js.homing.ui.focus.FocusStyles;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;

import java.util.List;

/** The page's DomOps party as a tree, a monitor widget: {@code new DomOpsTree(container, params)}, read from the party's snapshot. */
public record DomOpsTreeModule() implements DomModule<DomOpsTreeModule> {

    public record DomOpsTree() implements SelfContainedWidget<DomOpsTreeModule>, NeedKeyboard {
        @Override public String summary() { return "The page's DomOps party as a tree, a widget of its own: every branch at its level, a graft marked, the strays named."; }
        @Override public List<KeyBinding> keys() { return MonitorModule.KEYS; }
    }

    public static final DomOpsTreeModule INSTANCE = new DomOpsTreeModule();

    @Override
    public ImportsFor<DomOpsTreeModule> imports() {
        return ImportsFor.<DomOpsTreeModule>builder()
                .add(new ModuleImports<>(List.of(new MonitorModule.Monitor()), MonitorModule.INSTANCE))
                // the page's party, read; the rows as the focus tree's, so the two trees read alike
                .add(new ModuleImports<>(List.of(new domOpsParty(), new domOpsParties()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new FocusStyles.fm_tree(), new FocusStyles.fm_row(), new FocusStyles.fm_kind(),
                        new FocusStyles.fm_name(), new FocusStyles.fm_component(), new FocusStyles.fm_outside()), FocusStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<DomOpsTreeModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new DomOpsTree())); }
}
