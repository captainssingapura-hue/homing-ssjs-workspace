package hue.captains.singapura.js.homing.workspace.stage;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.dialog.ModalityModule;
import hue.captains.singapura.js.homing.ui.elements.Elements;

import java.util.List;

/**
 * The page's stage, as it looks: one modal layer over everything - a scrim, a frame filling the
 * view, its head with the title and the close, its seat a box the stage sizes. Escape closes it.
 */
public record StageLayerModule() implements DomModule<StageLayerModule> {

    public static final StageLayerModule INSTANCE = new StageLayerModule();

    /** A branch component: {@code new StageLayer(branch, { onClose })}; show, hide, showing, dispose. */
    public record StageLayer() implements BranchComponent<StageLayerModule>, NeedKeyboard {
        @Override public String summary() { return "The page's stage: one modal layer, a frame filling the view, its seat sized for what sits in it."; }

        /** Its one key, on its own layer while it shows. */
        public static final List<KeyBinding> KEYS = List.of(KeyBinding.of(Key.ESCAPE, "close the stage: what is on it goes back"));
        @Override public List<KeyBinding> keys() { return KEYS; }
    }

    @Override
    public ImportsFor<StageLayerModule> imports() {
        return ImportsFor.<StageLayerModule>builder()
                .add(new ModuleImports<>(List.of(new ModalityModule.Modality()), ModalityModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new Elements.ButtonBuilder()), Elements.INSTANCE))
                .add(new ModuleImports<>(List.of(new StageStyles.sg_layer(), new StageStyles.sg_hidden(), new StageStyles.sg_scrim(),
                        new StageStyles.sg_frame(), new StageStyles.sg_head(), new StageStyles.sg_title(), new StageStyles.sg_seat()), StageStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<StageLayerModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new StageLayer())); }
}
