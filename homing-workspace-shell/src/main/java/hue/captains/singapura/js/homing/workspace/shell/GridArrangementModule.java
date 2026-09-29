package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * A split grid's arrangement, laid out on a workspace that has nothing yet:
 * {@code GridArrangement.apply(ws, arrangement)} - the frame parted and
 * re-shared, the widgets opened region by region - through the grid and the
 * core, so the log keeps it. The arrangement is {@link GridArrangementJs}'s.
 */
public record GridArrangementModule() implements EsModule<GridArrangementModule> {

    public record GridArrangement() implements Exportable._Class<GridArrangementModule> {}

    public static final GridArrangementModule INSTANCE = new GridArrangementModule();

    @Override public ImportsFor<GridArrangementModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<GridArrangementModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new GridArrangement())); }
}
