package hue.captains.singapura.js.homing.workspace.switcher;

import hue.captains.singapura.js.homing.component.keyboard.FocusPartyModule;
import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.KeysModule;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.component.keyboard.focusParties;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParties;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;
import hue.captains.singapura.js.homing.reltree.RelTreeModule;
import hue.captains.singapura.js.homing.reltree.RelTreeStockCellsModule;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceChoiceModule;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceDirectoryModule;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetStyles;

import java.util.List;

/**
 * The kinds of workspace, as a tree: {@code new WorkspaceKinds(container, params)} -
 * the page's directory, its sections and the kinds filed in them; its cursor the
 * choice, Enter on a kind the asking for it to open.
 */
public record WorkspaceKindsModule() implements DomModule<WorkspaceKindsModule> {

    public record WorkspaceKinds() implements SelfContainedWidget<WorkspaceKindsModule>, NeedKeyboard {
        @Override public String summary() { return "The kinds of workspace the page's directory files, as a tree: its cursor the choice, Enter the asking to open."; }
        @Override public List<KeyBinding> keys() {
            return List.of(KeyBinding.of(Key.ARROW_RIGHT, "past a kind: its host told, to hand the keys on"),
                           KeyBinding.of(Key.ESCAPE, "the keys given back, when the tree did not take it"));
        }
    }

    public static final WorkspaceKindsModule INSTANCE = new WorkspaceKindsModule();

    @Override
    public ImportsFor<WorkspaceKindsModule> imports() {
        return ImportsFor.<WorkspaceKindsModule>builder()
                // its own DomOps party and focus party, each from its party of parties
                .add(new ModuleImports<>(List.of(new domOpsParties()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new focusParties()), FocusPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeysModule.Keys()), KeysModule.INSTANCE))
                // the relation tree, its stock cell, and its questions
                .add(new ModuleImports<>(List.of(new RelTreeModule.RelTree()), RelTreeModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelTreeStockCellsModule.RelTreeTextCell()), RelTreeStockCellsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridProtocolModule.RelTreeView(), new RelGridProtocolModule.RelTreeUnfold(),
                        new RelGridProtocolModule.RelTreeFold(), new RelGridProtocolModule.RelTreeViewChanged()), RelGridProtocolModule.INSTANCE))
                // what it shows: the page's directory
                .add(new ModuleImports<>(List.of(new WorkspaceDirectoryModule.WorkspaceDirectory()), WorkspaceDirectoryModule.INSTANCE))
                // the type it joins, by which it is given a party: its cursor is the choice
                .add(new ModuleImports<>(List.of(new WorkspaceChoiceModule.WORKSPACE_CHOICE()), WorkspaceChoiceModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WidgetStyles.wg_fill(), new WidgetStyles.wg_scroll()), WidgetStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new SwitcherStyles.sw_column(), new SwitcherStyles.sw_head(), new SwitcherStyles.sw_title(),
                        new SwitcherStyles.sw_count(), new SwitcherStyles.sw_note(), new SwitcherStyles.sw_hidden()), SwitcherStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceKindsModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceKinds())); }
}
