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
import hue.captains.singapura.js.homing.relgrid.RelGridModule;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceChoiceModule;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceDirectoryModule;
import hue.captains.singapura.js.homing.workspace.log.js.LogIdsModule;
import hue.captains.singapura.js.homing.workspace.log.store.IndexedDbLogModule;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceCatalogueModule;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetStyles;

import java.util.List;

/**
 * The workspaces of the chosen kind, as a table: {@code new WorkspaceInstances(container, params)} -
 * each one's name, when last opened and when made, as the catalogue this browser
 * keeps has them; Enter on one the asking for it to open.
 */
public record WorkspaceInstancesModule() implements DomModule<WorkspaceInstancesModule> {

    public record WorkspaceInstances() implements SelfContainedWidget<WorkspaceInstancesModule>, NeedKeyboard {
        @Override public String summary() { return "The workspaces of the chosen kind this browser keeps, as a table: Enter, or a double press, the asking to open one."; }
        @Override public List<KeyBinding> keys() {
            return List.of(KeyBinding.of(Key.ENTER, "the workspace at the cursor asked to open - on the new row, its name taken"),
                           KeyBinding.of(Key.F2, "the workspace at the cursor, its name taken, to be called otherwise"),
                           KeyBinding.of(Key.DELETE, "the workspace at the cursor asked to be deleted - softly"),
                           KeyBinding.of(Key.ARROW_LEFT, "past the grid's left edge: its host told, to hand the keys on"),
                           KeyBinding.of(Key.ESCAPE, "the keys given back, when the grid did not take it"));
        }
    }

    public static final WorkspaceInstancesModule INSTANCE = new WorkspaceInstancesModule();

    @Override
    public ImportsFor<WorkspaceInstancesModule> imports() {
        return ImportsFor.<WorkspaceInstancesModule>builder()
                .add(new ModuleImports<>(List.of(new domOpsParties()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new focusParties()), FocusPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeysModule.Keys()), KeysModule.INSTANCE))
                // the relation grid, its stock cell, and what tells it the rows changed
                .add(new ModuleImports<>(List.of(new RelGridModule.RelGrid()), RelGridModule.INSTANCE))
                // its relation: the rows, their cells, the new row
                .add(new ModuleImports<>(List.of(new WorkspaceRowsModule.WorkspaceRows()), WorkspaceRowsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridProtocolModule.RelGridViewChanged()), RelGridProtocolModule.INSTANCE))
                // what it shows: the catalogue this browser keeps, read by kind; and the kind's title, from the page's directory
                .add(new ModuleImports<>(List.of(new WorkspaceCatalogueModule.WorkspaceCatalogue()), WorkspaceCatalogueModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new IndexedDbLogModule.IndexedDbLog()), IndexedDbLogModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LogIdsModule.WorkspaceKind()), LogIdsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceDirectoryModule.WorkspaceDirectory()), WorkspaceDirectoryModule.INSTANCE))
                // the type it joins, by which it is given a party: it shows the chosen kind
                .add(new ModuleImports<>(List.of(new WorkspaceChoiceModule.WORKSPACE_CHOICE()), WorkspaceChoiceModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WidgetStyles.wg_fill(), new WidgetStyles.wg_scroll()), WidgetStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new SwitcherStyles.sw_column(), new SwitcherStyles.sw_head(), new SwitcherStyles.sw_title(),
                        new SwitcherStyles.sw_count(), new SwitcherStyles.sw_note(), new SwitcherStyles.sw_hidden()), SwitcherStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceInstancesModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceInstances())); }
}
