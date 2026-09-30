package hue.captains.singapura.js.homing.workspace.content;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.parties.PartyType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The content-party pattern: every content type's party speaks the same six kinds, and only
 * its content differs. A widget says what it wants; the party answers from what it holds, or
 * asks the steward of its hierarchy, once for many askers, and answers when it is told.
 *
 * <table>
 *   <caption>The kinds</caption>
 *   <tr><th>Kind</th><th>From</th><th>To</th><th>Fields</th></tr>
 *   <tr><td>Wanted</td><td>a widget</td><td>the party</td><td>{@code List<Param> params}</td></tr>
 *   <tr><td>Fetch</td><td>the party</td><td>its steward, or the party above</td><td>{@code List<Item> items}</td></tr>
 *   <tr><td>Loaded</td><td>the steward, or the party above</td><td>the party</td><td>{@code List<Param> params, C content}</td></tr>
 *   <tr><td>Failed</td><td>the steward, or the party above</td><td>the party</td><td>{@code List<Param> params, String why}</td></tr>
 *   <tr><td>Content</td><td>the party</td><td>the widgets that asked</td><td>{@code List<Param> params, C content}</td></tr>
 *   <tr><td>Unavailable</td><td>the party</td><td>the widgets that asked</td><td>{@code List<Param> params, String why}</td></tr>
 * </table>
 *
 * <p>A content type declares its vocabulary as a sealed interface of those six records, its
 * content one record of its own, the same in {@code Loaded} and {@code Content}; {@link #type}
 * holds it to the pattern and gives it the one secretary every content party has, at every
 * level: {@code ContentSecretary}. Its steward is its own - or its host's.</p>
 */
public final class ContentParty {

    private ContentParty() {}

    /** The kinds of the pattern, in the order they are told. */
    public static final List<String> KINDS = List.of("Wanted", "Fetch", "Loaded", "Failed", "Content", "Unavailable");

    /** The one secretary of every content party. */
    public static final ModuleImports<ContentSecretaryModule> SECRETARY =
            new ModuleImports<>(List.of(new ContentSecretaryModule.ContentSecretary()), ContentSecretaryModule.INSTANCE);

    /**
     * A content type's party type: the vocabulary held to the pattern, and the content secretary
     * its default. Refused, saying why, when a kind is missing, extra, or of other fields.
     */
    public static <M> PartyType<M> type(String name, Class<M> vocabulary) {
        PartyType<M> t = new PartyType<>(name, vocabulary);   // a party type first, by its own checks
        Map<String, Class<?>> kinds = new LinkedHashMap<>();
        for (Class<?> k : t.kinds()) kinds.put(k.getSimpleName(), k);
        if (!kinds.keySet().equals(Set.copyOf(KINDS))) {
            throw new IllegalArgumentException("the content type '" + name + "' speaks " + kinds.keySet() + " - a content party speaks " + KINDS);
        }
        var problems = new ArrayList<String>();
        fields(kinds.get("Wanted"), problems, "params", list(Param.class));
        fields(kinds.get("Fetch"), problems, "items", list(Item.class));
        fields(kinds.get("Failed"), problems, "params", list(Param.class), "why", String.class);
        fields(kinds.get("Unavailable"), problems, "params", list(Param.class), "why", String.class);
        Class<?> loaded = content(kinds.get("Loaded"), problems);
        Class<?> told = content(kinds.get("Content"), problems);
        if (loaded != null && told != null && loaded != told) {
            problems.add("Loaded carries " + loaded.getSimpleName() + " and Content " + told.getSimpleName() + " - one content, the same in both");
        }
        if (!problems.isEmpty()) throw new IllegalArgumentException("the content type '" + name + "' is not of the pattern: " + String.join("; ", problems));
        return t.withSecretary(SECRETARY);
    }

    /** The shape of a list of one element type, to compare with a field's generic type. */
    private static String list(Class<?> of) { return List.class.getName() + "<" + of.getName() + ">"; }

    /** A kind's fields, exactly: names and types, in order - a type given as a class, or as a list's name. */
    private static void fields(Class<?> kind, List<String> problems, Object... expected) {
        RecordComponent[] cs = kind.getRecordComponents();
        List<String> want = new ArrayList<>(), got = new ArrayList<>();
        for (int i = 0; i < expected.length; i += 2) want.add(expected[i] + ": " + typeName(expected[i + 1]));
        for (RecordComponent c : cs) got.add(c.getName() + ": " + typeName(c.getGenericType()));
        if (!want.equals(got)) problems.add(kind.getSimpleName() + " has " + got + ", not " + want);
    }

    /** A kind carrying content: params, then the content - a record; → the content's record, or null when refused. */
    private static Class<?> content(Class<?> kind, List<String> problems) {
        RecordComponent[] cs = kind.getRecordComponents();
        if (cs.length != 2 || !cs[0].getName().equals("params") || !typeName(cs[0].getGenericType()).equals(list(Param.class))
                || !cs[1].getName().equals("content") || !(cs[1].getGenericType() instanceof Class<?> c) || !c.isRecord()) {
            problems.add(kind.getSimpleName() + " has " + Arrays.stream(cs).map(x -> x.getName() + ": " + typeName(x.getGenericType())).toList()
                    + ", not [params: " + list(Param.class) + ", content: a record]");
            return null;
        }
        return (Class<?>) cs[1].getGenericType();
    }

    private static String typeName(Object t) {
        if (t instanceof String s) return s;
        if (t instanceof ParameterizedType p) return p.getRawType().getTypeName() + "<" + p.getActualTypeArguments()[0].getTypeName() + ">";
        return ((Type) t).getTypeName();
    }
}
