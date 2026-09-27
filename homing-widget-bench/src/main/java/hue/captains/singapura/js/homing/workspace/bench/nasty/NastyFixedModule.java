package hue.captains.singapura.js.homing.workspace.bench.nasty;

import hue.captains.singapura.js.homing.component.keyboard.FocusPartyModule;
import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.KeysModule;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.component.keyboard.focusParty;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParty;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;

import java.util.List;

/**
 * A widget that misbehaves on purpose: {@code new NastyFixed(container, params)},
 * fixed at 640 x 360 whatever container it is lent, and deaf to the container's
 * changes. Self-contained in every other way. It is the bench's, for the bench
 * to catch - never a widget to stand anywhere else.
 */
public record NastyFixedModule() implements DomModule<NastyFixedModule> {

    public record NastyFixed() implements SelfContainedWidget<NastyFixedModule>, NeedKeyboard {
        @Override public String summary() { return "A widget fixed at 640 x 360 that ignores its container: the bench's to catch."; }
        @Override public List<KeyBinding> keys() { return List.of(KeyBinding.of(Key.ESCAPE, "the keys given back")); }
    }

    public static final NastyFixedModule INSTANCE = new NastyFixedModule();

    @Override
    public ImportsFor<NastyFixedModule> imports() {
        return ImportsFor.<NastyFixedModule>builder()
                .add(new ModuleImports<>(List.of(new domOpsParty()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new focusParty()), FocusPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeysModule.Keys()), KeysModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new NastyStyles.nasty_fixed()), NastyStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<NastyFixedModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new NastyFixed())); }
}
