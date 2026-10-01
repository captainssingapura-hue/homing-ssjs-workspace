package hue.captains.singapura.js.homing.workspace.stage;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The steward of a page's stage party: {@code StageSteward.over({ placement, branch })} - the class
 * the party hires - asks the placement to lend the widget named, seats it on the stage, and gives
 * it back after. It knows the placement, never where in it a widget sits.
 */
public record StageStewardModule() implements DomModule<StageStewardModule> {

    public static final StageStewardModule INSTANCE = new StageStewardModule();

    public record StageSteward() implements Exportable._Class<StageStewardModule> {}

    @Override
    public ImportsFor<StageStewardModule> imports() {
        return ImportsFor.<StageStewardModule>builder()
                .add(new ModuleImports<>(List.of(new StageLayerModule.StageLayer()), StageLayerModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<StageStewardModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new StageSteward())); }
}
