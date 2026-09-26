package hue.captains.singapura.js.homing.workspace.log.store;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The validator: a file passes only when Java reads every line and writes back the same bytes. */
class ValidateWorkspaceLogTest {

    @TempDir Path dir;

    private static final String HEADER = "{\"format\":\"homing.workspace.log\",\"version\":2,\"kind\":\"demo\",\"workspaceId\":\"7f1b6c2e-5000-9000-7f1b-6c2e00000001\"}";
    private static final String E1 = "{\"seq\":1,\"at\":1790000000000,\"event\":{\"type\":\"TabOpened\",\"id\":\"tab-1\",\"kind\":\"note\",\"title\":\"Note\",\"host\":{\"type\":\"InRegion\",\"id\":\"main\"},\"index\":0}}";
    private static final String E2 = "{\"seq\":2,\"at\":1790000000001,\"event\":{\"type\":\"TabShown\",\"host\":{\"type\":\"InRegion\",\"id\":\"main\"},\"id\":\"tab-1\"}}";

    private record Run(int code, String out, String err) {}

    private Run runBytes(byte[] bytes) throws IOException {
        Path f = dir.resolve("w.workspace.log");
        Files.write(f, bytes);
        return runArgs(f.toString());
    }

    private Run runArgs(String... args) {
        var out = new ByteArrayOutputStream();
        var err = new ByteArrayOutputStream();
        int code = ValidateWorkspaceLog.run(args, new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8));
        return new Run(code, out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8));
    }

    private Run run(String text) throws IOException { return runBytes(text.getBytes(StandardCharsets.UTF_8)); }

    private void invalid(String text, String says) throws IOException {
        var r = run(text);
        assertEquals(ValidateWorkspaceLog.INVALID, r.code(), text);
        assertTrue(r.err().contains(says), () -> "expected '" + says + "' in: " + r.err());
    }

    @Test
    void aFileWrittenAsBothWriteItPasses() throws IOException {
        var r = run(HEADER + "\n" + E1 + "\n" + E2 + "\n");
        assertEquals(ValidateWorkspaceLog.VALID, r.code(), r.err());
        assertTrue(r.out().contains("valid - 2 events of demo 7f1b6c2e-5000-9000-7f1b-6c2e00000001, seq 1 to 2"), r.out());
        assertEquals(ValidateWorkspaceLog.VALID, run(HEADER + "\n").code());
    }

    @Test
    void aLineJavaReadsButWritesOtherwiseFailsThere() throws IOException {
        invalid(HEADER + "\n" + E1 + "\n" + E2.replace("\"host\":{\"type\":\"InRegion\",\"id\":\"main\"},\"id\":\"tab-1\"", "\"id\":\"tab-1\",\"host\":{\"type\":\"InRegion\",\"id\":\"main\"}") + "\n",
                "line 3 is not written as Java writes it");
        invalid(HEADER + "\n" + E1.replace("\"Note\"", "\"\\u004eote\"") + "\n", "line 2 is not written as Java writes it");
        invalid(HEADER + "\n" + E1.replace(":0}", ": 0}") + "\n", "line 2 is not written as Java writes it");
    }

    @Test
    void aLineJavaCannotReadFailsThere() throws IOException {
        invalid(HEADER + "\n" + E1.replace("\"index\":0", "\"index\":0.0") + "\n", "line 2: at ");
        invalid(HEADER + "\n" + E1.replace("\"tab-1\"", "\"tab 1\"") + "\n", "line 2: TabId");
        invalid(HEADER + "\n" + E1.replace("\"index\":0", "\"index\":-1") + "\n", "line 2: TabOpened");
        invalid(HEADER + "\n" + E1.replace("TabOpened", "TabGone") + "\n", "line 2: WorkspaceEvent.type");
        invalid(HEADER.replace("\"version\":2", "\"version\":1") + "\n", "line 1: LogHeader: LogHeader.version 1 — this reads 2");
        invalid(HEADER + "\n" + E2 + "\n" + E1 + "\n", "line 3: seq 1 does not come after 2");
        invalid(HEADER + "\r\n" + E1 + "\r\n", "line 1: a carriage return");
        invalid(HEADER + "\n" + E1, "line 2: the last line is not ended by a line feed");
        invalid(HEADER + "\n\n", "line 2: an empty line");
        invalid("", "line 1: the file is empty");
    }

    @Test
    void bytesThatAreNotUtf8FailAndNoFileDoesNotRun() throws IOException {
        var r = runBytes(new byte[]{'{', (byte) 0xC3, '}', '\n'});
        assertEquals(ValidateWorkspaceLog.INVALID, r.code());
        assertTrue(r.err().contains("not valid UTF-8"), r.err());
        assertEquals(ValidateWorkspaceLog.NOT_RUN, runArgs().code());
        assertEquals(ValidateWorkspaceLog.NOT_RUN, runArgs(dir.resolve("absent.log").toString()).code());
    }
}
