package hue.captains.singapura.js.homing.workspace.widgets;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A widget's params on an address: read off the query, and written back. A
 * functional object each widget kind provides, so that any page holding a
 * kind and a query can make the widget - the bench, first.
 *
 * <p>Both ways, as an app's {@code ParamCodec} is: a page's params are
 * written back into the page it serves, so what is read must be writable.
 * {@link #to} writes a param at its default as nothing, so that an address
 * says only what differs, and a routed page's own params never hide what
 * the address asked for.</p>
 *
 * @param <P> the widget's params
 */
public interface WidgetQuery<P extends WidgetParams> extends StatelessFunctionalObject {

    /** The params read off a query, or why not - naming the key. */
    Read<P> from(Map<String, List<String>> query);

    /** The params as a query: only what differs from the defaults. Total. */
    Map<String, List<String>> to(P params);

    /** The params, or the key that could not be read, its value, and what was expected. */
    sealed interface Read<P extends WidgetParams> {

        record Ok<P extends WidgetParams>(P params) implements Read<P> {
            public Ok { Objects.requireNonNull(params, "Read.Ok.params"); }
        }

        record Refused<P extends WidgetParams>(String key, String value, String expected) implements Read<P> {
            public Refused {
                Objects.requireNonNull(key, "Read.Refused.key");
                Objects.requireNonNull(expected, "Read.Refused.expected");
            }
        }

        static <P extends WidgetParams> Read<P> ok(P params) { return new Ok<>(params); }

        static <P extends WidgetParams> Read<P> refused(String key, String value, String expected) {
            return new Refused<>(key, value, expected);
        }
    }
}
