package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.KeysModule;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The PANE half of a tab-pane: the room a widget runs in.
 *
 * <p>A tab-pane is two parts. The TAB is the dock's — its chip, its title, its
 * place in the strip. The PANE is this: a member of the focus party that wraps
 * whatever element the widget gives back. The widget runs inside and touches
 * neither; it is handed a branch and gives back {@code { root }}.</p>
 *
 * <p>So the dock's law — a tab's widget must be a focus member with a root and
 * {@code activate()} — is kept by the ROOM, never by the widget. A widget with
 * no keys, or only native ones, needs nothing; one that wants keys offers
 * {@code keyDown} and {@code activate} and the room hands them on.</p>
 *
 * <p>The room has a branch of its own under the holder's, not the dock's, so it
 * travels with its tab: a dock letting a tab go dissolves only its own branch,
 * and the room — root, membership and widget — goes where the tab goes. The
 * widget's branch is made under the room's: dock, room, widget, one nesting.</p>
 */
public record WidgetPaneModule() implements DomModule<WidgetPaneModule> {

    public static final WidgetPaneModule INSTANCE = new WidgetPaneModule();

    /** The room. */
    public record WidgetPane() implements BranchComponent<WidgetPaneModule>, NeedKeyboard {
        @Override public String summary() {
            return "The pane half of a tab-pane: the focus party's member for a tab, wrapping whatever element its widget gives, and handing the widget the keys it holds.";
        }
        /**
         * The room's own keys. The rest it holds it hands on to its widget
         * (keyDown), and a widget of native controls takes its own; the room
         * says only how the keys come back out: Escape out of a native control
         * the control did not take, and Escape again back to the dock.
         */
        @Override public List<KeyBinding> keys() {
            return List.of(KeyBinding.of(Key.ESCAPE,
                    "out of a native control inside, the keys the room's again; again, back to the dock"));
        }
    }

    @Override
    public ImportsFor<WidgetPaneModule> imports() {
        return ImportsFor.<WidgetPaneModule>builder()
                .add(new ModuleImports<>(List.of(new KeysModule.Keys()), KeysModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspacePanesStyles.wp_host()), WorkspacePanesStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WidgetPaneModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new WidgetPane()));
    }
}
