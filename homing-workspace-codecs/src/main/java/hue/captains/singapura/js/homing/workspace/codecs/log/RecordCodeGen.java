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
 * The generators for a RECORD: an object on the wire, a member per component
 * in the component's order. A record that is a variant of a sealed type says
 * which, first, under {@code "type"} — so a value read back knows what it is —
 * and its JavaScript class extends the sealed type's, so {@code instanceof}
 * answers for the whole family.
 */
final class RecordCodeGen {

    private RecordCodeGen() {}

    /** The members on the wire, in order: the variant's name first when it is one. */
    private static List<String> keys(Class<?> t, List<LogShapes.Component> cs) {
        var keys = new ArrayList<String>();
        if (LogShapes.sealedParent(t) != null) keys.add("type");
        for (var c : cs) {
            if (c.name().equals("type") && !keys.isEmpty()) {
                throw new IllegalArgumentException(t.getName() + ": a variant's component may not be named \"type\" — the wire says the variant there");
            }
            keys.add(c.name());
        }
        return keys;
    }

    private static String jsList(List<String> keys) {
        var sb = new StringBuilder("[");
        for (int i = 0; i < keys.size(); i++) sb.append(i > 0 ? ", " : "").append(javaString(keys.get(i)));
        return sb.append("]").toString();
    }

    /** The JavaScript class. */
    record JsDefinition() implements DefinitionCodeGen, StatelessFunctionalObject {
        static final JsDefinition INSTANCE = new JsDefinition();

        @Override public String generate(ObjectDefinition<?> definition) {
            Class<?> t = definition.type();
            var cs = LogShapes.components(t);
            Class<?> parent = LogShapes.sealedParent(t);
            String n = simple(t);
            var sb = new StringBuilder();
            sb.append("/** Generated from ").append(t.getCanonicalName()).append(" — do not edit. */\n");
            sb.append("class ").append(n).append(parent != null ? " extends " + simple(parent) : "").append(" {\n");
            String constants = LogShapes.jsConstants(t);
            if (!constants.isEmpty()) sb.append(constants).append("\n");
            sb.append("    constructor(");
            for (int i = 0; i < cs.size(); i++) sb.append(i > 0 ? ", " : "").append(cs.get(i).name());
            sb.append(") {\n");
            if (parent != null) sb.append("        super();\n");
            for (var c : cs) {
                sb.append("        if (!(").append(LogSlot.jsCheck(c.slot(), c.name(), 0)).append(")) LogWire.no(")
                  .append(javaString(n + "." + c.name())).append(", \"expected ").append(LogSlot.describe(c.slot()))
                  .append(", got \" + LogWire.show(").append(c.name()).append("));\n");
            }
            for (var c : cs) {
                String v = c.slot() instanceof LogSlot.ListOf ? "Object.freeze(" + c.name() + ".slice())" : c.name();
                sb.append("        this.").append(c.name()).append(" = ").append(v).append(";\n");
            }
            sb.append("        Object.freeze(this);\n");
            sb.append("    }\n}\n");
            return sb.toString();
        }
    }

    /** The JavaScript codec. */
    record JsFunctions() implements FunctionsCodeGen, StatelessFunctionalObject {
        static final JsFunctions INSTANCE = new JsFunctions();

