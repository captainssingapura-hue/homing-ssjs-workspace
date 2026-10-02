package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.component.keyboard.FocusPartyModule;
import hue.captains.singapura.js.homing.component.keyboard.focusParties;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParties;
import hue.captains.singapura.js.homing.workspace.content.ContentSecretaryModule;
import hue.captains.singapura.js.homing.workspace.content.ContentStyles;
import hue.captains.singapura.js.homing.workspace.parties.MessagingPartyModule;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;

import java.util.List;

/**
 * A scope on the content bench: {@code new ContentPanel(container, opts)} - a composed widget
 * with content parties of its own, linked under the ones it is given, its parts made in them.
 */
public record ContentPanelModule() implements DomModule<ContentPanelModule> {

    public static final ContentPanelModule INSTANCE = new ContentPanelModule();

    public record ContentPanel() implements SelfContainedWidget<ContentPanelModule> {
        @Override public String summary() {
            return "A scope of the content bench's: content parties of its own, linked under the ones it is given, and its parts made in them.";
        }
    }

    @Override
    public ImportsFor<ContentPanelModule> imports() {
        return ImportsFor.<ContentPanelModule>builder()
                .add(new ModuleImports<>(List.of(new domOpsParties()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new focusParties()), FocusPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new MessagingPartyModule.MessagingParty()), MessagingPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ContentSecretaryModule.ContentSecretary()), ContentSecretaryModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ContentStyles.fl_part(), new ContentStyles.fl_part_fill()), ContentStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new ContentBenchStyles.cb_panel(), new ContentBenchStyles.cb_panel_title(), new ContentBenchStyles.cb_group(),
                        new ContentBenchStyles.cb_key()), ContentBenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ContentPanelModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new ContentPanel())); }
}
