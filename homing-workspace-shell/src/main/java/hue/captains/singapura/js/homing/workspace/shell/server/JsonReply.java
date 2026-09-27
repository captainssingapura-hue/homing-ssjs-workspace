package hue.captains.singapura.js.homing.workspace.shell.server;

import hue.captains.singapura.tao.http.action.TypedContent;

/** A reply of the workspace's server: JSON text, written by the log's own writer. */
public record JsonReply(String body) implements TypedContent {
    @Override public String contentType() { return "application/json; charset=utf-8"; }
}