        @Override public String generate(ObjectDefinition<?> definition) {
            Class<?> t = definition.type();
            var cs = LogShapes.components(t);
            boolean variant = LogShapes.sealedParent(t) != null;
            String n = simple(t);
            var sb = new StringBuilder();
            sb.append("/** Generated from ").append(t.getCanonicalName()).append(" — do not edit. */\n");
            sb.append("class ").append(codec(t)).append(" {\n");
            sb.append("    static KEYS = Object.freeze(").append(jsList(keys(t, cs))).append(");\n\n");
            sb.append("    static transformTo(v) {\n");
            sb.append("        if (!(v instanceof ").append(n).append(")) LogWire.no(").append(javaString(codec(t) + ".transformTo"))
              .append(", \"expected a ").append(n).append(", got \" + LogWire.show(v));\n");
            sb.append("        return {");
            boolean first = true;
            if (variant) { sb.append(" \"type\": ").append(javaString(n)); first = false; }
            for (var c : cs) {
                sb.append(first ? " " : ", ").append(javaString(c.name())).append(": ").append(LogSlot.jsEncode(c.slot(), "v." + c.name(), 0));
                first = false;
            }
            sb.append(" };\n");
            sb.append("    }\n\n");
            sb.append("    static transformFrom(w) {\n");
            sb.append("        LogWire.object(w, ").append(javaString(n)).append(", ").append(codec(t)).append(".KEYS);\n");
            if (variant) {
                sb.append("        if (w.type !== ").append(javaString(n)).append(") LogWire.no(").append(javaString(n + ".type"))
                  .append(", \"expected \\\"").append(n).append("\\\", got \" + LogWire.show(w.type));\n");
            }
            sb.append("        return new ").append(n).append("(");
            for (int i = 0; i < cs.size(); i++) {
                var c = cs.get(i);
                sb.append(i > 0 ? ", " : "").append(LogSlot.jsDecode(c.slot(), "w." + c.name(), n + "." + c.name(), 0));
            }
            sb.append(");\n");
            sb.append("    }\n}\n");
            return sb.toString();
        }
    }

    /** The Java codec. */
    record JavaFunctions() implements FunctionsCodeGen, StatelessFunctionalObject {
        static final JavaFunctions INSTANCE = new JavaFunctions();

        @Override public String generate(ObjectDefinition<?> definition) {
            Class<?> t = definition.type();
            var cs = LogShapes.components(t);
            boolean variant = LogShapes.sealedParent(t) != null;
            String fqn = t.getCanonicalName(), n = simple(t);
            var keys = keys(t, cs);
            var sb = new StringBuilder(LogShapes.javaHead(t, fqn));
            sb.append("    private static final java.util.Set<String> KEYS = java.util.Set.of(");
            for (int i = 0; i < keys.size(); i++) sb.append(i > 0 ? ", " : "").append(javaString(keys.get(i)));
            sb.append(");\n\n");
            sb.append("    @Override public Json transformTo(").append(fqn).append(" v) {\n");
            sb.append("        var m = new java.util.LinkedHashMap<String, Json>();\n");
            if (variant) sb.append("        m.put(\"type\", Wire.string(").append(javaString(n)).append("));\n");
            for (var c : cs) {
                sb.append("        m.put(").append(javaString(c.name())).append(", ")
                  .append(LogSlot.javaEncode(c.slot(), "v." + c.name() + "()", 0)).append(");\n");
            }
            sb.append("        return new Json.Obj(m);\n");
            sb.append("    }\n\n");
            sb.append("    @Override public ").append(fqn).append(" transformFrom(Json w) {\n");
            sb.append("        var o = Wire.object(w, ").append(javaString(n)).append(", KEYS);\n");
            if (variant) {
                sb.append("        String type = Wire.string(o.get(\"type\"), ").append(javaString(n + ".type")).append(");\n");
                sb.append("        if (!type.equals(").append(javaString(n)).append(")) throw new Wire.Refused(")
                  .append(javaString(n + ".type")).append(", \"expected \\\"").append(n).append("\\\", got \\\"\" + type + \"\\\"\");\n");
            }
            sb.append("        return Wire.build(").append(javaString(n)).append(", () -> new ").append(fqn).append("(");
            for (int i = 0; i < cs.size(); i++) {
                var c = cs.get(i);
                sb.append(i > 0 ? ",\n                " : "\n                ")
                  .append(LogSlot.javaDecode(c.slot(), "o.get(" + javaString(c.name()) + ")", n + "." + c.name(), 0));
            }
            sb.append("));\n");
            sb.append("    }\n}\n");
            return sb.toString();
        }
    }
}
