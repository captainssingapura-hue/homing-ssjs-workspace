package hue.captains.singapura.js.homing.workspace.content;

import java.util.Objects;

/**
 * One of a widget's params, as a content party carries it: a name and its value, both text,
 * as every arrangement's params are. A message carries a widget's params as a list of these,
 * in the order of their names, so the same params are the same list wherever they are asked.
 *
 * @param name  the param's name: {@code key}
 * @param value its value: {@code design/keys}
 */
public record Param(String name, String value) {
    public Param {
        Objects.requireNonNull(name, "Param.name");
        Objects.requireNonNull(value, "Param.value");
    }
}
