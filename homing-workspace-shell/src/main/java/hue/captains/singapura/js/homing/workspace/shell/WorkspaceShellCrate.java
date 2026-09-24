package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.site.mpa.MpaCrate;
import hue.captains.singapura.js.homing.ui.dialog.UiDialogCrate;
import hue.captains.singapura.js.homing.studio.base.StudioBaseCrate;
import hue.captains.singapura.js.homing.theme.color.ThemeColorCrate;
import hue.captains.singapura.js.homing.theme.type.ThemeTypeCrate;
import hue.captains.singapura.js.homing.workspace.WorkspaceCrate;
import hue.captains.singapura.js.homing.workspace.codecs.WorkspaceCodecsCrate;
import hue.captains.singapura.js.homing.workspace.persistence.WorkspacePersistenceCrate;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code homing-workspace-shell}: the universal
 * workspace-chrome substrate (orchestrator + the layout / party / codec /
 * checkpoint / replay / mounter sub-modules and the GenericWorkspace app).
 * Requires the workspace stack below it plus the core-js + studio-base substrate.
 */
public final class WorkspaceShellCrate implements Crate {

    public static final WorkspaceShellCrate INSTANCE = new WorkspaceShellCrate();

    private WorkspaceShellCrate() {}

    @Override public String name() { return "homing-workspace-shell"; }

    @Override public List<Crate> requires() {
        return List.of(
                CoreJsCrate.INSTANCE,
                ServerCrate.INSTANCE,
                StudioBaseCrate.INSTANCE,
                // The framework page model WorkspaceApp is a page of, for the one
                // word it wears on the slot it is handed.
                MpaCrate.INSTANCE,
                // The Dialog the switcher opens. MpaCrate happens to require it
                // too; an edge this crate depends on is this crate's to declare.
                UiDialogCrate.INSTANCE,
                WorkspaceCrate.INSTANCE,
                WorkspaceCodecsCrate.INSTANCE,
                WorkspacePersistenceCrate.INSTANCE,
                // RFC 0066 - the palettes its groups read: colour, and the mono face.
                ThemeColorCrate.INSTANCE,
                hue.captains.singapura.js.homing.design.DesignCrate.INSTANCE,
                ThemeTypeCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(CheckpointServiceModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(CodecRegistrarModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(EventEmitterModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(GenericWorkspace.INSTANCE),
                // The workspace as a page of any standard MPA: the app, and the
                // registry stamped into a module it can import.
                CrateEntry.of(WorkspaceSpecsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(WorkspaceApp.INSTANCE, StandardJsModuleType.CONSUMER),
                // RFC 0058 — the authentic-path app: one page per WorkspaceGroup, the kind an anchor.
                CrateEntry.of(WorkspaceGroupApp.INSTANCE),
                CrateEntry.of(WorkspaceGroupChrome.INSTANCE),
                CrateEntry.of(GenericWorkspaceChrome.INSTANCE),
                CrateEntry.of(LayoutCodecModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(PartyBootstrapModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(PersistenceAttacherModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                // The per-tab focus primitive the coordinator drives, brought over
                // from studio-base where nothing else ever called it.
                CrateEntry.of(FocusManagerModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(WorkspaceFocusCoordinatorModule.INSTANCE),
                CrateEntry.of(WorkspaceShallowKeyboardModule.INSTANCE),
                CrateEntry.of(WorkspaceKeyboardScopeModule.INSTANCE),
                CrateEntry.of(PickerTabFlowModule.INSTANCE),
                CrateEntry.of(ReplayEngineModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(TabRegistryModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(WidgetMounterModule.INSTANCE),
                // RFC 0057 Phase 3 — the switcher replaces WorkspaceControlModal.
                CrateEntry.of(WorkspaceSwitcherStyles.INSTANCE),
                CrateEntry.of(WorkspaceSwitcherModel.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(WorkspaceGroupPathModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),   // RFC 0058
                CrateEntry.of(WorkspaceSwitcherModule.INSTANCE),
                // RFC 0063 — the DomOpsParty monitor: sheet, renderer, widget.
                CrateEntry.of(PartyMonitorStyles.INSTANCE),
                CrateEntry.of(PartyMonitorAdapterModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(PartyMonitorRendererModule.INSTANCE),
                CrateEntry.of(DomOpsPartyMonitorWidget.INSTANCE),
                CrateEntry.of(CssGraphStyles.INSTANCE),
                CrateEntry.of(CssGraphRendererModule.INSTANCE),
                CrateEntry.of(CssGraphWorkbenchWidget.INSTANCE),
                CrateEntry.of(WorkspaceDirectoryModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(WorkspaceShellChromeModule.INSTANCE),
                CrateEntry.of(WorkspaceStateModelModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(WriteLockGuardModule.INSTANCE));
    }
}
