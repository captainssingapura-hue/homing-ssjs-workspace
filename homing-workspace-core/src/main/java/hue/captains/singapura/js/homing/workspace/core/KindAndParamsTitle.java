package hue.captains.singapura.js.homing.workspace.core;

import hue.captains.singapura.js.homing.workspace.core.models.WidgetId;

import java.util.Objects;

/**
 * The title every placement gives a widget unless told otherwise: its kind's
 * title; then, when it was made with params, their concise form after a middle
 * dot; then its sequence, when it is not the first of its prefix -
 * {@code Books grid}, {@code Books grid 2}, {@code Books grid · title-rating},
 * {@code Books grid · title-rating 2}. Unique in a workspace whenever its
 * kinds' titles are, as its ids are. The JavaScript is this, word for word
 * (KindAndParamsTitleModule.js), and the two agree (WidgetTitleRuleTest).
 */
public record KindAndParamsTitle() implements WidgetTitleRule {

    public static final KindAndParamsTitle INSTANCE = new KindAndParamsTitle();

    @Override
    public String title(String kindTitle, WidgetId id) {
        Objects.requireNonNull(id, "id");
        if (kindTitle == null || kindTitle.isEmpty()) throw new IllegalArgumentException("[KindAndParamsTitle] a kind's title is required, for " + id);
        String concise = WidgetIds.conciseOf(id);
        int n = id.sequence();
        return kindTitle + (concise.isEmpty() ? "" : " · " + concise) + (n > 1 ? " " + n : "");
    }
}
