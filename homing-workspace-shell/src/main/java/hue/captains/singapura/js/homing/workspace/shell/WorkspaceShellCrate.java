package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.site.mpa.MpaCrate;
import hue.captains.singapura.js.homing.ui.dialog.UiDialogCrate;
import hue.captains.singapura.js.homing.ui.docking.UiDockingCrate;
import hue.captains.singapura.js.homing.ui.menu.UiMenuCrate;
import hue.captains.singapura.js.homing.ui.panes.UiPanesCrate;
import hue.captains.singapura.js.homing.ui.splitgrid.UiSplitGridCrate;
import hue.captains.singapura.js.homing.workspace.WorkspaceCrate;
import hue.captains.singapura.js.homing.workspace.codecs.WorkspaceCodecsCrate;
import hue.captains.singapura.js.homing.workspace.persistence.WorkspacePersistenceCrate;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code homing-workspace-shell}: the universal
 * workspace-chrome substrate (orchestrator + the layout / party / codec /
 * checkpoint / replay / mounter sub-modules), and {@link WorkspaceApp}, the
 * workspace as a page of any standard MPA. Requires the workspace stack below
 * it, the core-js and server substrate, the MPA and the ui-components it is
 * built of — and nothing of the studio.
 */
public final class WorkspaceShellCrate implements Crate {

    public static final WorkspaceShellCrate INSTANCE = new WorkspaceShellCrate();

    private WorkspaceShellCrate() {}

    @Override public String name() { return "homing-workspace-shell"; }

    @Override public List<Crate> requires() {
        return List.of(
                CoreJsCrate.INSTANCE,
                ServerCrate.INSTANCE,
                // The framework page model WorkspaceApp is a page of, for the one
                // word it wears on the slot it is handed.
                MpaCrate.INSTANCE,
                // The Dialog the switcher opens. MpaCrate happens to require it
                // too; an edge this crate depends on is this crate's to declare.
                UiDialogCrate.INSTANCE,
                // The three the workspace's panes are made of.
                UiSplitGridCrate.INSTANCE,
                UiPanesCrate.INSTANCE,
                UiDockingCrate.INSTANCE,
                UiMenuCrate.INSTANCE,
                WorkspaceCrate.INSTANCE,
                WorkspaceCodecsCrate.INSTANCE,
                // The workspace log: its store, where it keeps its rows, its export.
                hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLogCrate.INSTANCE,
                WorkspacePersistenceCrate.INSTANCE,
                // The design substrate its sheets wear words of. No legacy palette:
                // nothing here reads a --color-* or a legacy font token.
                hue.captains.singapura.js.homing.design.DesignCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(CheckpointServiceModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(CodecRegistrarModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(EventEmitterModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                // The workspace as a page of any standard MPA: the app, and the
                // registry stamped into a module it can import.
                CrateEntry.of(WorkspaceSpecsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(WorkspaceApp.INSTANCE, StandardJsModuleType.CONSUMER),
                // The workspace, built as the gallery's docking page is (RFC 0066 E3, the workspace
                // detour): a desk and its dock grid on a floor. What follows below it is the old
                // shell, unwired, kept until its headless parts are ported.
                CrateEntry.of(WorkspaceModule.INSTANCE, StandardJsModuleType.CONSUMER),
                // Its log: the recorder (headless) and the bar that exports it.
                CrateEntry.of(WorkspaceRecorderModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(WorkspaceProjectionModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(WorkspaceLogBarModule.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(WorkspaceStyles.INSTANCE),
                // Stand-ins for the widgets while the tabs are built.
                CrateEntry.of(FakeWidgetsModule.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(LayoutCodecModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(PartyBootstrapModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(PersistenceAttacherModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(PickerTabFlowModule.INSTANCE),
                CrateEntry.of(ReplayEngineModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(TabRegistryModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(WidgetMounterModule.INSTANCE),
                // RFC 0057 Phase 3 — the switcher replaces WorkspaceControlModal.
                CrateEntry.of(WorkspaceSwitcherStyles.INSTANCE),
                CrateEntry.of(WorkspaceSwitcherModel.INSTANCE, StandardJsModuleType.PURE_LOGIC),
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
                // The two event vocabularies, joined: what a component reports,
                // as what the workspace records.
                CrateEntry.of(WorkspaceEventsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                // What a right-click offers: the tab's, which is the pane's own,
                // and the room's, which only the workspace can know.
                CrateEntry.of(WorkspaceMenus.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                // The panes: the grid, a dock per cell, the desk over them.
                CrateEntry.of(WorkspacePanesStyles.INSTANCE),
                // The one layout in its two spellings, the panes' and the model's.
                CrateEntry.of(WorkspaceGridModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                // The pane half of a tab-pane: the room a widget runs in.
                CrateEntry.of(WidgetPaneModule.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(WorkspacePanesModule.INSTANCE, StandardJsModuleType.CONSUMER),
                // What the panes fill into their events, and a tab's name and icon: split out of them.
                CrateEntry.of(WorkspacePaneEventsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(WorkspaceTabNamesModule.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(WorkspaceStateModelModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(WriteLockGuardModule.INSTANCE));
    }
}
