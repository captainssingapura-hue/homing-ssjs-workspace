// =============================================================================
// NoteCard — a note of the content bench's: it knows the params it was made
// with, and nothing else. Joined, it asks the note party for its note with
// them, and shows it when it comes - its title and its text - or says it is
// unavailable, and why. It never learns where it sits, nor who fetched it.
// It flows: as tall as its content.
//
//   new NoteCard(container, params)   params: { key }
//   card.root  card.roots   { dom, focus }
//   card.join(given)   given: { [party type name]: party }   card.leave()
//   card.state() → "alone" | "waiting" | "shown" | "unavailable" - on the root as data-state too
//   card.dispose()
// =============================================================================

const _noteCardOwner = Object.freeze({ toString: () => "noteCard" });
var _noteCards = 0;

class NoteCard {

    /** It takes the height its content needs. */
    static SIZING = "flow";

    constructor(container, params) {
        if (!container || typeof container.appendChild !== "function") throw new Error("[NoteCard] a container is required: the one its host lends it");
        var name = "noteCard-" + (++_noteCards);
        this._params = ContentParams.of(params);
        this._dom = domOpsParties.mobile(name);
        this._dom.activate(_noteCardOwner);
        this._focusParty = focusParties.mobile(name);
        var root = this._dom.createElement("root", "article");
        css.addClass(root, cb_card);
        var key = this._dom.createElement("key", "p");
        css.addClass(key, cb_key);
        key.textContent = ContentParams.key(this._params);
        this._title = this._dom.createElement("title", "p");
        css.addClass(this._title, cb_title);
        this._text = this._dom.createElement("text", "p");
        css.addClass(this._text, cb_text);
        root.appendChild(key);
        root.appendChild(this._title);
        root.appendChild(this._text);
        container.appendChild(root);
        this.root = root;
        this.roots = Object.freeze({ dom: this._dom, focus: this._focusParty });
        this._member = null;
        this._at("alone", "", "Not asked for: no note party here.");
    }

    /** Joined: its note asked for, with its params. */
    join(given) {
        if (this._member) throw new Error("[NoteCard] joined already: leave first");
        var party = given && given[BENCH_NOTE.name], self = this;
        if (!party) return;
        this._member = party.join("noteCard", {
            Content: function (m) { if (ContentParams.same(m.params, self._params)) self._at("shown", m.content.title, m.content.text); },
            Unavailable: function (m) { if (ContentParams.same(m.params, self._params)) self._at("unavailable", "", "Unavailable: " + m.why); }
        });
        if (this._state === "alone") this._at("waiting", "", "Waiting for its note.");
        this._member.tell({ kind: "Wanted", params: this._params });
    }

    leave() { if (this._member) { this._member.leave(); this._member = null; } }

    state() { return this._state; }

    dispose() {
        this.leave();
        this._focusParty.dissolve();
        this._dom.dissolve();
    }

    _at(state, title, text) {
        this._state = state;
        this.root.setAttribute("data-state", state);
        this._title.textContent = title;
        this._text.textContent = text;
        css.toggleClass(this._text, cb_waiting, state !== "shown");
    }
}
