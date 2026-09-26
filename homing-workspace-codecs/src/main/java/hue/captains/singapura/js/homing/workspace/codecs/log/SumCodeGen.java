package hue.captains.singapura.js.homing.workspace.codecs.log;

import hue.captains.singapura.js.homing.codec.DefinitionCodeGen;
import hue.captains.singapura.js.homing.codec.FunctionsCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.List;

import static hue.captains.singapura.js.homing.workspace.codecs.log.LogShapes.codec;
import static hue.captains.singapura.js.homing.workspace.codecs.log.LogShapes.simple;
import static hue.captains.singapura.js.homing.workspace.codecs.log.LogSlot.javaString;

/**
 * The generators for a SEALED type, which comes in two forms.
 *
 * <p>A family of RECORDS — its variants, told apart on the wire by the
 * {@code "type"} each writes first. In JavaScript it is the class its variants
 * extend, and cannot be made itself; its codec hands a value to its variant's.
 * Its variants are declared in its own file, so they are in its own module.</p>
 *
 * <p>A family of FAMILIES — sealed interfaces of their own, each a file and a
 * module of its own. On the wire it adds nothing: a value says only its
 * variant's name, and the family it falls in is read off that name. In
 * JavaScript nothing extends it — its families' modules would import it, and it
 * imports them to hand a value on — so {@code instanceof} asks the families
 * instead, and every import runs one way.</p>
 *
 * <p>In Java the codec switches over the sealed type, so a variant added and
 * not given a codec does not compile. A family mixing the two is refused.</p>
 */
final class SumCodeGen {

    private SumCodeGen() {}

    /** Its members: all records, or all sealed interfaces. */
    static List<Class<?>> variants(Class<?> t) {
        if (!t.isInterface() || !t.isSealed()) throw new IllegalArgumentException(t.getName() + " is not a sealed interface");
        var vs = List.of(t.getPermittedSubclasses());
        boolean families = vs.stream().allMatch(SumCodeGen::isFamily);
        boolean records = vs.stream().allMatch(Class::isRecord);
        if (!families && !records) {
            throw new IllegalArgumentException(t.getName() + ": its members are records or sealed interfaces, all one or all the other");
        }
        return vs;
    }

    static boolean isFamily(Class<?> c) { return c.isInterface() && c.isSealed(); }

    /** Whether this sealed type's members are families of their own. */
    static boolean ofFamilies(Class<?> t) { return variants(t).stream().allMatch(SumCodeGen::isFamily); }

    /** The names a member answers to on the wire: a record's own; a family's, each of its variants'. */
    static List<String> wireNames(Class<?> member) {
        if (member.isRecord()) return List.of(simple(member));
        var out = new ArrayList<String>();
        for (Class<?> v : variants(member)) out.addAll(wireNames(v));
        return out;
    }

    private static String jsList(List<String> names) {
        var sb = new StringBuilder("[");
        for (int i = 0; i < names.size(); i++) sb.append(i > 0 ? ", " : "").append(javaString(names.get(i)));
        return sb.append("]").toString();
    }

    /** The JavaScript class: the one its variants extend, or the one that asks its families. */
    record JsDefinition() implements DefinitionCodeGen, StatelessFunctionalObject {
        static final JsDefinition INSTANCE = new JsDefinition();

        @Override public String generate(ObjectDefinition<?> definition) {
            Class<?> t = definition.type();
            var vs = variants(t);
            String n = simple(t);
            var sb = new StringBuilder();
            if (ofFamilies(t)) {
                sb.append("/** Generated from ").append(t.getCanonicalName()).append(" — do not edit. Sealed: a family of families, none of which extends it. */\n");
                sb.append("class ").append(n).append(" {\n");
                sb.append("    static VARIANTS = Object.freeze(").append(jsList(wireNames(t))).append(");\n\n");
                sb.append("    static [Symbol.hasInstance](v) {\n");
                sb.append("        return ");
                for (int i = 0; i < vs.size(); i++) sb.append(i > 0 ? " || " : "").append("v instanceof ").append(simple(vs.get(i)));
                sb.append(";\n    }\n\n");
                sb.append("    constructor() {\n");
                sb.append("        LogWire.no(").append(javaString(n)).append(", \"sealed: make one of its variants\");\n");
                sb.append("    }\n}\n");
                return sb.toString();
            }
            sb.append("/** Generated from ").append(t.getCanonicalName()).append(" — do not edit. Sealed: its variants are its only kinds. */\n");
            sb.append("class ").append(n).append(" {\n");
            sb.append("    static VARIANTS = Object.freeze(").append(jsList(wireNames(t))).append(");\n\n");
            sb.append("    constructor() {\n");
            sb.append("        if (new.target === ").append(n).append(") LogWire.no(").append(javaString(n))
              .append(", \"sealed: make one of its variants\");\n");
            sb.append("    }\n}\n");
            return sb.toString();
        }
    }

