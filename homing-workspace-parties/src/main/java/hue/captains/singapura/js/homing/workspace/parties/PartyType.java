package hue.captains.singapura.js.homing.workspace.parties;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A messaging party's type, declared (the doctrine Messaging Parties Are
 * Joined Top-Down): its name, and the vocabulary it carries - a sealed
 * interface whose every permitted kind is a record, the record's components
 * the message's fields. The type is the party's identity: a widget declares
 * the types it needs, a substrate gives it an instance of each, and it joins
 * each by its type. What happens in a party is actions, so a kind is named
 * for what it does or what it says, and the vocabulary is all a party knows.
 *
 * <p>Its JavaScript is generated from here - the name, and each kind's fields
 * with their types - so a page checks every message against what Java
 * declared, and no second list can drift from it.</p>
 *
 * @param name       the type's name, which is its identity on a page: {@code book-selection}
 * @param vocabulary the sealed interface its messages are records of
 * @param <M>        the vocabulary
 */
public record PartyType<M>(String name, Class<M> vocabulary) {

    /** A type's name: lowercase letters, digits and hyphens, a letter first. */
    public static final Pattern NAME = Pattern.compile("[a-z][a-z0-9-]*");

    public PartyType {
        Objects.requireNonNull(name, "PartyType.name");
        Objects.requireNonNull(vocabulary, "PartyType.vocabulary");
        if (!NAME.matcher(name).matches()) throw new IllegalArgumentException("PartyType.name '" + name + "': lowercase letters, digits and hyphens, a letter first");
        if (!vocabulary.isInterface() || !vocabulary.isSealed()) throw new IllegalArgumentException("PartyType '" + name + "': its vocabulary is a sealed interface, not " + vocabulary.getName());
        var seen = new HashSet<String>();
        for (Class<?> kind : vocabulary.getPermittedSubclasses()) {
            if (!kind.isRecord()) throw new IllegalArgumentException("PartyType '" + name + "': every kind is a record, not " + kind.getName());
            if (!seen.add(kind.getSimpleName())) throw new IllegalArgumentException("PartyType '" + name + "': two kinds named " + kind.getSimpleName());
            for (var c : kind.getRecordComponents()) {
                if (c.getName().equals("kind")) throw new IllegalArgumentException("PartyType '" + name + "': " + kind.getSimpleName() + " has a field named kind, which is the message's own");
                jsType(c.getType());   // refused here, not when the page is served
            }
        }
        if (seen.isEmpty()) throw new IllegalArgumentException("PartyType '" + name + "': a vocabulary of no kinds");
    }

    /** The kinds, in the order the vocabulary declares them. */
    public List<Class<?>> kinds() { return List.of(vocabulary.getPermittedSubclasses()); }

    /** The constant's name in JavaScript: {@code book-selection} is {@code BOOK_SELECTION}. */
    public String constName() { return name.replace('-', '_').toUpperCase(Locale.ROOT); }

    /**
     * The type in JavaScript, one frozen constant:
     * {@code const BOOK_SELECTION = Object.freeze({ name: "book-selection", kinds: { Select: { id: "string" }, … } })}.
     */
    public String js() {
        String kinds = kinds().stream().map(k -> {
            String fields = java.util.Arrays.stream(k.getRecordComponents())
                    .map(c -> c.getName() + ": \"" + jsType(c.getType()) + "\"")
                    .collect(Collectors.joining(", "));
            return k.getSimpleName() + ": Object.freeze({" + (fields.isEmpty() ? "" : " " + fields + " ") + "})";
        }).collect(Collectors.joining(", "));
        return "const " + constName() + " = Object.freeze({ name: \"" + name + "\", kinds: Object.freeze({ " + kinds + " }) });";
    }

    /** A field's type as JavaScript's typeof says it: text, a number, or a truth. Anything else is refused. */
    static String jsType(Class<?> t) {
        if (t == String.class) return "string";
        if (t == boolean.class || t == Boolean.class) return "boolean";
        if (t == int.class || t == long.class || t == double.class || t == float.class || t == short.class || t == byte.class
                || Number.class.isAssignableFrom(t)) return "number";
        throw new IllegalArgumentException("a message's field is text, a number or a truth, not " + t.getName());
    }
}
