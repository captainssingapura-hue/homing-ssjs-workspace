package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.log.js.LogIdsModule;
import hue.captains.singapura.js.homing.workspace.log.js.LogKeyModule;
import hue.captains.singapura.js.homing.workspace.log.js.WorkspaceEntryModule;

import java.util.List;

/** The workspaces of each kind the browser keeps, listed as the WorkspaceEntry declared in Java. */
public record WorkspaceCatalogueModule() implements DomModule<WorkspaceCatalogueModule> {

    public record WorkspaceCatalogue() implements Exportable._Class<WorkspaceCatalogueModule> {}

    public static final WorkspaceCatalogueModule INSTANCE = new WorkspaceCatalogueModule();

    @Override
    public ImportsFor<WorkspaceCatalogueModule> imports() {
        return ImportsFor.<WorkspaceCatalogueModule>builder()
                .add(new ModuleImports<>(List.of(new LogIdsModule.WorkspaceKind(), new LogIdsModule.WorkspaceName()), LogIdsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LogKeyModule.LogKey()), LogKeyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceEntryModule.WorkspaceEntry(), new WorkspaceEntryModule.WorkspaceEntryCodec()), WorkspaceEntryModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceCatalogueModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceCatalogue())); }
}
