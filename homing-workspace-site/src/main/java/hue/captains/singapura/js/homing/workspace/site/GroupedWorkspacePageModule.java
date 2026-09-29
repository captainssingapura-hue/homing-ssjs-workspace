package hue.captains.singapura.js.homing.workspace.site;

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
import hue.captains.singapura.js.homing.server.HrefManager;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceChoiceModule;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceChoiceSecretaryModule;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceDirectoryModule;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceOpenerModule;
import hue.captains.singapura.js.homing.workspace.log.js.LogIdsModule;
import hue.captains.singapura.js.homing.workspace.log.js.LogKeyModule;
import hue.captains.singapura.js.homing.workspace.log.store.IndexedDbLogModule;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceCatalogueModule;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLogIdentityModule;
import hue.captains.singapura.js.homing.workspace.parties.MessagingPartyModule;
import hue.captains.singapura.js.homing.workspace.shell.WorkspacePageModule;
import hue.captains.singapura.js.homing.workspace.switcher.WorkspaceSwitcherDialogModule;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A grouped site's workspace page: {@code GroupedWorkspacePage.main(el, params, workspaces, groups)} -
 * the shell's page, the workspace chosen by its kind from the site's manifests, the
 * page's directory provided from the site's groups, and the page opening what its
 * workspace choice party says is asked to open, at the place the groups file it.
 */
public record GroupedWorkspacePageModule() implements DomModule<GroupedWorkspacePageModule> {

    public record GroupedWorkspacePage() implements Exportable._Class<GroupedWorkspacePageModule> {}

    public static final GroupedWorkspacePageModule INSTANCE = new GroupedWorkspacePageModule();

    /** A kind of workspace, as its log names it. */
    static final Pattern KIND = Pattern.compile("[A-Za-z0-9_-]+");

    /**
     * A grouped workspace page's params: which kind of workspace - the route's -
     * and then a workspace page's own: which of that kind, and whether the server
     * keeps its states ({@link WorkspacePageModule.Params}).
     */
    public record Params(String ws_kind, String ws_id, boolean ws_server) implements AppModule._Param {
        public Params {
            Objects.requireNonNull(ws_kind, "Params.ws_kind");
            if (!KIND.matcher(ws_kind).matches()) throw new IllegalArgumentException("Params.ws_kind '" + ws_kind + "': letters, digits, hyphen, underscore");
        }
    }

    /** The kind, required and of a kind's letters; the rest read as a workspace page's. */
    public static final ParamCodec<Params> CODEC = new ParamCodec<>() {

        @Override public Decoded<Params> from(Map<String, List<String>> query) {
            String kind = QueryString.first(query, "ws_kind");
            if (kind == null || kind.isBlank()) return Decoded.missing("ws_kind");
            if (!KIND.matcher(kind).matches()) return Decoded.malformed("ws_kind", kind, "a kind of workspace: letters, digits, hyphen, underscore");
            return switch (WorkspacePageModule.CODEC.from(query)) {
                case Decoded.Ok<WorkspacePageModule.Params>(WorkspacePageModule.Params p) -> Decoded.ok(new Params(kind, p.ws_id(), p.ws_server()));
                case Decoded.Missing<WorkspacePageModule.Params> m -> Decoded.missing(m.key());
                case Decoded.Malformed<WorkspacePageModule.Params> m -> Decoded.malformed(m.key(), m.value(), m.expected());
            };
        }

        @Override public Map<String, List<String>> to(Params params) {
            var q = QueryString.params();
            QueryString.put(q, "ws_kind", params.ws_kind());
            WorkspacePageModule.CODEC.to(new WorkspacePageModule.Params(params.ws_id(), params.ws_server()))
                    .forEach((k, vs) -> vs.forEach(v -> QueryString.put(q, k, v)));
            return q;
        }
    };

    @Override
    public ImportsFor<GroupedWorkspacePageModule> imports() {
        return ImportsFor.<GroupedWorkspacePageModule>builder()
                // the page a workspace is
                .add(new ModuleImports<>(List.of(new WorkspacePageModule.WorkspacePage()), WorkspacePageModule.INSTANCE))
                // the page's directory, and its member of the workspace choice party: the opener
                .add(new ModuleImports<>(List.of(new WorkspaceDirectoryModule.WorkspaceDirectory()), WorkspaceDirectoryModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceOpenerModule.WorkspaceOpener()), WorkspaceOpenerModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceChoiceModule.WORKSPACE_CHOICE()), WorkspaceChoiceModule.INSTANCE))
                // a kind's own workspace, as its log names it
                .add(new ModuleImports<>(List.of(new WorkspaceLogIdentityModule.WorkspaceLogIdentity()), WorkspaceLogIdentityModule.INSTANCE))
                // a new workspace made: listed in the catalogue this browser keeps, under a fresh id, by its log's key
                .add(new ModuleImports<>(List.of(new WorkspaceCatalogueModule.WorkspaceCatalogue()), WorkspaceCatalogueModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new IndexedDbLogModule.IndexedDbLog()), IndexedDbLogModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LogKeyModule.LogKey()), LogKeyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LogIdsModule.WorkspaceKind()), LogIdsModule.INSTANCE))
                // THE PAGE'S OWN CHOICE: its workspace choice party, with the root secretary, and the switcher it summons
                .add(new ModuleImports<>(List.of(new MessagingPartyModule.MessagingParty()), MessagingPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceChoiceSecretaryModule.WorkspaceChoiceSecretary()), WorkspaceChoiceSecretaryModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceSwitcherDialogModule.WorkspaceSwitcherDialog()), WorkspaceSwitcherDialogModule.INSTANCE))
                // the page's DomOps party, where the dialogs are made
                .add(new ModuleImports<>(List.of(new domOpsParty()), DomOpsPartyModule.INSTANCE))
                // the one way a page goes somewhere
                .add(new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()), HrefManager.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<GroupedWorkspacePageModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new GroupedWorkspacePage())); }
}
