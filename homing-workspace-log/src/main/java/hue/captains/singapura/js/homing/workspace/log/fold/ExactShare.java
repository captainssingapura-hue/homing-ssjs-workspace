package hue.captains.singapura.js.homing.workspace.log.fold;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * A share of a split while the layout algebra works on it: an exact fraction,
 * never a double, so the algebra's arithmetic is the same in every language
 * that runs it. What the algebra ends with is brought back to whole millionths
 * by {@link #millionths}, one rule for every split: each share's floor, the
 * millionths left over to the largest remainders — the earlier on a tie — and
 * no share left at nothing. The JavaScript fold does exactly this, with BigInt.
 *
 * @param num the numerator
 * @param den the denominator, above zero, and the fraction in lowest terms
 */
record ExactShare(BigInteger num, BigInteger den) {

    static final long WHOLE = 1_000_000L;
    private static final BigInteger W = BigInteger.valueOf(WHOLE);

    ExactShare {
        if (den.signum() <= 0) throw new IllegalArgumentException("ExactShare: a denominator above zero");
        BigInteger g = num.gcd(den);
        if (!g.equals(BigInteger.ONE) && g.signum() != 0) { num = num.divide(g); den = den.divide(g); }
        if (num.signum() == 0) den = BigInteger.ONE;
    }

    static ExactShare millionthsOf(long units) { return new ExactShare(BigInteger.valueOf(units), W); }

    static ExactShare half() { return new ExactShare(BigInteger.ONE, BigInteger.TWO); }

    ExactShare plus(ExactShare o)  { return new ExactShare(num.multiply(o.den).add(o.num.multiply(den)), den.multiply(o.den)); }
    ExactShare minus(ExactShare o) { return new ExactShare(num.multiply(o.den).subtract(o.num.multiply(den)), den.multiply(o.den)); }
    ExactShare times(ExactShare o) { return new ExactShare(num.multiply(o.num), den.multiply(o.den)); }
    ExactShare over(ExactShare o) {
        if (o.num.signum() == 0) throw new ArithmeticException("ExactShare: over nothing");
        BigInteger n = num.multiply(o.den), d = den.multiply(o.num);
        return d.signum() < 0 ? new ExactShare(n.negate(), d.negate()) : new ExactShare(n, d);
    }

    /**
     * Shares adding up to exactly one, as whole millionths adding up to exactly
     * a million: floors, then one each to the largest remainders, the earlier
     * first on a tie; then any share at nothing is given one, from the largest.
     */
    static long[] millionths(List<ExactShare> shares) {
        int n = shares.size();
        long[] units = new long[n];
        BigInteger[] rem = new BigInteger[n], den = new BigInteger[n];
        long sum = 0;
        for (int i = 0; i < n; i++) {
            BigInteger[] qr = shares.get(i).num.multiply(W).divideAndRemainder(shares.get(i).den);
            units[i] = qr[0].longValueExact();
            rem[i] = qr[1];
            den[i] = shares.get(i).den;
            sum += units[i];
        }
        var order = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) order.add(i);
        order.sort((a, b) -> {
            int c = rem[b].multiply(den[a]).compareTo(rem[a].multiply(den[b]));   // the larger remainder first
            return c != 0 ? c : Integer.compare(a, b);
        });
        for (int k = 0; k < WHOLE - sum; k++) units[order.get(k)]++;
        for (int i = 0; i < n; i++) {
            if (units[i] > 0) continue;
            int big = 0;
            for (int j = 1; j < n; j++) if (units[j] > units[big]) big = j;
            units[big]--;
            units[i] = 1;
        }
        return units;
    }
}
