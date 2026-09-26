package hue.captains.singapura.js.homing.workspace.log.json;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A JSON value as the workspace log's wire carries it: a string, a SAFE
 * integer, a boolean, null, an array, an object whose members keep their
 * order. Nothing else — no fraction, no exponent, no integer JavaScript
 * cannot hold exactly — so a value means the same thing to either language
 * that reads it, and is written the same way by both.
 */
public sealed interface Json {

    /** The largest integer a JavaScript number holds exactly: 2^53 − 1. */
    long MAX_SAFE = 9_007_199_254_740_991L;

    record Str(String value) implements Json {
        public Str { Objects.requireNonNull(value, "Json.Str.value"); }
    }

    record Int(long value) implements Json {
        public Int {
            if (value > MAX_SAFE || value < -MAX_SAFE) {
                throw new IllegalArgumentException("Json.Int " + value + " — beyond ±(2^53 − 1)");
            }
        }
    }

    record Bool(boolean value) implements Json {
        public static final Bool TRUE = new Bool(true);
        public static final Bool FALSE = new Bool(false);
        public static Bool of(boolean b) { return b ? TRUE : FALSE; }
    }

    record Null() implements Json {
        public static final Null INSTANCE = new Null();
    }

    record Arr(List<Json> items) implements Json {
        public Arr { items = List.copyOf(Objects.requireNonNull(items, "Json.Arr.items")); }
    }

    /** An object: its members in the order they were put, which is the order they are written. */
    record Obj(Map<String, Json> members) implements Json {
        public Obj {
            Objects.requireNonNull(members, "Json.Obj.members");
            var copy = new LinkedHashMap<String, Json>();
            members.forEach((k, v) -> copy.put(Objects.requireNonNull(k, "Json.Obj key"), Objects.requireNonNull(v, "Json.Obj value")));
            members = Collections.unmodifiableMap(copy);
        }
    }
}
