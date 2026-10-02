package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.log.js.LoggedEventModule;
import hue.captains.singapura.js.homing.workspace.log.js.LogHeaderModule;
import hue.captains.singapura.js.homing.workspace.log.js.FoldedStateModule;
import hue.captains.singapura.js.homing.workspace.log.fold.WorkspaceFoldModule;

import java.util.List;

/** A workspace log as a file - the header and a line per event - and its meaning as another, the state the browser folds it to: as the Java side writes both. */
public record WorkspaceLogExportModule() implements DomModule<WorkspaceLogExportModule> {

    public record WorkspaceLogExport() implements Exportable._Class<WorkspaceLogExportModule> {}

    public static final WorkspaceLogExportModule INSTANCE = new WorkspaceLogExportModule();

    @Override
    public ImportsFor<WorkspaceLogExportModule> imports() {
        return ImportsFor.<WorkspaceLogExportModule>builder()
                .add(new ModuleImports<>(List.of(new LoggedEventModule.LoggedEventCodec()), LoggedEventModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LogHeaderModule.LogHeaderCodec()), LogHeaderModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new FoldedStateModule.FoldedStateCodec()), FoldedStateModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceFoldModule.WorkspaceFold()), WorkspaceFoldModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceLogExportModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceLogExport())); }
}
