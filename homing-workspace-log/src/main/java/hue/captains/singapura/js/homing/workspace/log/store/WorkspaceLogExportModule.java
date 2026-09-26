package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.codecs.WorkspaceLogCodecsModule;

import java.util.List;

/** A workspace log as a file: the header and a line per event, as the Java side writes the same log. */
public record WorkspaceLogExportModule() implements DomModule<WorkspaceLogExportModule> {

    public record WorkspaceLogExport() implements Exportable._Class<WorkspaceLogExportModule> {}

    public static final WorkspaceLogExportModule INSTANCE = new WorkspaceLogExportModule();

    @Override
    public ImportsFor<WorkspaceLogExportModule> imports() {
        return ImportsFor.<WorkspaceLogExportModule>builder()
                .add(new ModuleImports<>(List.of(new WorkspaceLogCodecsModule.LogHeaderCodec(), new WorkspaceLogCodecsModule.LoggedEventCodec()), WorkspaceLogCodecsModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceLogExportModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceLogExport())); }
}
