package com.linbit.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegexMatcherTest
{
    private static boolean matches(String regex, String value, boolean caseInsensitive)
    {
        return RegexMatcher.toPattern(regex, caseInsensitive).matcher(value).matches();
    }

    @Test
    public void testIsRegex()
    {
        assertTrue(RegexMatcher.isRegex("a.*"));
        assertTrue(RegexMatcher.isRegex("a?b"));
        assertTrue(RegexMatcher.isRegex("a[bc]"));
        assertTrue(RegexMatcher.isRegex("a|b"));
        assertTrue(RegexMatcher.isRegex("^a$"));
        // plain literals - including names with '-' and namespaced property keys with '/'
        assertFalse(RegexMatcher.isRegex("plain"));
        assertFalse(RegexMatcher.isRegex("node-1"));
        assertFalse(RegexMatcher.isRegex("Aux/site"));
        assertFalse(RegexMatcher.isRegex("DrbdOptions/auto-quorum"));
        // '.' is deliberately not a metacharacter -> a dotted string is still a literal
        assertFalse(RegexMatcher.isRegex("some.node.name"));
        assertFalse(RegexMatcher.isRegex("192.168.1.1"));
        assertFalse(RegexMatcher.isRegex(""));
    }

    @Test
    public void testPlainStringIsExactMatch()
    {
        assertTrue(matches("node1", "node1", false));
        assertFalse(matches("node1", "node12", false));
        assertFalse(matches("node1", "anode1", false));
    }

    @Test
    public void testFullMatchSemantics()
    {
        // Matcher.matches() anchors the whole value, so a bare 'ssd' does not match a substring
        assertFalse(matches("ssd", "ssd-pool", false));
        assertTrue(matches(".*ssd.*", "fast-ssd-pool", false));
        assertTrue(matches("node.*", "node1", false));
        assertTrue(matches("node.*", "node", false));
    }

    @Test
    public void testRegexMetacharacters()
    {
        assertTrue(matches("node[12]", "node1", false));
        assertFalse(matches("node[12]", "node3", false));
        assertTrue(matches("node[0-9]+", "node42", false));
        assertTrue(matches("node1|node2", "node2", false));
        assertFalse(matches("node1|node2", "node3", false));
    }

    @Test
    public void testDotIsLiteralUnlessOtherMetachar()
    {
        // '.' alone is literal, so FQDNs / IPs / versions match exactly and are not wildcards
        assertFalse(matches("some.node.name", "some1node_name", false));
        assertTrue(matches("some.node.name", "some.node.name", false));
        assertTrue(matches("192.168.1.1", "192.168.1.1", false));
        assertFalse(matches("192.168.1.1", "192x168y1z1", false));
        // adding any other metacharacter turns the filter into a regex, so '.' becomes "any char"
        assertTrue(matches("some.node.name()", "some1node_name", false));
    }

    @Test
    public void testCaseSensitivity()
    {
        assertFalse(matches("Node.*", "node1", false));
        assertTrue(matches("Node.*", "node1", true));
        assertTrue(matches("NODE1", "node1", true));
    }

    @Test
    public void testMatchesAnyEmptyMeansMatchAll()
    {
        assertTrue(RegexMatcher.matchesAny(Collections.emptyList(), "anything"));
    }

    @Test
    public void testMatchesAny()
    {
        List<Pattern> patterns = RegexMatcher.compileAll(Arrays.asList("foo.*", "bar"), true);
        assertEquals(2, patterns.size());
        assertTrue(RegexMatcher.matchesAny(patterns, "foobar"));
        assertTrue(RegexMatcher.matchesAny(patterns, "BAR"));
        assertFalse(RegexMatcher.matchesAny(patterns, "baz"));
    }

    @Test
    public void testCompileAllEmpty()
    {
        assertTrue(RegexMatcher.compileAll(Collections.emptyList(), false).isEmpty());
    }
}
