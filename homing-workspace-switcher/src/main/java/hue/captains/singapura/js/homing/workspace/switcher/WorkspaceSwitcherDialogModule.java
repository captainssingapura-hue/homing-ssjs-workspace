package hue.captains.singapura.js.homing.workspace.switcher;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.component.keyboard.FocusPartyModule;
import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.component.keyboard.focusParty;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.server.HrefManager;
import hue.captains.singapura.js.homing.ui.dialog.DialogModule;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceChoiceModule;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetStyles;

import java.util.List;

/**
 * The workspace switcher, summoned: {@code new WorkspaceSwitcherDialog(branch, opts)} -
 * the composed switcher in the system dialog, modal, with Cancel, Open in new tab
 * and Open; a page's, never a workspace's, and kept by no log.
 */
public record WorkspaceSwitcherDialogModule() implements DomModule<WorkspaceSwitcherDialogModule> {

    public record WorkspaceSwitcherDialog() implements BranchComponent<WorkspaceSwitcherDialogModule>, NeedKeyboard {
        @Override public String summary() { return "The workspace switcher in the system dialog: summoned by a page, gone when closed, kept by no log."; }
        @Override public List<KeyBinding> keys() {
            return List.of(KeyBinding.of(Key.ESCAPE, "the dialog closed, wherever the hand is in the switcher"));
        }
    }

    public static final WorkspaceSwitcherDialogModule INSTANCE = new WorkspaceSwitcherDialogModule();

    @Override
    public ImportsFor<WorkspaceSwitcherDialogModule> imports() {
        return ImportsFor.<WorkspaceSwitcherDialogModule>builder()
                // the system dialog it is, and the switcher in it
                .add(new ModuleImports<>(List.of(new DialogModule.Dialog()), DialogModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceSwitcherModule.WorkspaceSwitcher()), WorkspaceSwitcherModule.INSTANCE))
                // the page's focus party, where the switcher's is grafted for the steward to reach
                .add(new ModuleImports<>(List.of(new focusParty()), FocusPartyModule.INSTANCE))
                // the type the switcher is given its party by
                .add(new ModuleImports<>(List.of(new WorkspaceChoiceModule.WORKSPACE_CHOICE()), WorkspaceChoiceModule.INSTANCE))
                // Open in new tab: the one way a page opens another
                .add(new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()), HrefManager.INSTANCE))
                .add(new ModuleImports<>(List.of(new WidgetStyles.wg_slot()), WidgetStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceSwitcherDialogModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceSwitcherDialog())); }
}
