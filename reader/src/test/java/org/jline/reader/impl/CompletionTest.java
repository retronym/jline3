/*
 * Copyright (c) 2002-2020, the original author or authors.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * https://opensource.org/licenses/BSD-3-Clause
 */
package org.jline.reader.impl;

import java.io.IOException;
import java.util.Arrays;

import org.jline.reader.*;
import org.jline.reader.LineReader.Option;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Size;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CompletionTest extends ReaderTestSupport {

    @Test
    public void testCompleteEscape() throws IOException {
        reader.setCompleter(new StringsCompleter("foo bar"));
        assertBuffer("foo\\ bar ", new TestBuffer("fo\t"));
        assertBuffer("\"foo bar\" ", new TestBuffer("\"fo\t"));
    }

    @Test
    public void testListAndMenu() throws IOException {
        reader.setCompleter(new StringsCompleter("foo", "foobar"));

        reader.unsetOpt(Option.MENU_COMPLETE);
        reader.unsetOpt(Option.AUTO_LIST);
        reader.unsetOpt(Option.AUTO_MENU);
        reader.unsetOpt(Option.LIST_AMBIGUOUS);

        assertBuffer("foo", new TestBuffer("fo\t"));
        assertFalse(reader.list);
        assertFalse(reader.menu);

        assertBuffer("foo", new TestBuffer("fo\t\t"));
        assertFalse(reader.list);
        assertFalse(reader.menu);

        reader.setOpt(Option.AUTO_LIST);
        reader.unsetOpt(Option.AUTO_MENU);
        reader.unsetOpt(Option.LIST_AMBIGUOUS);

        assertBuffer("foo", new TestBuffer("fo\t"));
        assertTrue(reader.list);
        assertFalse(reader.menu);

        reader.setOpt(Option.AUTO_LIST);
        reader.unsetOpt(Option.AUTO_MENU);
        reader.setOpt(Option.LIST_AMBIGUOUS);

        assertBuffer("foo", new TestBuffer("fo\t"));
        assertFalse(reader.list);
        assertFalse(reader.menu);

        assertBuffer("foo", new TestBuffer("fo\t\t"));
        assertTrue(reader.list);
        assertFalse(reader.menu);

        reader.unsetOpt(Option.AUTO_LIST);
        reader.setOpt(Option.AUTO_MENU);
        reader.unsetOpt(Option.LIST_AMBIGUOUS);

        assertBuffer("foo", new TestBuffer("fo\t"));
        assertFalse(reader.list);
        assertFalse(reader.menu);

        assertBuffer("foo", new TestBuffer("fo\t\t"));
        assertFalse(reader.list);
        assertTrue(reader.menu);

        reader.setOpt(Option.AUTO_LIST);
        reader.setOpt(Option.AUTO_MENU);
        reader.unsetOpt(Option.LIST_AMBIGUOUS);

        assertBuffer("foo", new TestBuffer("fo\t"));
        assertTrue(reader.list);
        assertFalse(reader.menu);

        assertBuffer("foo", new TestBuffer("fo\t\t"));
        assertTrue(reader.list);
        assertTrue(reader.menu);

        reader.setOpt(Option.AUTO_LIST);
        reader.setOpt(Option.AUTO_MENU);
        reader.setOpt(Option.LIST_AMBIGUOUS);

        assertBuffer("foo", new TestBuffer("fo\t"));
        assertFalse(reader.list);
        assertFalse(reader.menu);

        assertBuffer("foo", new TestBuffer("fo\t\t"));
        assertTrue(reader.list);
        assertFalse(reader.menu);

        assertBuffer("foo", new TestBuffer("fo\t\t\t"));
        assertTrue(reader.list);
        assertTrue(reader.menu);

    }

    @Test
    public void testCompletePrefix() throws Exception {
        Completer nil = new NullCompleter();
        Completer read = new StringsCompleter("read");
        Completer and = new StringsCompleter("and");
        Completer save = new StringsCompleter("save");
        Completer aggregator = new AggregateCompleter(
                new ArgumentCompleter(read, and, save, nil)
        );
        reader.setCompleter(aggregator);

        reader.getKeys().bind(new Reference("complete-word"), "\t");

        assertLine("read and ", new TestBuffer("read an\t\n"));
        assertLine("read and ", new TestBuffer("read an\033[D\t\n"));

        reader.getKeys().bind(new Reference("complete-prefix"), "\t");

        assertLine("read and nd", new TestBuffer("read and\033[D\033[D\t\n"));
    }

    @Test
    public void testSuffix() {
        reader.setCompleter((reader, line, candidates) -> {
                    candidates.add(new Candidate(
                            /* value    = */ "range(",
                            /* displ    = */ "range(",
                            /* group    = */ null,
                            /* descr    = */ null,
                            /* suffix   = */ "(",
                            /* key      = */ null,
                            /* complete = */ false));
                    candidates.add(new Candidate(
                            /* value    = */ "strangeTest",
                            /* displ    = */ "strangeTest",
                            /* group    = */ null,
                            /* descr    = */ null,
                            /* suffix   = */ "Test",
                            /* key      = */ null,
                            /* complete = */ false));
                }
        );
        //  DEFAULT_REMOVE_SUFFIX_CHARS = " \t\n;&|";

        assertLine("range ;", new TestBuffer("r\t;\n"));
        assertLine("range(1", new TestBuffer("r\t1\n"));
        assertLine("strange ", new TestBuffer("s\t\n"));
        assertLine("strangeTests", new TestBuffer("s\ts\n"));
    }

    @Test
    public void testMenuOrder() {
        reader.setCompleter(new StringsCompleter(Arrays.asList("ae_helloWorld1", "ad_helloWorld12", "ac_helloWorld1234", "ab_helloWorld123", "aa_helloWorld12345")));
        reader.unsetOpt(Option.AUTO_LIST);
        reader.setOpt(Option.AUTO_MENU);

        assertLine("aa_helloWorld12345 ", new TestBuffer("a\t\n\n"));

        assertLine("ab_helloWorld123 ", new TestBuffer("a\t\t\n\n"));
    }

    @Test
    public void testDumbTerminalNoSizeComplete() {
        terminal.setSize(new Size());
        reader.setCompleter(new StringsCompleter(Arrays.asList("ae_helloWorld", "ad_helloWorld", "ac_helloWorld", "ab_helloWorld", "aa_helloWorld")));

        assertLine("a", new TestBuffer("a\t\n\n"));
    }

    @Test
    public void testParserEofOnEscapedNewLine() {
        DefaultParser parser = new DefaultParser();
        parser.setEofOnEscapedNewLine(true);
        reader.setParser(parser);

        assertLine("test ", new TestBuffer("test \\\t\n\n"));
    }

}
