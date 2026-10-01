package hue.captains.singapura.js.homing.workspace.stage;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.elements.Elements;

import java.util.List;

/** What a widget offers itself to the stage by: a button that joins the stage party when pressed, and asks for its widget by name. */
public record StageButtonModule() implements DomModule<StageButtonModule> {

    public static final StageButtonModule INSTANCE = new StageButtonModule();

    /** A branch component: {@code new StageButton(branch, { party, widget })}; root, shown, dispose. */
    public record StageButton() implements BranchComponent<StageButtonModule> {
        @Override public String summary() { return "A widget's offer of itself to the stage: Focus to show it, Back while it is shown."; }
    }

    @Override
    public ImportsFor<StageButtonModule> imports() {
        return ImportsFor.<StageButtonModule>builder()
                .add(new ModuleImports<>(List.of(new Elements.ButtonBuilder()), Elements.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<StageButtonModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new StageButton())); }
}
