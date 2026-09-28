package hue.captains.singapura.js.homing.workspace.parties;

import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
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
 * <p>How a page has it: the module its constant is served from, and its
 * DEFAULT ROOT SECRETARY - the secretary of a root instance of the type, the
 * one a workspace puts there unless it puts its own. A workspace's root
 * parties are resolved from its kinds' types, in Java, and a type with no
 * secretary for its root - neither its default nor the workspace's - fails
 * the build, never the page. A type that is never on a page needs neither.</p>
 *
 * @param name       the type's name, which is its identity on a page: {@code book-selection}
 * @param vocabulary the sealed interface its messages are records of
 * @param constant   the import of its constant, {@link #constName()}, from the module that serves it
 * @param secretary  the import of its default root secretary, if it has one
 * @param <M>        the vocabulary
 */
public record PartyType<M>(String name, Class<M> vocabulary, Optional<ModuleImports<?>> constant, Optional<ModuleImports<?>> secretary) {

    /** A type's name: lowercase letters, digits and hyphens, a letter first. */
    public static final Pattern NAME = Pattern.compile("[a-z][a-z0-9-]*");

    /** A type as Java has it, not yet on a page: no constant served, no default secretary. */
    public PartyType(String name, Class<M> vocabulary) { this(name, vocabulary, Optional.empty(), Optional.empty()); }

    /** The same type, its constant served by the import given: one export, named {@link #constName()}. */
    public PartyType<M> servedFrom(ModuleImports<?> imports) { return new PartyType<>(name, vocabulary, Optional.of(imports), secretary); }

    /** The same type, the import given its default root secretary: one export. */
    public PartyType<M> withSecretary(ModuleImports<?> imports) { return new PartyType<>(name, vocabulary, constant, Optional.of(imports)); }

    /** The one export an import brings: a constant's, a secretary's name in JavaScript. */
    public static String exportName(ModuleImports<?> imports) {
        List<?> all = imports.allImports();
        if (all.size() != 1) throw new IllegalArgumentException("one export, not " + all.size() + ": " + all);
        return all.get(0).getClass().getSimpleName();
    }

    public PartyType {
        Objects.requireNonNull(name, "PartyType.name");
        Objects.requireNonNull(vocabulary, "PartyType.vocabulary");
        Objects.requireNonNull(constant, "PartyType.constant");
        Objects.requireNonNull(secretary, "PartyType.secretary");
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
        String constName = name.replace('-', '_').toUpperCase(Locale.ROOT);
        constant.ifPresent(c -> {
            if (!exportName(c).equals(constName)) throw new IllegalArgumentException("PartyType '" + name + "': its constant is served as " + constName + ", not " + exportName(c));
        });
        secretary.ifPresent(PartyType::exportName);
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
