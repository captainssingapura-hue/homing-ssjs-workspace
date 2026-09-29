package hue.captains.singapura.js.homing.workspace.groups.core.models;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A placement engine, by name: what decides where a workspace's widgets go -
 * the split grid, one pane, magnetic tiles. A workspace has an arrangement for
 * each engine it may be shown by; each placement says the engine it is for.
 */
public record PlacementEngine(String value) {

    private static final Pattern GRAMMAR = Pattern.compile("[a-z][a-z0-9-]*");

    public PlacementEngine {
        Objects.requireNonNull(value, "PlacementEngine.value");
        if (!GRAMMAR.matcher(value).matches()) {
            throw new IllegalArgumentException("PlacementEngine.value '" + value + "' - lower case, a letter first, then letters, digits, hyphen");
        }
    }

    public static PlacementEngine of(String value) { return new PlacementEngine(value); }

    @Override public String toString() { return value; }
}
