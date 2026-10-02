package hue.captains.singapura.js.homing.workspace.log.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * JSON text for the workspace log's wire, both ways.
 *
 * <p>{@link #write} writes a value the way JavaScript's {@code JSON.stringify}
 * writes the same value, byte for byte: no whitespace; members in their order;
 * integers in plain decimal; in a string, the quote and the backslash escaped,
 * the control characters by their short escapes (b, f, n, r, t) or as a
 * four-digit escape in lower-case hex, a lone surrogate as a four-digit escape
 * too, and everything else as it is. So a log written by either language is
 * the same file.</p>
 *
 * <p>{@link #parse} reads JSON and refuses what the wire does not carry: a
 * fraction, an exponent, {@code -0}, an integer beyond ±(2^53 − 1), a member
 * named twice. Whitespace between tokens is read and forgotten; whether a text
 * was written canonically is a question for writing it back and comparing.</p>
 */
public final class JsonText {

    private JsonText() {}

    /** Malformed text, or text the wire does not carry: where, and what. */
    public static final class Malformed extends IllegalArgumentException {
        public final int offset;
        Malformed(int offset, String what) {
            super("at " + offset + ": " + what);
            this.offset = offset;
        }
    }

    // ── writing ──────────────────────────────────────────────────────────

    public static String write(Json value) {
        var sb = new StringBuilder();
        write(sb, value);
        return sb.toString();
    }

    private static void write(StringBuilder sb, Json v) {
        switch (v) {
            case Json.Str s  -> string(sb, s.value());
            case Json.Int i  -> sb.append(i.value());
            case Json.Bool b -> sb.append(b.value());
            case Json.Null n -> sb.append("null");
            case Json.Arr a  -> {
                sb.append('[');
                for (int k = 0; k < a.items().size(); k++) {
                    if (k > 0) sb.append(',');
                    write(sb, a.items().get(k));
                }
                sb.append(']');
            }
            case Json.Obj o  -> {
                sb.append('{');
                boolean first = true;
                for (var e : o.members().entrySet()) {
                    if (!first) sb.append(',');
                    first = false;
                    string(sb, e.getKey());
                    sb.append(':');
                    write(sb, e.getValue());
                }
                sb.append('}');
            }
        }
    }

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private static void string(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        unicode(sb, c);
                    } else if (Character.isHighSurrogate(c)) {
                        if (i + 1 < s.length() && Character.isLowSurrogate(s.charAt(i + 1))) {
                            sb.append(c).append(s.charAt(++i));
                        } else {
                            unicode(sb, c);
                        }
                    } else if (Character.isLowSurrogate(c)) {
                        unicode(sb, c);
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }

    private static void unicode(StringBuilder sb, char c) {
        sb.append("\\u").append(HEX[(c >> 12) & 0xF]).append(HEX[(c >> 8) & 0xF])
          .append(HEX[(c >> 4) & 0xF]).append(HEX[c & 0xF]);
    }

    // ── reading ──────────────────────────────────────────────────────────

    public static Json parse(String text) {
        var p = new Parser(text);
        p.space();
        Json v = p.value();
        p.space();
        if (p.at < text.length()) throw new Malformed(p.at, "text after the value");
        return v;
    }

    private static final class Parser {
        final String s;
        int at;

        Parser(String s) { this.s = s; }

        void space() {
            while (at < s.length()) {
                char c = s.charAt(at);
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r') at++;
                else break;
            }
        }

        char peek() {
            if (at >= s.length()) throw new Malformed(at, "the text ended early");
            return s.charAt(at);
        }

        void expect(char c) {
            if (peek() != c) throw new Malformed(at, "expected '" + c + "', found '" + s.charAt(at) + "'");
            at++;
        }

        void word(String w, int from) {
            if (!s.startsWith(w, from)) throw new Malformed(from, "expected " + w);
            at = from + w.length();
        }

        Json value() {
            char c = peek();
            return switch (c) {
                case '{' -> object();
                case '[' -> array();
                case '"' -> new Json.Str(string());
                case 't' -> { word("true", at); yield Json.Bool.TRUE; }
                case 'f' -> { word("false", at); yield Json.Bool.FALSE; }
                case 'n' -> { word("null", at); yield Json.Null.INSTANCE; }
                default -> {
                    if (c == '-' || (c >= '0' && c <= '9')) yield integer();
                    throw new Malformed(at, "no value starts with '" + c + "'");
                }
            };
        }

        Json object() {
            expect('{');
            var members = new LinkedHashMap<String, Json>();
            space();
            if (peek() == '}') { at++; return new Json.Obj(members); }
            while (true) {
                space();
                int keyAt = at;
                if (peek() != '"') throw new Malformed(at, "a member's name must be a string");
                String key = string();
                if (members.containsKey(key)) throw new Malformed(keyAt, "the member \"" + key + "\" is named twice");
                space();
                expect(':');
                space();
                members.put(key, value());
                space();
                if (peek() == ',') { at++; continue; }
                expect('}');
                return new Json.Obj(members);
            }
        }

        Json array() {
            expect('[');
            var items = new ArrayList<Json>();
            space();
            if (peek() == ']') { at++; return new Json.Arr(items); }
            while (true) {
                space();
                items.add(value());
                space();
                if (peek() == ',') { at++; continue; }
                expect(']');
                return new Json.Arr(items);
            }
        }

        Json integer() {
            int from = at;
            boolean negative = peek() == '-';
            if (negative) at++;
            int digitsFrom = at;
            if (peek() == '0') {
                at++;
            } else if (peek() >= '1' && peek() <= '9') {
                while (at < s.length() && s.charAt(at) >= '0' && s.charAt(at) <= '9') at++;
            } else {
                throw new Malformed(at, "a digit after '-'");
            }
            if (at < s.length()) {
                char c = s.charAt(at);
                if (c == '.' || c == 'e' || c == 'E') throw new Malformed(at, "a fraction or an exponent: the wire carries integers only");
                if (c >= '0' && c <= '9') throw new Malformed(at, "a leading zero");
            }
            String digits = s.substring(digitsFrom, at);
            if (digits.length() > 16) throw new Malformed(from, "an integer beyond ±(2^53 − 1)");
            long magnitude = Long.parseLong(digits);
            if (magnitude > Json.MAX_SAFE) throw new Malformed(from, "an integer beyond ±(2^53 − 1)");
            if (negative && magnitude == 0) throw new Malformed(from, "-0: the same number as 0, written another way");
            return new Json.Int(negative ? -magnitude : magnitude);
        }

        String string() {
            expect('"');
            var sb = new StringBuilder();
            while (true) {
                if (at >= s.length()) throw new Malformed(at, "a string not closed");
                char c = s.charAt(at++);
                if (c == '"') return sb.toString();
                if (c < 0x20) throw new Malformed(at - 1, "a control character not escaped");
                if (c != '\\') { sb.append(c); continue; }
                if (at >= s.length()) throw new Malformed(at, "an escape not finished");
                char e = s.charAt(at++);
                switch (e) {
                    case '"'  -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/'  -> sb.append('/');
                    case 'b'  -> sb.append('\b');
                    case 'f'  -> sb.append('\f');
                    case 'n'  -> sb.append('\n');
                    case 'r'  -> sb.append('\r');
                    case 't'  -> sb.append('\t');
                    case 'u'  -> {
                        if (at + 4 > s.length()) throw new Malformed(at, "\\u wants four hex digits");
                        int code = 0;
                        for (int k = 0; k < 4; k++) {
                            int d = Character.digit(s.charAt(at + k), 16);
                            if (d < 0) throw new Malformed(at + k, "\\u wants four hex digits");
                            code = code * 16 + d;
                        }
                        at += 4;
                        sb.append((char) code);
                    }
                    default -> throw new Malformed(at - 1, "no escape \\" + e);
                }
            }
        }
    }

}
