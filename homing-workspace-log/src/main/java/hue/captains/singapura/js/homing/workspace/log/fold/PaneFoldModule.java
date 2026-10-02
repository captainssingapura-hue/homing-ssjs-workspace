package hue.captains.singapura.js.homing.workspace.log.fold;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.log.js.PaneEventModule;
import hue.captains.singapura.js.homing.workspace.log.js.PaneStateModule;

import java.util.List;

/** The one pane's layer of the fold: a pane event on the PaneState, as Java's PaneFold folds it. */
public record PaneFoldModule() implements DomModule<PaneFoldModule> {

    public record PaneFold() implements Exportable._Class<PaneFoldModule> {}

    public static final PaneFoldModule INSTANCE = new PaneFoldModule();

    @Override
    public ImportsFor<PaneFoldModule> imports() {
        return ImportsFor.<PaneFoldModule>builder()
                .add(new ModuleImports<>(List.of(new PaneEventModule.PaneShown()), PaneEventModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new PaneStateModule.PaneState()), PaneStateModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PaneFoldModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new PaneFold())); }
}
