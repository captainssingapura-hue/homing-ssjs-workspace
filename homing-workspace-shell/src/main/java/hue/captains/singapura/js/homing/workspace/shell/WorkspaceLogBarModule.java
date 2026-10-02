package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.server.HrefManager;
import hue.captains.singapura.js.homing.ui.elements.EdgeStripModule;
import hue.captains.singapura.js.homing.ui.elements.Elements;
import hue.captains.singapura.js.homing.ui.icons.IconModule;
import hue.captains.singapura.js.homing.workspace.log.js.WriteLockModule;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLogExportModule;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLogIdentityModule;

import java.util.List;

/**
 * The workspace's control strip: an edge strip at the foot of the floor, laid
 * over the grid when the hand comes to its lip, and kept out while it says
 * something a person must know. On it, the workspace log's line - how many
 * events it has recorded, and Export, which saves the log as a workspace log
 * file, handed over by a link the href manager sets, never a raw one - and the
 * workspace's own controls, each a designed button.
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
                .add(new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()), HrefManager.INSTANCE))
                .add(new ModuleImports<>(List.of(new EdgeStripModule.EdgeStripBuilder()), EdgeStripModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new Elements.ButtonBuilder()), Elements.INSTANCE))
                .add(new ModuleImports<>(List.of(new IconModule.Icon()), IconModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceStyles.ws_logbar_name(), new WorkspaceStyles.ws_logbar_count(), new WorkspaceStyles.ws_logbar_note(),
                                                 new WorkspaceStyles.ws_logbar_fresh(), new WorkspaceStyles.ws_logbar_off(), new WorkspaceStyles.ws_logbar_link()), WorkspaceStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceLogBarModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceLogBar())); }
}
