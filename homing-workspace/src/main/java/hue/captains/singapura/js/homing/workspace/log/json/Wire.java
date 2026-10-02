package hue.captains.singapura.js.homing.workspace.log.json;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * What a generated codec reads a wire value as, and writes one from: the few
 * shapes the workspace log's types are made of. Each reader names what it was
 * reading when the value is not what it should be, so a refused line says
 * which field refused it. Straight-line: no reflection, nothing looked up.
 */
public final class Wire {

    private Wire() {}

    /** A wire value that is not what its type says it must be. */
    public static final class Refused extends IllegalArgumentException {
        public Refused(String what, String why) { super(what + ": " + why); }
    }

    // ── reading ──────────────────────────────────────────────────────────

    /** An object with exactly these members, no more and no fewer. */
    public static Map<String, Json> object(Json w, String what, Set<String> keys) {
        if (!(w instanceof Json.Obj o)) throw new Refused(what, "expected an object, got " + kind(w));
        for (String k : o.members().keySet()) {
            if (!keys.contains(k)) throw new Refused(what, "no member \"" + k + "\"");
        }
        for (String k : keys) {
            if (!o.members().containsKey(k)) throw new Refused(what, "the member \"" + k + "\" is missing");
        }
        return o.members();
    }

    /** The variant a sealed type's value says it is. */
    public static String type(Json w, String what) {
        if (!(w instanceof Json.Obj o)) throw new Refused(what, "expected an object, got " + kind(w));
        return string(o.members().get("type"), what + ".type");
    }

    public static String string(Json w, String what) {
        if (w instanceof Json.Str s) return s.value();
        throw new Refused(what, "expected a string, got " + kind(w));
    }

    public static long safeLong(Json w, String what) {
        if (w instanceof Json.Int i) return i.value();
        throw new Refused(what, "expected an integer, got " + kind(w));
    }

    public static int int32(Json w, String what) {
        long v = safeLong(w, what);
        if (v < Integer.MIN_VALUE || v > Integer.MAX_VALUE) throw new Refused(what, v + " is beyond an int");
        return (int) v;
    }

    public static boolean bool(Json w, String what) {
        if (w instanceof Json.Bool b) return b.value();
        throw new Refused(what, "expected a boolean, got " + kind(w));
    }

    public static List<Json> array(Json w, String what) {
        if (w instanceof Json.Arr a) return a.items();
        throw new Refused(what, "expected an array, got " + kind(w));
    }

    /** Whole milliseconds since the epoch. */
    public static Instant instant(Json w, String what) {
        long ms = safeLong(w, what);
        if (ms < 0) throw new Refused(what, ms + " is before the epoch");
        return Instant.ofEpochMilli(ms);
    }

    /** A UUID in its one written form: lower-case, hyphenated. */
    public static UUID uuid(Json w, String what) {
        String s = string(w, what);
        UUID u;
        try { u = UUID.fromString(s); }
        catch (IllegalArgumentException e) { throw new Refused(what, "\"" + s + "\" is not a UUID"); }
        if (!u.toString().equals(s)) throw new Refused(what, "\"" + s + "\" is not written as " + u);
        return u;
    }

    /** An optional value: null on the wire is none; anything else is read as the value. */
    public static <T> java.util.Optional<T> optionalOf(Json w, java.util.function.Function<Json, T> read) {
        return w instanceof Json.Null ? java.util.Optional.empty() : java.util.Optional.of(read.apply(w));
    }

    /** An optional value written: none is null on the wire. */
    public static <T> Json orNull(java.util.Optional<T> v, java.util.function.Function<T, Json> write) {
        return v.isPresent() ? write.apply(v.get()) : Json.Null.INSTANCE;
    }

    /** Whatever a record's constructor refused, said as the field that carried it. */
    public static <T> T build(String what, java.util.function.Supplier<T> make) {
        try { return make.get(); }
        catch (Refused e) { throw e; }
        catch (IllegalArgumentException | NullPointerException e) { throw new Refused(what, e.getMessage()); }
    }

    // ── writing ──────────────────────────────────────────────────────────

    public static Json string(String s) { return new Json.Str(s); }

    public static Json integer(long v) { return new Json.Int(v); }

    public static Json bool(boolean b) { return Json.Bool.of(b); }

    public static Json instant(Instant t) {
        if (t.getNano() % 1_000_000 != 0) throw new Refused("instant", t + " is finer than a millisecond");
        return new Json.Int(t.toEpochMilli());
    }

    public static Json uuid(UUID u) { return new Json.Str(u.toString()); }

    private static String kind(Json w) {
        if (w == null) return "nothing";
        return switch (w) {
            case Json.Str s  -> "a string";
            case Json.Int i  -> "an integer";
            case Json.Bool b -> "a boolean";
            case Json.Null n -> "null";
            case Json.Arr a  -> "an array";
            case Json.Obj o  -> "an object";
        };
    }
}
