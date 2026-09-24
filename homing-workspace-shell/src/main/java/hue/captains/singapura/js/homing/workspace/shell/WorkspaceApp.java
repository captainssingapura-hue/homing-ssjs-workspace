package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.core.QueryString;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParty;
import hue.captains.singapura.js.homing.site.mpa.MpaStyles;

import java.util.List;
import java.util.Map;

/**
 * The workspace as a page of any standard MPA: {@code /app?app=workspace&ws_kind=<kind>},
 * or wherever a site's router puts it.
 *
 * <p>The same workspace {@link GenericWorkspace} is, hosted differently. That
 * one extends the studio's {@code WorkspaceMPA}, so a page of it is a studio
 * page: the studio draws the chrome, the studio's widget machinery writes the
 * body, and a site that is not a studio cannot have one. This one extends
 * nothing. It is an {@code AppModule} like any other — the MPA hands it the
 * slot and its params, it fills the slot — so the workspace is a page anywhere
 * the framework's page model reaches, and a standalone workspace application
 * is a site with one route.</p>
 *
 * <p>Both are live on purpose. The studio keeps its own mounting while its
 * copy of this stack is retired; nothing here points back at it.</p>
 *
 * <p>{@code ws_kind} is required and selects a registered {@link WorkspaceSpec}
 * from {@link WorkspaceSpecRegistry}, as it always did — the URL contract is
 * the same one, so a catalogue entry or a link that named a kind still names
 * it. Adding a workspace is still registering a spec: no new app, no new
 * widget, no new route.</p>
 */
public record WorkspaceApp() implements AppModule<WorkspaceApp.Params, WorkspaceApp> {

    public static final WorkspaceApp INSTANCE = new WorkspaceApp();

    /** The kind to mount: a registered {@link WorkspaceSpec}'s {@code kind()}. */
    public record Params(String ws_kind) implements AppModule._Param {}

    record appMain() implements AppModule._AppMain<Params, WorkspaceApp> {}

    /**
     * RFC 0051 — the kind is required. This app mounts a registered spec by
     * kind and there is no sensible default to fall back to, so an absent kind
     * is a malformed request rather than a page. Same reading as
     * {@link GenericWorkspace#CODEC}, and deliberately not shared with it: the
     * two apps are separate URL contracts that happen to agree today.
     */
    public static final ParamCodec<Params> CODEC = new ParamCodec<>() {

        @Override public Decoded<Params> from(Map<String, List<String>> query) {
            String kind = QueryString.first(query, "ws_kind");
            if (kind == null || kind.isBlank()) return Decoded.missing("ws_kind");
            return Decoded.ok(new Params(kind));
        }

        @Override public Map<String, List<String>> to(Params params) {
            return QueryString.of("ws_kind", params.ws_kind());
        }
    };

    @Override public String title()      { return "Workspace"; }
    @Override public String simpleName() { return "workspace"; }
    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public ParamCodec<Params> paramCodec() { return CODEC; }

    @Override
    public ImportsFor<WorkspaceApp> imports() {
        return ImportsFor.<WorkspaceApp>builder()
                .add(new ModuleImports<>(List.of(new domOpsParty()), DomOpsPartyModule.INSTANCE))
                // The slot is a reading column until an app says otherwise; a
                // shell of panes says otherwise.
                .add(new ModuleImports<>(List.of(new MpaStyles.mpa_main_full()), MpaStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceSpecsModule.SPECS()), WorkspaceSpecsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceShellChromeModule.mountWorkspaceShell()),
                        WorkspaceShellChromeModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceApp> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new appMain()));
    }
}
