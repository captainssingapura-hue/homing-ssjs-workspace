package hue.captains.singapura.js.homing.workspace.stage;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.ui.dialog.UiDialogCrate;
import hue.captains.singapura.js.homing.ui.elements.UiElementsCrate;
import hue.captains.singapura.js.homing.ui.look.UiLookCrate;

import java.util.List;

/**
 * The stage, a set of its own: the party's type and its secretary; the steward that moves a
 * widget lent by its placement; the layer the stage is, and the button a widget offers itself by.
 * Nothing here knows a doc, a widget, or where one sits.
 */
public final class WorkspaceStageCrate implements Crate {

    public static final WorkspaceStageCrate INSTANCE = new WorkspaceStageCrate();

    private WorkspaceStageCrate() {}

    @Override public String name() { return "homing-workspace-stage"; }

    @Override public List<Crate> requires() {
        return List.of(
                // the css manager
                ServerCrate.INSTANCE,
                // the elements' buttons
                UiLookCrate.INSTANCE,
                UiElementsCrate.INSTANCE,
                // the dialog's modality: everything else inert while the stage shows
                UiDialogCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                new CrateEntry(StagePartyModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(StageSecretaryModule.INSTANCE, StandardJsModuleType.SECRETARY),
                new CrateEntry(StageStyles.INSTANCE),
                new CrateEntry(StageLayerModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                new CrateEntry(StageStewardModule.INSTANCE, StandardJsModuleType.CONSUMER),
                new CrateEntry(StageButtonModule.INSTANCE, StandardJsModuleType.PRIMITIVE));
    }
}
