package hue.captains.singapura.js.homing.workspace.stage;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.parties.PartyType;

import java.util.List;

/**
 * The stage party's words: one stage a page, one widget on it at a time. A member asks for a
 * widget - by its name, the one its placement keeps it by - to be shown, and for what is shown to
 * be brought back; the steward does it and says so, or says why not; every member hears what the
 * steward says.
 */
public sealed interface StageParty {

    /** A member: show this widget on the stage. */
    record Present(String widget) implements StageParty {}

    /** A member: bring back what is on the stage. */
    record Dismiss() implements StageParty {}

    /** The steward: the widget is on the stage, under this title. */
    record Shown(String widget, String title) implements StageParty {}

    /** The steward: the widget is back where it was. */
    record Returned(String widget) implements StageParty {}

    /** The steward, or the secretary: the widget could not be shown, and why. */
    record Refused(String widget, String why) implements StageParty {}

    /**
     * The type: {@code stage}, served as {@code STAGE}, with its secretary. Its steward is the
     * host's to hire - {@code StageSteward.over({ placement, branch })} - since only the host knows
     * the placement that keeps its widgets.
     */
    PartyType<StageParty> TYPE = new PartyType<>("stage", StageParty.class)
            .servedFrom(new ModuleImports<>(List.of(new StagePartyModule.STAGE()), StagePartyModule.INSTANCE))
            .withSecretary(new ModuleImports<>(List.of(new StageSecretaryModule.StageSecretary()), StageSecretaryModule.INSTANCE));
}
