package hue.captains.singapura.js.homing.workspace.content;

import hue.captains.singapura.js.homing.component.keyboard.FocusPartyModule;
import hue.captains.singapura.js.homing.component.keyboard.focusParties;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParties;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;

import java.util.List;

/**
 * The flow widget: {@code new Flow(container, params)} - a column of widgets, its parts asked
 * of the flow party with the params it was made with, each made from its type and params.
 */
public record FlowModule() implements DomModule<FlowModule> {

    public static final FlowModule INSTANCE = new FlowModule();

    public record Flow() implements SelfContainedWidget<FlowModule> {
        @Override public String summary() {
            return "A column of widgets: its parts asked of the flow party with its params, each made from its type and params, in order.";
        }
    }

    @Override
    public ImportsFor<FlowModule> imports() {
        return ImportsFor.<FlowModule>builder()
                .add(new ModuleImports<>(List.of(new domOpsParties()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new focusParties()), FocusPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new FlowContentModule.FLOW()), FlowContentModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ContentParamsModule.ContentParams()), ContentParamsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ContentStyles.fl_column(), new ContentStyles.fl_part(), new ContentStyles.fl_part_fill(),
                        new ContentStyles.fl_note(), new ContentStyles.fl_hidden()), ContentStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<FlowModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new Flow())); }
}
