package hue.captains.singapura.js.homing.workspace.widgets;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.component.keyboard.FocusPartyModule;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;

import java.util.List;

/**
 * A self-contained widget where a component would stand - a tab-pane's widget:
 * {@code new HostedWidget(branch, tab, Kind, params)}. The host's side of the
 * graft: the widget lent a container minted on the branch, its DomOps party
 * grafted there, its focus party grafted under a holder that joins the tab's
 * focus branch, so a tab that travels takes the graft with it. Made with no
 * class, {@code new HostedWidget(branch, tab)}, it is a holder lent empty: the
 * widget is made in its container by whoever owns its life - a workspace's core -
 * and handed in after, {@code hosted.hold(widget)}, and left to its maker.
 */
public record HostedWidgetModule() implements DomModule<HostedWidgetModule> {

    public record HostedWidget() implements BranchComponent<HostedWidgetModule> {
        @Override public String summary() { return "A self-contained widget hosted where a component would stand: its container lent, its parties grafted at the host's place."; }
    }

    public static final HostedWidgetModule INSTANCE = new HostedWidgetModule();

    @Override
    public ImportsFor<HostedWidgetModule> imports() {
        return ImportsFor.<HostedWidgetModule>builder()
                // what a widget offers its host to graft
                .add(new ModuleImports<>(List.of(new DomOpsPartyModule.MobileDomOpsParty()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new FocusPartyModule.MobileFocusParty()), FocusPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WidgetStyles.wg_slot()), WidgetStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<HostedWidgetModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new HostedWidget())); }
}
