package hue.captains.singapura.js.homing.workspace.layers;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/** The one pane, headless, in JavaScript: the dual of {@link hue.captains.singapura.js.homing.workspace.layers.PanePlacement}. No DOM. */
public record PanePlacementModule() implements EsModule<PanePlacementModule> {

    public record PanePlacement() implements Exportable._Class<PanePlacementModule> {}

    public static final PanePlacementModule INSTANCE = new PanePlacementModule();

    @Override public ImportsFor<PanePlacementModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<PanePlacementModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new PanePlacement())); }
}
