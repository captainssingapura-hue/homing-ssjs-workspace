// =============================================================================
// KindAndParamsTitle — how a widget is named for the eye unless a page says
// otherwise: a display rule, a stateless functional object, apart from what an
// id is (the id says which widget; this says what it is called). Its kind's
// title; then, when it was made with params, their concise form after a middle
// dot; then its sequence, when it is not the first of its prefix —
// "Books grid", "Books grid 2", "Books grid · title-rating",
// "Books grid · title-rating 2". Every placement asks the same rule, so a
// widget is called the same wherever it is shown.
//
// The dual of KindAndParamsTitle.java, word for word; the two agree
// (WidgetTitleRuleTest).
//
//   KindAndParamsTitle.INSTANCE.title(kindTitle, id) → the widget's title as it opens
//   KindAndParamsTitle.INSTANCE.titleOf(kindTitle, id, name) → its title now: the name a user
//                gave it (a string), when one is — else as it opened; the derived title is only the first name
//     kindTitle  its kind's title, as its declaration says it (the manifest's kinds[kind].title)
//     id         its id, as the core gave it
// =============================================================================

class KindAndParamsTitle {
    title(kindTitle, id) {
        if (typeof kindTitle !== "string" || kindTitle === "") throw new Error("[KindAndParamsTitle] a kind's title is required, for " + id);
        var s = WidgetIds.split(id);
        if (!s) throw new Error("[KindAndParamsTitle] '" + id + "' is not a widget's id");
        var concise = WidgetIds.conciseOf(id);
        return kindTitle + (concise ? " · " + concise : "") + (s.n > 1 ? " " + s.n : "");
    }

    titleOf(kindTitle, id, name) {
        return name != null ? String(name) : this.title(kindTitle, id);
    }
}

/** The one there is: it holds nothing. */
KindAndParamsTitle.INSTANCE = Object.freeze(new KindAndParamsTitle());
