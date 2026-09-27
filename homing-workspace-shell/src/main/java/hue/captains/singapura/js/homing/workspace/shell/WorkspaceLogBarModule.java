package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.server.HrefManager;
import hue.captains.singapura.js.homing.workspace.log.js.WriteLockModule;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLogExportModule;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLogIdentityModule;

import java.util.List;

/**
 * The workspace log's line along the foot of the floor: how many events it has
 * recorded this visit, and Export, which saves the log as a workspace log file
 * — handed over by a link the href manager sets, never a raw one.
 */
public record WorkspaceLogBarModule() implements DomModule<WorkspaceLogBarModule> {

    public record WorkspaceLogBar() implements Exportable._Class<WorkspaceLogBarModule> {}

    public static final WorkspaceLogBarModule INSTANCE = new WorkspaceLogBarModule();

    @Override
    public ImportsFor<WorkspaceLogBarModule> imports() {
        return ImportsFor.<WorkspaceLogBarModule>builder()
                .add(new ModuleImports<>(List.of(new WorkspaceLogExportModule.WorkspaceLogExport()), WorkspaceLogExportModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WriteLockModule.Held()), WriteLockModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceLogIdentityModule.WorkspaceLogIdentity()), WorkspaceLogIdentityModule.INSTANCE))
                // a new workspace of the kind, while another page writes this one
                .add(new ModuleImports<>(List.of(new WorkspaceApp.link()), WorkspaceApp.INSTANCE))
                .add(new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()), HrefManager.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceStyles.ws_logbar(), new WorkspaceStyles.ws_logbar_count(),
                                                 new WorkspaceStyles.ws_logbar_link()), WorkspaceStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceLogBarModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceLogBar())); }
}
