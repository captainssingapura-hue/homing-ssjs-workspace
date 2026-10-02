package hue.captains.singapura.js.homing.workspace.log.fold;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/** A share while the layout algebra works on it: an exact fraction, brought back to whole millionths by one rule, as Java's ExactShare. */
public record ExactShareModule() implements DomModule<ExactShareModule> {

    public record ExactShare() implements Exportable._Class<ExactShareModule> {}

    public static final ExactShareModule INSTANCE = new ExactShareModule();

    @Override
    public ImportsFor<ExactShareModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<ExactShareModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new ExactShare())); }
}
