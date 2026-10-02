package hue.captains.singapura.js.homing.workspace.parties;

import hue.captains.singapura.js.homing.core.ModuleImports;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
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
 * <p>And its DEFAULT STEWARD, when its parties fetch: the one member that does
 * I/O, a class a root instance hires the first time its secretary sends to the
 * steward - one per hierarchy, since a scope linked under another sends up
 * through its link instead. A host that makes a root may hire its own.</p>
 *
 * @param name       the type's name, which is its identity on a page: {@code book-selection}
 * @param vocabulary the sealed interface its messages are records of
 * @param constant   the import of its constant, {@link #constName()}, from the module that serves it
 * @param secretary  the import of its default root secretary, if it has one
 * @param steward    the import of its default steward, if its parties fetch
 * @param <M>        the vocabulary
 */
public record PartyType<M>(String name, Class<M> vocabulary, Optional<ModuleImports<?>> constant, Optional<ModuleImports<?>> secretary,
                           Optional<ModuleImports<?>> steward) {

    /** A type's name: lowercase letters, digits and hyphens, a letter first. */
    public static final Pattern NAME = Pattern.compile("[a-z][a-z0-9-]*");

    /** A type as Java has it, not yet on a page: no constant served, no default secretary, no default steward. */
    public PartyType(String name, Class<M> vocabulary) { this(name, vocabulary, Optional.empty(), Optional.empty(), Optional.empty()); }

    /** The same type, its constant served by the import given: one export, named {@link #constName()}. */
    public PartyType<M> servedFrom(ModuleImports<?> imports) { return new PartyType<>(name, vocabulary, Optional.of(imports), secretary, steward); }

    /** The same type, the import given its default root secretary: one export. */
    public PartyType<M> withSecretary(ModuleImports<?> imports) { return new PartyType<>(name, vocabulary, constant, Optional.of(imports), steward); }

    /** The same type, the import given its default steward - a class a root hires: one export. */
    public PartyType<M> withSteward(ModuleImports<?> imports) { return new PartyType<>(name, vocabulary, constant, secretary, Optional.of(imports)); }

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
        Objects.requireNonNull(steward, "PartyType.steward");
        if (!NAME.matcher(name).matches()) throw new IllegalArgumentException("PartyType.name '" + name + "': lowercase letters, digits and hyphens, a letter first");
        if (!vocabulary.isInterface() || !vocabulary.isSealed()) throw new IllegalArgumentException("PartyType '" + name + "': its vocabulary is a sealed interface, not " + vocabulary.getName());
        var seen = new HashSet<String>();
        for (Class<?> kind : vocabulary.getPermittedSubclasses()) {
            if (!kind.isRecord()) throw new IllegalArgumentException("PartyType '" + name + "': every kind is a record, not " + kind.getName());
            if (!seen.add(kind.getSimpleName())) throw new IllegalArgumentException("PartyType '" + name + "': two kinds named " + kind.getSimpleName());
            for (var c : kind.getRecordComponents()) {
                if (c.getName().equals("kind")) throw new IllegalArgumentException("PartyType '" + name + "': " + kind.getSimpleName() + " has a field named kind, which is the message's own");
                shape(c.getGenericType());   // refused here, not when the page is served
            }
        }
        if (seen.isEmpty()) throw new IllegalArgumentException("PartyType '" + name + "': a vocabulary of no kinds");
        String constName = name.replace('-', '_').toUpperCase(Locale.ROOT);
        constant.ifPresent(c -> {
            if (!exportName(c).equals(constName)) throw new IllegalArgumentException("PartyType '" + name + "': its constant is served as " + constName + ", not " + exportName(c));
        });
        secretary.ifPresent(PartyType::exportName);
        steward.ifPresent(PartyType::exportName);
    }

    /** The kinds, in the order the vocabulary declares them. */
    public List<Class<?>> kinds() { return List.of(vocabulary.getPermittedSubclasses()); }

    /** The constant's name in JavaScript: {@code book-selection} is {@code BOOK_SELECTION}. */
    public String constName() { return name.replace('-', '_').toUpperCase(Locale.ROOT); }

    /**
     * The type in JavaScript, one frozen constant - each kind its fields' shapes:
     * {@code const BOOK_SELECTION = Object.freeze({ name: "book-selection", kinds: { Select: { id: "string" }, … } })};
     * a record field, the object of its fields' shapes, {@code { x: "number", … }}; a list, the one shape all
     * it holds has, in brackets, {@code [{ x: "number", … }]}.
     */
    public String js() {
        String kinds = kinds().stream().map(k -> k.getSimpleName() + ": " + record(k, new ArrayDeque<>())).collect(Collectors.joining(", "));
        return "const " + constName() + " = Object.freeze({ name: \"" + name + "\", kinds: Object.freeze({ " + kinds + " }) });";
    }

    /**
     * A field's shape, as the page checks it: text, a number or a truth as {@code typeof} says it; a
     * RECORD of such fields, the object of their shapes; a LIST of any of these, the one shape all it holds
     * has. Plain data all the way down - what JSON could carry - so a message is a plain object a page
     * can check, copy and freeze. Anything else is refused: a map, an array, an optional, a raw or a
     * wildcard list, a record that holds itself.
     */
    static String shape(Type t) { return shape(t, new ArrayDeque<>()); }

    private static String shape(Type t, Deque<Class<?>> within) {
        if (t instanceof ParameterizedType p && p.getRawType() == List.class) return "Object.freeze([" + shape(p.getActualTypeArguments()[0], within) + "])";
        if (t instanceof Class<?> c && c.isRecord()) return record(c, within);
        if (t instanceof Class<?> c) return "\"" + jsType(c) + "\"";
        throw new IllegalArgumentException("a message's field is text, a number, a truth, a record of such, or a list of such - not " + t.getTypeName());
    }

    /** A record's fields' shapes, as one frozen object; a record already being read, refused - it would hold itself. */
    private static String record(Class<?> r, Deque<Class<?>> within) {
        if (within.contains(r)) throw new IllegalArgumentException("a message's record holds itself: " + r.getName());
        within.push(r);
        String fields = Arrays.stream(r.getRecordComponents()).map(c -> c.getName() + ": " + shape(c.getGenericType(), within)).collect(Collectors.joining(", "));
        within.pop();
        return "Object.freeze({" + (fields.isEmpty() ? "" : " " + fields + " ") + "})";
    }

    /** A scalar field's type as JavaScript's typeof says it: text, a number, or a truth. Anything else is refused. */
    static String jsType(Class<?> t) {
        if (t == String.class) return "string";
        if (t == boolean.class || t == Boolean.class) return "boolean";
        if (t == int.class || t == long.class || t == double.class || t == float.class || t == short.class || t == byte.class
                || Number.class.isAssignableFrom(t)) return "number";
        throw new IllegalArgumentException("a message's field is text, a number, a truth, a record of such, or a list of such - not " + t.getName());
    }
}
