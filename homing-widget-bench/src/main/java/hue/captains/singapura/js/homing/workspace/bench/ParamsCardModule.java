package hue.captains.singapura.js.homing.workspace.bench;

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
 * A stand-in for the tree bench: {@code new ParamsCard(container, params)} - a card of its
 * params, all it knows; it flows, as tall as its content.
 */
public record ParamsCardModule() implements DomModule<ParamsCardModule> {

    public static final ParamsCardModule INSTANCE = new ParamsCardModule();

    public record ParamsCard() implements SelfContainedWidget<ParamsCardModule> {
        @Override public String summary() { return "A stand-in widget: a card of the params it was made with, and nothing else."; }
    }

    @Override
    public ImportsFor<ParamsCardModule> imports() {
        return ImportsFor.<ParamsCardModule>builder()
                .add(new ModuleImports<>(List.of(new domOpsParties()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new focusParties()), FocusPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new TreeBenchStyles.tb_card(), new TreeBenchStyles.tb_key(), new TreeBenchStyles.tb_value()),
                        TreeBenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ParamsCardModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new ParamsCard())); }
}
