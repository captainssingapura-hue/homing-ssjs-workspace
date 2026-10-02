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
 * The generators for an ENUM: its constant's name on the wire. In JavaScript
 * each constant is one frozen instance, a static of the class, so a value is
 * checked by {@code instanceof} like any other of the log's types.
 */
final class EnumCodeGen {

    private EnumCodeGen() {}

    static List<String> names(Class<?> t) {
        if (!t.isEnum()) throw new IllegalArgumentException(t.getName() + " is not an enum");
        var out = new ArrayList<String>();
        for (Object c : t.getEnumConstants()) out.add(((Enum<?>) c).name());
        return out;
    }

    /** The JavaScript class, its constants its statics. */
    record JsDefinition() implements DefinitionCodeGen, StatelessFunctionalObject {
        static final JsDefinition INSTANCE = new JsDefinition();

        @Override public String generate(ObjectDefinition<?> definition) {
            Class<?> t = definition.type();
            var names = names(t);
            String n = simple(t);
            var sb = new StringBuilder();
            sb.append("/** Generated from ").append(t.getCanonicalName()).append(" — do not edit. */\n");
            sb.append("class ").append(n).append(" {\n");
            sb.append("    static NAMES = Object.freeze([");
            for (int i = 0; i < names.size(); i++) sb.append(i > 0 ? ", " : "").append(javaString(names.get(i)));
            sb.append("]);\n\n");
            sb.append("    constructor(name) {\n");
            sb.append("        if (!").append(n).append(".NAMES.includes(name) || ").append(n).append("[name]) LogWire.no(")
              .append(javaString(n)).append(", \"no constant \" + LogWire.show(name));\n");
            sb.append("        this.name = name;\n");
            sb.append("        Object.freeze(this);\n");
            sb.append("    }\n\n");
            for (String c : names) sb.append("    static ").append(c).append(" = new ").append(n).append("(").append(javaString(c)).append(");\n");
            sb.append("\n    static of(name) {\n");
            sb.append("        if (typeof name !== \"string\" || !").append(n).append(".NAMES.includes(name)) LogWire.no(")
              .append(javaString(n)).append(", \"no constant \" + LogWire.show(name));\n");
            sb.append("        return ").append(n).append("[name];\n");
            sb.append("    }\n}\n");
            return sb.toString();
        }
    }

    /** The JavaScript codec. */
    record JsFunctions() implements FunctionsCodeGen, StatelessFunctionalObject {
        static final JsFunctions INSTANCE = new JsFunctions();

        @Override public String generate(ObjectDefinition<?> definition) {
            Class<?> t = definition.type();
            String n = simple(t);
            return "/** Generated from " + t.getCanonicalName() + " — do not edit. */\n"
                 + "class " + codec(t) + " {\n"
                 + "    static transformTo(v) {\n"
                 + "        if (!(v instanceof " + n + ")) LogWire.no(" + javaString(codec(t) + ".transformTo") + ", \"expected a " + n + ", got \" + LogWire.show(v));\n"
                 + "        return v.name;\n"
                 + "    }\n\n"
                 + "    static transformFrom(w) { return " + n + ".of(w); }\n"
                 + "}\n";
        }
    }

    /** The Java codec. */
    record JavaFunctions() implements FunctionsCodeGen, StatelessFunctionalObject {
        static final JavaFunctions INSTANCE = new JavaFunctions();

        @Override public String generate(ObjectDefinition<?> definition) {
            Class<?> t = definition.type();
            names(t);
            String fqn = t.getCanonicalName(), n = simple(t);
            return LogShapes.javaHead(t, fqn)
                 + "    @Override public Json transformTo(" + fqn + " v) {\n"
                 + "        return Wire.string(v.name());\n"
                 + "    }\n\n"
                 + "    @Override public " + fqn + " transformFrom(Json w) {\n"
                 + "        String name = Wire.string(w, " + javaString(n) + ");\n"
                 + "        try { return " + fqn + ".valueOf(name); }\n"
                 + "        catch (IllegalArgumentException e) { throw new Wire.Refused(" + javaString(n) + ", \"no constant \\\"\" + name + \"\\\"\"); }\n"
                 + "    }\n"
                 + "}\n";
        }
    }
}
