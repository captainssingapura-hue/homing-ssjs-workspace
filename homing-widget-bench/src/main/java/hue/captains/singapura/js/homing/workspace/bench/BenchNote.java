package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.content.ContentParty;
import hue.captains.singapura.js.homing.workspace.content.Item;
import hue.captains.singapura.js.homing.workspace.content.Param;
import hue.captains.singapura.js.homing.workspace.parties.PartyType;

import java.util.List;

/**
 * The bench's notes, a content type of the pattern: a note is a title and a text, fetched from
 * the bench's server by its key. What the content bench's cards ask for.
 */
public sealed interface BenchNote {

    /** A note. */
    record Note(String title, String text) {}

    record Wanted(List<Param> params) implements BenchNote {}
    record Fetch(List<Item> items) implements BenchNote {}
    record Loaded(List<Param> params, Note content) implements BenchNote {}
    record Failed(List<Param> params, String why) implements BenchNote {}
    record Content(List<Param> params, Note content) implements BenchNote {}
    record Unavailable(List<Param> params, String why) implements BenchNote {}

    /** The type: {@code bench-note}, served as {@code BENCH_NOTE}; its steward the bench's, which fetches a note by its key. */
    PartyType<BenchNote> TYPE = ContentParty.type("bench-note", BenchNote.class)
            .servedFrom(new ModuleImports<>(List.of(new BenchNoteModule.BENCH_NOTE()), BenchNoteModule.INSTANCE))
            .withSteward(new ModuleImports<>(List.of(new BenchNoteStewardModule.BenchNoteSteward()), BenchNoteStewardModule.INSTANCE));
}
