package hue.captains.singapura.js.homing.workspace.content;

import java.util.List;
import java.util.Objects;

/**
 * An item a party asks its steward to fetch: the params it is wanted by.
 *
 * @param params the params, in the order of their names
 */
public record Item(List<Param> params) {
    public Item {
        params = List.copyOf(Objects.requireNonNull(params, "Item.params"));
    }
}
