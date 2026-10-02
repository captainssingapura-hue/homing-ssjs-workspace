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
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceAnchorModule;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceChoiceModule;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceChoiceSecretaryModule;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceDirectoryModule;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceKeeperModule;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceOpenerModule;
import hue.captains.singapura.js.homing.workspace.log.js.LogIdsModule;
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
 * A grouped site's page of one group: {@code GroupedWorkspacePage.main(el, params, workspaces, groups)} -
 * the shell's page of the kind the anchor names, {@code #ws/<section>/<kind>}, from the
 * site's manifests; which workspace of it, the anchor's query at its end,
 * {@code ?ws_name=} or {@code ?ws_id=}; the page's directory provided from the site's
 * groups, and the page opening what its workspace choice party says is asked to open,
 * at its address (RFC 0058).
 */
public record GroupedWorkspacePageModule() implements DomModule<GroupedWorkspacePageModule> {

    public record GroupedWorkspacePage() implements Exportable._Class<GroupedWorkspacePageModule> {}

    public static final GroupedWorkspacePageModule INSTANCE = new GroupedWorkspacePageModule();

    /** A group's id, as it is a segment of the site's addresses ({@code GroupId}). */
    static final Pattern GROUP = Pattern.compile("[A-Za-z0-9_-]+");

    /** Where a site places its groups: "" for its root, else an address of such segments, {@code /workspaces}. */
    static final Pattern UNDER = Pattern.compile("(/[A-Za-z0-9_-]+)*");

    /**
     * A grouped workspace page's params: which group - the route's - whether the server keeps
     * its states, and where the site places its groups - {@code ws_under}, "" for its root, else
     * an address such as {@code /workspaces}: a group's page is {@code <ws_under>/<group>}, the
     * site's other groups beside it, which is where the page sends a choice of another. Nothing of
     * what is inside the group: the kind, and which workspace of it, are the anchor's, which the
     * server never sees.
     */
    public record Params(String ws_group, boolean ws_server, String ws_under) implements AppModule._Param {
        public Params {
            Objects.requireNonNull(ws_group, "Params.ws_group");
            if (!GROUP.matcher(ws_group).matches()) throw new IllegalArgumentException("Params.ws_group '" + ws_group + "': letters, digits, hyphen, underscore");
            if (ws_under == null) ws_under = "";
            if (!UNDER.matcher(ws_under).matches()) throw new IllegalArgumentException("Params.ws_under '" + ws_under + "': \"\", or /segments, no trailing slash");
        }

        /** A group's page at the site's root. */
        public Params(String ws_group, boolean ws_server) { this(ws_group, ws_server, ""); }
    }

    /** The group, required and of a group's letters; the server's word, only "on"; where the groups are, when not the root; nothing else read. */
    public static final ParamCodec<Params> CODEC = new ParamCodec<>() {

        @Override public Decoded<Params> from(Map<String, List<String>> query) {
            String group = QueryString.first(query, "ws_group");
            if (group == null || group.isBlank()) return Decoded.missing("ws_group");
            if (!GROUP.matcher(group).matches()) return Decoded.malformed("ws_group", group, "a group's id: letters, digits, hyphen, underscore");
            String server = QueryString.first(query, "ws_server");
            if (server != null && !server.equals("on")) return Decoded.malformed("ws_server", server, "on, or absent");
            String under = QueryString.first(query, "ws_under");
            if (under != null && !UNDER.matcher(under).matches()) return Decoded.malformed("ws_under", under, "where the groups are: /segments, no trailing slash");
            return Decoded.ok(new Params(group, server != null, under == null ? "" : under));
        }

        @Override public Map<String, List<String>> to(Params params) {
            var q = QueryString.params();
            QueryString.put(q, "ws_group", params.ws_group());
            if (params.ws_server()) QueryString.put(q, "ws_server", "on");
            if (!params.ws_under().isEmpty()) QueryString.put(q, "ws_under", params.ws_under());
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
                // where a kind is in its group, as the anchor names it; a kind, as the catalogue is asked by it
                .add(new ModuleImports<>(List.of(new WorkspaceAnchorModule.WorkspaceAnchor()), WorkspaceAnchorModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LogIdsModule.WorkspaceKind()), LogIdsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceOpenerModule.WorkspaceOpener()), WorkspaceOpenerModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceKeeperModule.WorkspaceKeeper()), WorkspaceKeeperModule.INSTANCE))
                // what the page does to its workspaces, as its opener and keepers ask
                .add(new ModuleImports<>(List.of(new WorkspaceKeepingModule.WorkspaceKeeping()), WorkspaceKeepingModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceChoiceModule.WORKSPACE_CHOICE()), WorkspaceChoiceModule.INSTANCE))
                // a kind's own workspace, as its log names it
                .add(new ModuleImports<>(List.of(new WorkspaceLogIdentityModule.WorkspaceLogIdentity()), WorkspaceLogIdentityModule.INSTANCE))
                // the catalogue this browser keeps, which the keeping makes, renames and deletes by
                .add(new ModuleImports<>(List.of(new WorkspaceCatalogueModule.WorkspaceCatalogue()), WorkspaceCatalogueModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new IndexedDbLogModule.IndexedDbLog()), IndexedDbLogModule.INSTANCE))
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
