package hue.captains.singapura.js.homing.workspace.core;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.parties.MessagingPartyModule;

import java.util.List;

/**
 * The workspace's messaging parties, headless, beside its core: a root instance
 * of each type its opened widgets declare, each widget joined when opened and
 * left when closed. Its dual in Java comes with the dual of the parties' runtime.
 */
public record WorkspacePartiesModule() implements EsModule<WorkspacePartiesModule> {

    public record WorkspaceParties() implements Exportable._Class<WorkspacePartiesModule> {}

    public static final WorkspacePartiesModule INSTANCE = new WorkspacePartiesModule();

    @Override
    public ImportsFor<WorkspacePartiesModule> imports() {
        return ImportsFor.<WorkspacePartiesModule>builder()
                .add(new ModuleImports<>(List.of(new MessagingPartyModule.MessagingParty()), MessagingPartyModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspacePartiesModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceParties())); }
}
