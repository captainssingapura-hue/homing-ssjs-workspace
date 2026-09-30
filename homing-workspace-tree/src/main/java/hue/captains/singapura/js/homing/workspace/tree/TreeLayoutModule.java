package hue.captains.singapura.js.homing.workspace.tree;

import hue.captains.singapura.js.homing.component.keyboard.FocusPartyModule;
import hue.captains.singapura.js.homing.component.keyboard.focusParties;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParties;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetStyles;

import java.util.List;

/**
 * The tree placement's engine: {@code new TreeLayout(container, opts)} - a tree's arrangement
 * laid out as a reading flow, each widget of a node's leaf made from its type and params.
 */
public record TreeLayoutModule() implements DomModule<TreeLayoutModule> {

    public static final TreeLayoutModule INSTANCE = new TreeLayoutModule();

    public record TreeLayout() implements SelfContainedWidget<TreeLayoutModule> {
        @Override public String summary() {
            return "A tree's arrangement laid out as a reading flow: each node a section under its heading, its leaf's widgets in order, then its children.";
        }
    }

    @Override
    public ImportsFor<TreeLayoutModule> imports() {
        return ImportsFor.<TreeLayoutModule>builder()
                .add(new ModuleImports<>(List.of(new domOpsParties()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new focusParties()), FocusPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WidgetStyles.wg_fill(), new WidgetStyles.wg_scroll()), WidgetStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new TreeStyles.tl_column(), new TreeStyles.tl_section(), new TreeStyles.tl_own(), new TreeStyles.tl_current(),
                        new TreeStyles.tl_body(), new TreeStyles.tl_nested(),
                        new TreeStyles.tl_hidden(), new TreeStyles.tl_heading(),
                        new TreeStyles.tl_subheading(), new TreeStyles.tl_code(), new TreeStyles.tl_leaf(), new TreeStyles.tl_leaf_fill(),
                        new TreeStyles.tl_missing()), TreeStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<TreeLayoutModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new TreeLayout())); }
}
