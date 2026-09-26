package hue.captains.singapura.js.homing.workspace.codecs.log;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * What a record component is on the workspace log's wire, read off its Java
 * type at build time: a string, an int, a safe integer, a boolean, an instant
 * (whole milliseconds), a UUID, one of the log's own types, or a list of any of
 * these. Anything else has no form on the wire — a double, a float, a boxed
 * number, a map — and the generator refuses it where it is declared, so an
 * ambiguous type never reaches a log.
 *
 * <p>Each slot says, in each language, how a value of it is checked, encoded and
 * decoded; the generators compose these into straight-line code.</p>
 */
sealed interface LogSlot {

    record Str()    implements LogSlot {}
    record Int32()  implements LogSlot {}
    record Long53() implements LogSlot {}
    record Bool()   implements LogSlot {}
    record Time()   implements LogSlot {}
    record Uid()    implements LogSlot {}
    record Typed(Class<?> type) implements LogSlot {}
    record ListOf(LogSlot element) implements LogSlot {}

    static LogSlot of(Type t, String where) {
        if (t == String.class)  return new Str();
        if (t == int.class)     return new Int32();
        if (t == long.class)    return new Long53();
        if (t == boolean.class) return new Bool();
        if (t == Instant.class) return new Time();
        if (t == UUID.class)    return new Uid();
        if (t instanceof ParameterizedType p && p.getRawType() == List.class) {
            return new ListOf(of(p.getActualTypeArguments()[0], where + "[]"));
        }
        if (t instanceof Class<?> c && (c.isRecord() || c.isEnum() || (c.isInterface() && c.isSealed()))) return new Typed(c);
        throw new IllegalArgumentException(where + ": the workspace log's wire has no form for " + t.getTypeName()
                + " — declare an exact type (a safe integer, a Scaled, a record) instead");
    }

    /** The log types a slot refers to, so the manifest can be checked for them. */
    static void typesIn(LogSlot s, List<Class<?>> into) {
        switch (s) {
            case Typed t  -> into.add(t.type());
            case ListOf l -> typesIn(l.element(), into);
            default       -> { }
        }
    }

    // ── JavaScript ───────────────────────────────────────────────────────

    /** A boolean expression: whether {@code x} is a value of this slot. */
    static String jsCheck(LogSlot s, String x, int depth) {
        return switch (s) {
            case Str st    -> "typeof " + x + " === \"string\"";
            case Int32 i   -> "Number.isInteger(" + x + ") && " + x + " >= -2147483648 && " + x + " <= 2147483647";
            case Long53 l  -> "Number.isSafeInteger(" + x + ")";
            case Bool b    -> "typeof " + x + " === \"boolean\"";
            case Time t    -> "Number.isSafeInteger(" + x + ") && " + x + " >= 0";
            case Uid u     -> "typeof " + x + " === \"string\" && LogWire.UUID.test(" + x + ")";
            case Typed t   -> x + " instanceof " + t.type().getSimpleName();
            case ListOf l  -> {
                String e = "e" + depth;
                yield "Array.isArray(" + x + ") && " + x + ".every(function (" + e + ") { return " + jsCheck(l.element(), e, depth + 1) + "; })";
            }
        };
    }

    /** What a value of this slot is, said in a refusal. */
    static String describe(LogSlot s) {
        return switch (s) {
            case Str st    -> "a string";
            case Int32 i   -> "an int";
            case Long53 l  -> "a safe integer";
            case Bool b    -> "a boolean";
            case Time t    -> "whole milliseconds since the epoch";
            case Uid u     -> "a lower-case UUID";
            case Typed t   -> "a " + t.type().getSimpleName();
            case ListOf l  -> "an array of " + describe(l.element());
        };
    }

    static String jsEncode(LogSlot s, String x, int depth) {
        return switch (s) {
            case Typed t  -> t.type().getSimpleName() + "Codec.transformTo(" + x + ")";
            case ListOf l -> {
                String e = "e" + depth;
                yield x + ".map(function (" + e + ") { return " + jsEncode(l.element(), e, depth + 1) + "; })";
            }
            default       -> x;
        };
    }

    static String jsDecode(LogSlot s, String x, String what, int depth) {
        return switch (s) {
            case Typed t  -> t.type().getSimpleName() + "Codec.transformFrom(" + x + ")";
            case ListOf l -> {
                String e = "e" + depth;
                yield "LogWire.array(" + x + ", " + jsString(what) + ").map(function (" + e + ") { return "
                        + jsDecode(l.element(), e, what + "[]", depth + 1) + "; })";
            }
            default       -> x;
        };
    }

    // ── Java ─────────────────────────────────────────────────────────────

    static String javaEncode(LogSlot s, String x, int depth) {
        return switch (s) {
            case Str st    -> "Wire.string(" + x + ")";
            case Int32 i   -> "Wire.integer(" + x + ")";
            case Long53 l  -> "Wire.integer(" + x + ")";
            case Bool b    -> "Wire.bool(" + x + ")";
            case Time t    -> "Wire.instant(" + x + ")";
            case Uid u     -> "Wire.uuid(" + x + ")";
            case Typed t   -> t.type().getSimpleName() + "Codec.INSTANCE.transformTo(" + x + ")";
            case ListOf l  -> {
                String e = "e" + depth;
                yield "new Json.Arr(" + x + ".stream().map(" + e + " -> " + javaEncode(l.element(), e, depth + 1) + ").toList())";
            }
        };
    }

    static String javaDecode(LogSlot s, String x, String what, int depth) {
        String w = javaString(what);
        return switch (s) {
            case Str st    -> "Wire.string(" + x + ", " + w + ")";
            case Int32 i   -> "Wire.int32(" + x + ", " + w + ")";
            case Long53 l  -> "Wire.safeLong(" + x + ", " + w + ")";
            case Bool b    -> "Wire.bool(" + x + ", " + w + ")";
            case Time t    -> "Wire.instant(" + x + ", " + w + ")";
            case Uid u     -> "Wire.uuid(" + x + ", " + w + ")";
            case Typed t   -> t.type().getSimpleName() + "Codec.INSTANCE.transformFrom(" + x + ")";
            case ListOf l  -> {
                String e = "e" + depth;
                yield "Wire.array(" + x + ", " + w + ").stream().map(" + e + " -> "
                        + javaDecode(l.element(), e, what + "[]", depth + 1) + ").toList()";
            }
        };
    }

    // ── literals ─────────────────────────────────────────────────────────

    static String jsString(String s) { return javaString(s); }

    /** A string literal both languages read the same: letters, digits and the few marks names are made of. */
    static String javaString(String s) {
        var sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            if (c == '"' || c == '\\') sb.append('\\');
            if (c < 0x20 || c > 0x7e) throw new IllegalArgumentException("not a plain name: " + s);
            sb.append(c);
        }
        return sb.append('"').toString();
    }
}
