package hue.captains.singapura.js.homing.workspace.parties;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * One instance of a messaging party: {@code new MessagingParty(type,
 * secretary)} - flat, one secretary and its members, of one type; members
 * join, tell, and hear the kinds they have reactors for; every message is
 * checked against the type where it enters. Headless: it touches no DOM.
 */
public record MessagingPartyModule() implements EsModule<MessagingPartyModule> {

    public record MessagingParty() implements Exportable._Class<MessagingPartyModule> {}

    public static final MessagingPartyModule INSTANCE = new MessagingPartyModule();

    @Override public ImportsFor<MessagingPartyModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<MessagingPartyModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new MessagingParty())); }
}
