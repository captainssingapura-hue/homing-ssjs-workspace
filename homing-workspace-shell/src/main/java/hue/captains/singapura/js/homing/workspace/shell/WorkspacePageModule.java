package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.core.QueryString;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParty;
import hue.captains.singapura.js.homing.site.mpa.MpaStyles;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceInstanceId;
import hue.captains.singapura.js.homing.workspace.log.js.LogKeyModule;
import hue.captains.singapura.js.homing.workspace.log.store.IndexedDbLogModule;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceCatalogueModule;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceCheckpointerModule;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLoadModule;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLogIdentityModule;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLogStoreModule;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceWriteLockModule;
import hue.captains.singapura.js.homing.workspace.shell.server.WorkspaceServer;

import java.util.List;
import java.util.Map;

/**
 * A split-grid workspace as a page of a standard MPA, for any workspace
 * declared in Java: {@code WorkspacePage.main(el, params, manifest)}. The shell
 * knows no widget - a widget set's own app hands its manifest in, generated
 * from its {@code WorkspaceDeclaration}, and takes {@link Params} for its
 * params: the page's log is kept under the workspace's name as its kind.
 */
public record WorkspacePageModule() implements DomModule<WorkspacePageModule> {

    public record WorkspacePage() implements Exportable._Class<WorkspacePageModule> {}

    public static final WorkspacePageModule INSTANCE = new WorkspacePageModule();

    /**
     * A workspace page's params, for a widget set's app to take as its own:
     * which workspace of the kind - a {@link WorkspaceInstanceId}'s value, a
     * lowercase UUID, or null for the kind's own - and whether the server keeps
     * its states too, when the page posts each checkpoint to {@link
     * WorkspaceServer#CHECKPOINTS} on the server that served it.
     */
    public record Params(String ws_id, boolean ws_server) implements AppModule._Param {
        public Params(boolean ws_server) { this(null, ws_server); }
    }

    /** A workspace's id as the log writes one, and the server's word: each checked where the address is read. */
    public static final ParamCodec<Params> CODEC = new ParamCodec<>() {

        @Override public Decoded<Params> from(Map<String, List<String>> query) {
            String id = QueryString.first(query, "ws_id");
            if (id != null && !isWorkspaceId(id)) return Decoded.malformed("ws_id", id, "a workspace's id (a lowercase UUID), or absent for the kind's own");
            String server = QueryString.first(query, "ws_server");
            if (server != null && !server.equals("on")) return Decoded.malformed("ws_server", server, "on, or absent");
            return Decoded.ok(new Params(id, server != null));
        }

        /** A UUID as the log writes one - lowercase, hyphenated - so that one workspace has one address. */
        private static boolean isWorkspaceId(String id) {
            try { return WorkspaceInstanceId.parse(id).toString().equals(id); }
            catch (IllegalArgumentException e) { return false; }
        }

        @Override public Map<String, List<String>> to(Params params) {
            var q = QueryString.params();
            if (params.ws_id() != null) QueryString.put(q, "ws_id", params.ws_id());
            if (params.ws_server()) QueryString.put(q, "ws_server", "on");
            return q;
        }
    };

    @Override
    public ImportsFor<WorkspacePageModule> imports() {
        return ImportsFor.<WorkspacePageModule>builder()
                .add(new ModuleImports<>(List.of(new domOpsParty()), DomOpsPartyModule.INSTANCE))
                // the slot is a reading column until an app says otherwise; a shell of panes says otherwise
                .add(new ModuleImports<>(List.of(new MpaStyles.mpa_main_full()), MpaStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new GridWorkspaceModule.GridWorkspace()), GridWorkspaceModule.INSTANCE))
                // its log: the store, where it is kept, whose it is, one writer at a time, the load, the checkpoints, the list
                .add(new ModuleImports<>(List.of(new WorkspaceLogStoreModule.WorkspaceLogStore()), WorkspaceLogStoreModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceLogIdentityModule.WorkspaceLogIdentity()), WorkspaceLogIdentityModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new IndexedDbLogModule.IndexedDbLog()), IndexedDbLogModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceLoadModule.WorkspaceLoad()), WorkspaceLoadModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceCheckpointerModule.WorkspaceCheckpointer()), WorkspaceCheckpointerModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceWriteLockModule.WorkspaceWriteLock()), WorkspaceWriteLockModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceCatalogueModule.WorkspaceCatalogue()), WorkspaceCatalogueModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LogKeyModule.LogKey()), LogKeyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceAddressesModule.WORKSPACE_ADDRESSES()), WorkspaceAddressesModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspacePageModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspacePage())); }
}
