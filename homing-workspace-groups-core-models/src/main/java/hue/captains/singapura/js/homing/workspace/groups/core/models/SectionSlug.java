package hue.captains.singapura.js.homing.workspace.groups.core.models;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A section's segment in its workspaces' paths: lowercase letters and digits in
 * words joined by single hyphens - {@code media}, {@code games-and-toys}.
 * Derived from the section's title unless given; a title with no letter or
 * digit to keep (one in a script other than Latin) derives nothing, and is
 * refused with a request for a slug of its own.
 *
 * @param value the slug
 */
public record SectionSlug(String value) {

    private static final Pattern GRAMMAR = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*");

    public SectionSlug {
        Objects.requireNonNull(value, "SectionSlug.value");
        if (!GRAMMAR.matcher(value).matches()) {
            throw new IllegalArgumentException("SectionSlug.value '" + value + "' - lowercase letters and digits, words joined by single hyphens");
        }
    }

    public static SectionSlug of(String value) { return new SectionSlug(value); }

    /**
     * The slug a title derives: its ASCII letters, lowercased, and its digits;
     * every run of anything else one hyphen; none at either end.
     */
    public static SectionSlug from(String title) {
        Objects.requireNonNull(title, "SectionSlug.from(title)");
        var out = new StringBuilder();
        boolean gap = false;
        for (int i = 0; i < title.length(); i++) {
            char c = title.charAt(i);
            boolean keep = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
            if (keep) {
                if (gap && out.length() > 0) out.append('-');
                out.append(Character.toLowerCase(c));
                gap = false;
            } else {
                gap = true;
            }
        }
        if (out.length() == 0) {
            throw new IllegalArgumentException("the section '" + title + "' derives no slug - it has no letter or digit to keep; give it one");
        }
        return new SectionSlug(out.toString());
    }

    @Override public String toString() { return value; }
}
