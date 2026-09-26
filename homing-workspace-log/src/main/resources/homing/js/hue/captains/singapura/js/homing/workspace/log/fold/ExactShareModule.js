// =============================================================================
// ExactShare — a share of a split while the layout algebra works on it: an
// exact fraction of BigInts, never a double, so the arithmetic is the same as
// the Java fold's. What the algebra ends with is brought back to whole
// millionths by ExactShare.millionths, one rule for every split: each share's
// floor, the millionths left over to the largest remainders — the earlier on a
// tie — and no share left at nothing. Java's ExactShare, transcribed.
//
//   ExactShare.millionthsOf(units)  ExactShare.half()
//   a.plus(b) a.minus(b) a.times(b) a.over(b)
//   ExactShare.millionths([shares adding up to one]) → [whole millionths adding up to a million]
// =============================================================================

function _gcd(a, b) {
    a = a < 0n ? -a : a;
    b = b < 0n ? -b : b;
    while (b !== 0n) { var t = a % b; a = b; b = t; }
    return a;
}

class ExactShare {
    static WHOLE = 1000000n;

    constructor(num, den) {
        if (den <= 0n) throw new Error("[ExactShare] a denominator above zero");
        var g = _gcd(num, den);
        if (g !== 1n && g !== 0n) { num = num / g; den = den / g; }
        if (num === 0n) den = 1n;
        this.num = num;
        this.den = den;
        Object.freeze(this);
    }

    static millionthsOf(units) { return new ExactShare(BigInt(units), ExactShare.WHOLE); }

    static half() { return new ExactShare(1n, 2n); }

    plus(o)  { return new ExactShare(this.num * o.den + o.num * this.den, this.den * o.den); }
    minus(o) { return new ExactShare(this.num * o.den - o.num * this.den, this.den * o.den); }
    times(o) { return new ExactShare(this.num * o.num, this.den * o.den); }
    over(o) {
        if (o.num === 0n) throw new Error("[ExactShare] over nothing");
        var n = this.num * o.den, d = this.den * o.num;
        return d < 0n ? new ExactShare(-n, -d) : new ExactShare(n, d);
    }

    /** Shares adding up to exactly one, as whole millionths adding up to exactly a million. */
    static millionths(shares) {
        var n = shares.length, units = [], rem = [], den = [], sum = 0n;
        for (var i = 0; i < n; i++) {
            var scaled = shares[i].num * ExactShare.WHOLE;
            units.push(scaled / shares[i].den);
            rem.push(scaled % shares[i].den);
            den.push(shares[i].den);
            sum += units[i];
        }
        var order = [];
        for (var k = 0; k < n; k++) order.push(k);
        order.sort(function (a, b) {
            var x = rem[b] * den[a], y = rem[a] * den[b];   // the larger remainder first
            return x > y ? 1 : x < y ? -1 : a - b;
        });
        for (var m = 0n; m < ExactShare.WHOLE - sum; m++) units[order[Number(m)]] += 1n;
        for (var j = 0; j < n; j++) {
            if (units[j] > 0n) continue;
            var big = 0;
            for (var q = 1; q < n; q++) if (units[q] > units[big]) big = q;
            units[big] -= 1n;
            units[j] = 1n;
        }
        return units.map(function (u) { return Number(u); });
    }
}
