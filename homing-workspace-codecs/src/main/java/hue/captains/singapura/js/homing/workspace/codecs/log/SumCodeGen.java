package hue.captains.singapura.js.homing.workspace.codecs.log;

import hue.captains.singapura.js.homing.codec.DefinitionCodeGen;
import hue.captains.singapura.js.homing.codec.FunctionsCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;

import static hue.captains.singapura.js.homing.workspace.codecs.log.LogShapes.codec;
import static hue.captains.singapura.js.homing.workspace.codecs.log.LogShapes.simple;
import static hue.captains.singapura.js.homing.workspace.codecs.log.LogSlot.javaString;

/**
 * The generators for a SEALED type: the family of its variants, each a record
 * of its own, told apart on the wire by the {@code "type"} each writes first.
 * In JavaScript it is the class its variants extend, and cannot be made itself;
 * its codec hands a value to its variant's. In Java the codec switches over the
 * sealed type, so a variant added and not given a codec does not compile.
 */
final class SumCodeGen {

    private SumCodeGen() {}

    static List<Class<?>> variants(Class<?> t) {
        if (!t.isInterface() || !t.isSealed()) throw new IllegalArgumentException(t.getName() + " is not a sealed interface");
        var vs = List.of(t.getPermittedSubclasses());
        for (Class<?> v : vs) {
            if (!v.isRecord()) throw new IllegalArgumentException(t.getName() + ": its variant " + v.getName() + " is not a record");
        }
        return vs;
    }

    /** The JavaScript class the variants extend. */
    record JsDefinition() implements DefinitionCodeGen, StatelessFunctionalObject {
        static final JsDefinition INSTANCE = new JsDefinition();

        @Override public String generate(ObjectDefinition<?> definition) {
            Class<?> t = definition.type();
            var vs = variants(t);
            String n = simple(t);
            var sb = new StringBuilder();
            sb.append("/** Generated from ").append(t.getName()).append(" — do not edit. Sealed: its variants are its only kinds. */\n");
            sb.append("class ").append(n).append(" {\n");
            sb.append("    static VARIANTS = Object.freeze([");
            for (int i = 0; i < vs.size(); i++) sb.append(i > 0 ? ", " : "").append(javaString(simple(vs.get(i))));
            sb.append("]);\n\n");
            sb.append("    constructor() {\n");
            sb.append("        if (new.target === ").append(n).append(") LogWire.no(").append(javaString(n))
              .append(", \"sealed: make one of its variants\");\n");
            sb.append("    }\n}\n");
            return sb.toString();
        }
    }

    /** The JavaScript codec: a value to its variant's codec, and back by its type. */
    record JsFunctions() implements FunctionsCodeGen, StatelessFunctionalObject {
        static final JsFunctions INSTANCE = new JsFunctions();

        @Override public String generate(ObjectDefinition<?> definition) {
            Class<?> t = definition.type();
            var vs = variants(t);
            String n = simple(t);
            var sb = new StringBuilder();
            sb.append("/** Generated from ").append(t.getName()).append(" — do not edit. */\n");
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
                sb.append("        if (type === ").append(javaString(simple(v))).append(") return ").append(codec(v)).append(".transformFrom(w);\n");
            }
            sb.append("        LogWire.no(").append(javaString(n + ".type")).append(", \"no variant \" + LogWire.show(type));\n");
            sb.append("    }\n}\n");
            return sb.toString();
        }
    }

    /** The Java codec: an exhaustive switch over the sealed type. */
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
                sb.append("            case ").append(javaString(simple(v))).append(" -> ").append(codec(v)).append(".INSTANCE.transformFrom(w);\n");
            }
            sb.append("            default -> throw new Wire.Refused(").append(javaString(n + ".type")).append(", \"no variant \\\"\" + type + \"\\\"\");\n");
            sb.append("        };\n");
            sb.append("    }\n}\n");
            return sb.toString();
        }
    }
}
