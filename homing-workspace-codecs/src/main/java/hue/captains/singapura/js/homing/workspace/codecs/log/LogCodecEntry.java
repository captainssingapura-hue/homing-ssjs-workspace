package hue.captains.singapura.js.homing.workspace.codecs.log;

import hue.captains.singapura.js.homing.codec.DefinitionCodeGen;
import hue.captains.singapura.js.homing.codec.FunctionsCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.Objects;

/**
 * One row of the workspace log's manifest: a type, and the functions that
 * write it — its JavaScript class, its JavaScript codec, its Java codec. The
 * generators are the type's own, chosen here per type; the four shapes below
 * are the ones the log's types come in.
 *
 * @param type          the Java type: the declaration everything else is generated from
 * @param jsDefinition  writes its JavaScript class
 * @param jsFunctions   writes its JavaScript codec
 * @param javaFunctions writes its Java codec
 */
public record LogCodecEntry<T>(Class<T> type, DefinitionCodeGen jsDefinition,
                               FunctionsCodeGen jsFunctions, FunctionsCodeGen javaFunctions)
        implements StatelessFunctionalObject {

    public LogCodecEntry {
        Objects.requireNonNull(type, "LogCodecEntry.type");
        Objects.requireNonNull(jsDefinition, "LogCodecEntry.jsDefinition");
        Objects.requireNonNull(jsFunctions, "LogCodecEntry.jsFunctions");
        Objects.requireNonNull(javaFunctions, "LogCodecEntry.javaFunctions");
    }

    public ObjectDefinition<T> definition() { return ObjectDefinition.of(type); }

    /** A record of one scalar, the scalar alone on the wire. */
    public static <T> LogCodecEntry<T> id(Class<T> type) {
        return new LogCodecEntry<>(type, IdCodeGen.JsDefinition.INSTANCE, IdCodeGen.JsFunctions.INSTANCE, IdCodeGen.JavaFunctions.INSTANCE);
    }

    /** A record, an object on the wire; a variant says its name first. */
    public static <T> LogCodecEntry<T> record(Class<T> type) {
        return new LogCodecEntry<>(type, RecordCodeGen.JsDefinition.INSTANCE, RecordCodeGen.JsFunctions.INSTANCE, RecordCodeGen.JavaFunctions.INSTANCE);
    }

    /** A sealed interface of records, told apart by their names. */
    public static <T> LogCodecEntry<T> sealed(Class<T> type) {
        return new LogCodecEntry<>(type, SumCodeGen.JsDefinition.INSTANCE, SumCodeGen.JsFunctions.INSTANCE, SumCodeGen.JavaFunctions.INSTANCE);
    }

    /** An enum, its constant's name on the wire. */
    public static <T> LogCodecEntry<T> enumeration(Class<T> type) {
        return new LogCodecEntry<>(type, EnumCodeGen.JsDefinition.INSTANCE, EnumCodeGen.JsFunctions.INSTANCE, EnumCodeGen.JavaFunctions.INSTANCE);
    }
}
