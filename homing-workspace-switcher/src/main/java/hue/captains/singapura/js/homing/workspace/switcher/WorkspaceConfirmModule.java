package hue.captains.singapura.js.homing.workspace.switcher;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.dialog.DialogModule;

import java.util.List;

/**
 * A question put to a person before an act on a workspace:
 * {@code new WorkspaceConfirm(branch, opts)} - the system dialog, modal, with
 * Cancel and the act; {@code answered} true only when the act was pressed.
 */
public record WorkspaceConfirmModule() implements DomModule<WorkspaceConfirmModule> {

    public record WorkspaceConfirm() implements BranchComponent<WorkspaceConfirmModule> {
        @Override public String summary() { return "A question before an act on a workspace, in the system dialog: Cancel, or the act."; }
    }

    public static final WorkspaceConfirmModule INSTANCE = new WorkspaceConfirmModule();

    @Override
    public ImportsFor<WorkspaceConfirmModule> imports() {
        return ImportsFor.<WorkspaceConfirmModule>builder()
                .add(new ModuleImports<>(List.of(new DialogModule.Dialog()), DialogModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new SwitcherStyles.sw_question()), SwitcherStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceConfirmModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceConfirm())); }
}
