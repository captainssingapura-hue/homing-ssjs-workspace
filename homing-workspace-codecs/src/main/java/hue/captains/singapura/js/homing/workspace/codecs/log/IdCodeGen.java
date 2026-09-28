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
 * The generators for an IDENTIFIER: a record of one scalar — a string, a
 * UUID, an integer — written on the wire as that scalar alone, {@code "tab-3"}
 * rather than {@code {"value":"tab-3"}}. Its JavaScript class checks the
 * scalar, and the grammar the Java record declares, as the record's own
 * constructor does.
 */
final class IdCodeGen {

    private IdCodeGen() {}

    private static LogShapes.Component only(Class<?> t) {
        List<LogShapes.Component> cs = LogShapes.components(t);
        if (cs.size() != 1) throw new IllegalArgumentException(t.getName() + " is not an identifier: it has " + cs.size() + " components");
        var c = cs.get(0);
        if (c.slot() instanceof LogSlot.Typed || c.slot() instanceof LogSlot.ListOf || c.slot() instanceof LogSlot.Opt) {
            throw new IllegalArgumentException(t.getName() + " is not an identifier: its one component is not a scalar");
        }
        return c;
    }

    /** The JavaScript class. */
    record JsDefinition() implements DefinitionCodeGen, StatelessFunctionalObject {
        static final JsDefinition INSTANCE = new JsDefinition();

        @Override public String generate(ObjectDefinition<?> definition) {
            Class<?> t = definition.type();
            var c = only(t);
            String n = simple(t), f = c.name(), grammar = LogShapes.grammar(t);
            var sb = new StringBuilder();
            sb.append("/** Generated from ").append(t.getCanonicalName()).append(" — do not edit. */\n");
            sb.append("class ").append(n).append(" {\n");
            String constants = LogShapes.jsConstants(t);
            if (!constants.isEmpty()) sb.append(constants).append("\n");
            if (grammar != null) {
                sb.append("    static GRAMMAR = new RegExp(").append(javaString("^(?:" + grammar + ")$")).append(");\n\n");
            }
            sb.append("    constructor(").append(f).append(") {\n");
            sb.append("        if (!(").append(LogSlot.jsCheck(c.slot(), f, 0)).append(")) LogWire.no(")
              .append(javaString(n + "." + f)).append(", \"expected ").append(LogSlot.describe(c.slot()))
              .append(", got \" + LogWire.show(").append(f).append("));\n");
            if (grammar != null) {
                sb.append("        if (!").append(n).append(".GRAMMAR.test(").append(f).append(")) LogWire.no(")
                  .append(javaString(n + "." + f)).append(", LogWire.show(").append(f).append(") + ")
                  .append(javaString(" does not match " + grammar)).append(");\n");
            }
            sb.append("        this.").append(f).append(" = ").append(f).append(";\n");
            sb.append("        Object.freeze(this);\n");
            sb.append("    }\n}\n");
            return sb.toString();
        }
    }

    /** The JavaScript codec: the scalar itself on the wire. */
    record JsFunctions() implements FunctionsCodeGen, StatelessFunctionalObject {
        static final JsFunctions INSTANCE = new JsFunctions();

        @Override public String generate(ObjectDefinition<?> definition) {
            Class<?> t = definition.type();
            var c = only(t);
            String n = simple(t);
            return "/** Generated from " + t.getCanonicalName() + " — do not edit. */\n"
                 + "class " + codec(t) + " {\n"
                 + "    static transformTo(v) {\n"
                 + "        if (!(v instanceof " + n + ")) LogWire.no(" + javaString(codec(t) + ".transformTo") + ", \"expected a " + n + ", got \" + LogWire.show(v));\n"
                 + "        return v." + c.name() + ";\n"
                 + "    }\n\n"
                 + "    static transformFrom(w) { return new " + n + "(w); }\n"
                 + "}\n";
        }
    }

    /** The Java codec. */
    record JavaFunctions() implements FunctionsCodeGen, StatelessFunctionalObject {
        static final JavaFunctions INSTANCE = new JavaFunctions();

        @Override public String generate(ObjectDefinition<?> definition) {
            Class<?> t = definition.type();
            var c = only(t);
            String fqn = t.getCanonicalName(), n = simple(t);
            return LogShapes.javaHead(t, fqn)
                 + "    @Override public Json transformTo(" + fqn + " v) {\n"
                 + "        return " + LogSlot.javaEncode(c.slot(), "v." + c.name() + "()", 0) + ";\n"
                 + "    }\n\n"
                 + "    @Override public " + fqn + " transformFrom(Json w) {\n"
                 + "        return Wire.build(" + javaString(n) + ", () -> new " + fqn + "("
                 + LogSlot.javaDecode(c.slot(), "w", n, 0) + "));\n"
                 + "    }\n"
                 + "}\n";
        }
    }
}