    /** The JavaScript codec: a value to its member's codec, and back by its type. */
    record JsFunctions() implements FunctionsCodeGen, StatelessFunctionalObject {
        static final JsFunctions INSTANCE = new JsFunctions();

        @Override public String generate(ObjectDefinition<?> definition) {
            Class<?> t = definition.type();
            var vs = variants(t);
            boolean families = ofFamilies(t);
            String n = simple(t);
            var sb = new StringBuilder();
            sb.append("/** Generated from ").append(t.getCanonicalName()).append(" — do not edit. */\n");
            sb.append("class ").append(codec(t)).append(" {\n");
            sb.append("    static transformTo(v) {\n");
            for (Class<?> v : vs) {
                sb.append("        if (v instanceof ").append(simple(v)).append(") return ").append(codec(v)).append(".transformTo(v);\n");
            }
            sb.append("        LogWire.no(").append(javaString(codec(t) + ".transformTo")).append(", \"expected a ").append(n)
              .append(", got \" + LogWire.show(v));\n");
            sb.append("    }\n\n");
            sb.append("    static transformFrom(w) {\n");
            sb.append("        const type = LogWire.type(w, ").append(javaString(n)).append(");\n");
            for (Class<?> v : vs) {
                String test = families ? simple(v) + ".VARIANTS.includes(type)" : "type === " + javaString(simple(v));
                sb.append("        if (").append(test).append(") return ").append(codec(v)).append(".transformFrom(w);\n");
            }
            sb.append("        LogWire.no(").append(javaString(n + ".type")).append(", \"no variant \" + LogWire.show(type));\n");
            sb.append("    }\n}\n");
            return sb.toString();
        }
    }

    /** The Java codec: an exhaustive switch over the sealed type, and over the names on the wire. */
    record JavaFunctions() implements FunctionsCodeGen, StatelessFunctionalObject {
        static final JavaFunctions INSTANCE = new JavaFunctions();

        @Override public String generate(ObjectDefinition<?> definition) {
            Class<?> t = definition.type();
            var vs = variants(t);
            String fqn = t.getCanonicalName(), n = simple(t);
            var sb = new StringBuilder(LogShapes.javaHead(t, fqn));
            sb.append("    @Override public Json transformTo(").append(fqn).append(" v) {\n");
            sb.append("        return switch (v) {\n");
            for (Class<?> v : vs) {
                sb.append("            case ").append(v.getCanonicalName()).append(" x -> ").append(codec(v)).append(".INSTANCE.transformTo(x);\n");
            }
            sb.append("        };\n");
            sb.append("    }\n\n");
            sb.append("    @Override public ").append(fqn).append(" transformFrom(Json w) {\n");
            sb.append("        String type = Wire.type(w, ").append(javaString(n)).append(");\n");
            sb.append("        return switch (type) {\n");
            for (Class<?> v : vs) {
                var names = wireNames(v);
                sb.append("            case ");
                for (int i = 0; i < names.size(); i++) sb.append(i > 0 ? ", " : "").append(javaString(names.get(i)));
                sb.append(" -> ").append(codec(v)).append(".INSTANCE.transformFrom(w);\n");
            }
            sb.append("            default -> throw new Wire.Refused(").append(javaString(n + ".type")).append(", \"no variant \\\"\" + type + \"\\\"\");\n");
            sb.append("        };\n");
            sb.append("    }\n}\n");
            return sb.toString();
        }
    }
}
