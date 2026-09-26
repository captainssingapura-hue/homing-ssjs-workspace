package hue.captains.singapura.js.homing.workspace.log;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * An exact decimal: {@code units} of {@code 10^-scale}. The log carries no
 * floating-point number — a double is written differently by every language
 * that prints one — so a fraction is a count of a fixed unit instead. Both
 * parts are safe integers, the same number in Java and in JavaScript.
 *
 * @param units the count; its magnitude at most 2^53 − 1
 * @param scale how many decimal places the unit is; 0 to 15
 */
public record Scaled(long units, int scale) {

    /** The largest integer a JavaScript number holds exactly: 2^53 − 1. */
    public static final long MAX_SAFE = 9_007_199_254_740_991L;

    public Scaled {
        if (units > MAX_SAFE || units < -MAX_SAFE) {
            throw new IllegalArgumentException("Scaled.units " + units + " — beyond ±(2^53 − 1)");
        }
        if (scale < 0 || scale > 15) {
            throw new IllegalArgumentException("Scaled.scale " + scale + " — 0 to 15");
        }
    }

    public static Scaled of(long units, int scale) { return new Scaled(units, scale); }

    /** The same number exactly, as a BigDecimal. */
    public BigDecimal toBigDecimal() { return BigDecimal.valueOf(units, scale); }

    /** The number {@code d}, exactly, at the scale {@code scale}; refused when it would need rounding. */
    public static Scaled of(BigDecimal d, int scale) {
        Objects.requireNonNull(d, "Scaled.of(d)");
        return new Scaled(d.setScale(scale).unscaledValue().longValueExact(), scale);
    }

    @Override public String toString() { return toBigDecimal().toPlainString(); }
}
