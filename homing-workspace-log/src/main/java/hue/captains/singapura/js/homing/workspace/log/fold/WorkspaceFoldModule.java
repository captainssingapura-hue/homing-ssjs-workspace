package hue.captains.singapura.js.homing.workspace.log.fold;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.log.js.FloatEventModule;
import hue.captains.singapura.js.homing.workspace.log.js.FoldedStateModule;
import hue.captains.singapura.js.homing.workspace.log.js.LogIdsModule;
import hue.captains.singapura.js.homing.workspace.log.js.PaneEventModule;
import hue.captains.singapura.js.homing.workspace.log.js.RegionEventModule;
import hue.captains.singapura.js.homing.workspace.log.js.RosterEventModule;
import hue.captains.singapura.js.homing.workspace.log.js.TabEventModule;
import hue.captains.singapura.js.homing.workspace.log.js.WorkspaceStateModule;

import java.util.List;

/** A workspace log's meaning: its events handed, by family, to their layers' folds, into the WorkspaceState they leave, as Java's WorkspaceFold folds them. */
public record WorkspaceFoldModule() implements DomModule<WorkspaceFoldModule> {

    public record WorkspaceFold() implements Exportable._Class<WorkspaceFoldModule> {}

    public static final WorkspaceFoldModule INSTANCE = new WorkspaceFoldModule();

    @Override
    public ImportsFor<WorkspaceFoldModule> imports() {
        return ImportsFor.<WorkspaceFoldModule>builder()
                .add(new ModuleImports<>(List.of(new RosterFoldModule.RosterFold()), RosterFoldModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new PaneFoldModule.PaneFold()), PaneFoldModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new GridFoldModule.GridFold()), GridFoldModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LogIdsModule.EventSeq()), LogIdsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RosterEventModule.RosterEvent()), RosterEventModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new PaneEventModule.PaneEvent()), PaneEventModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new TabEventModule.TabEvent()), TabEventModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RegionEventModule.RegionEvent()), RegionEventModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new FloatEventModule.FloatEvent()), FloatEventModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceStateModule.WorkspaceState()), WorkspaceStateModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new FoldedStateModule.FoldedState()), FoldedStateModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceFoldModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceFold())); }
}
