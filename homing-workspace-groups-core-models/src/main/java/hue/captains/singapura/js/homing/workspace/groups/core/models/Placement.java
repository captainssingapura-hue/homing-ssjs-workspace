package hue.captains.singapura.js.homing.workspace.groups.core.models;

import java.util.List;

/**
 * Where a workspace's widgets go, as one placement engine has it: the engine's
 * own vocabulary - a split grid's regions, a pane, tiles - naming each widget by
 * the arrangement's {@link WidgetRef}. An engine's placement is a type of its
 * own; this is what every one of them says.
 */
public interface Placement {

    /** The engine it is for. */
    PlacementEngine engine();

    /** The widgets it places, in the engine's order: each once. */
    List<WidgetRef> placed();
}
