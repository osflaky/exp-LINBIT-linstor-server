package com.linbit.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Compiles filter strings as regular expressions and matches values against them.
 *
 * <p>
 * A filter string that contains a regex metacharacter is treated as a Java regular expression
 * ({@link Pattern}) matched against the whole value (via {@link java.util.regex.Matcher#matches()});
 * any other filter is matched literally (quoted), so it behaves like an exact (optionally
 * case-insensitive) equality check. E.g. {@code node1} only matches {@code node1}, while {@code node.*}
 * matches anything starting with {@code node}.
 *
 * <p>
 * Note that {@code .} is deliberately <i>not</i> treated as a metacharacter: dotted names and property
 * values (FQDN node names, IPs, versions, namespaced keys) stay literal by default. To use {@code .} as
 * "any character" the filter must contain another metacharacter, e.g. {@code some.node.*} or a no-op
 * group like {@code some.node.name()}.
 */
public class RegexMatcher
{
    /**
     * Characters that, if present, make a filter a regex instead of a literal. {@code .} is intentionally
     * excluded (see class javadoc).
     */
    private static final String REGEX_META_CHARS = "\\+*?[](){}^$|";

    private RegexMatcher()
    {
    }

    /**
     * Tests whether a string contains a regex metacharacter.
     *
     * @return true if the given string contains at least one regex metacharacter, i.e. it is not a plain
     *     literal string.
     */
    public static boolean isRegex(String str)
    {
        return str.chars().anyMatch(chr -> REGEX_META_CHARS.indexOf(chr) >= 0);
    }

    /**
     * Compiles a single filter into a {@link Pattern}. If the filter contains a regex metacharacter it is
     * compiled as a regular expression; otherwise it is quoted and matched literally.
     *
     * @param filter the filter (regex or literal)
     * @param caseInsensitive whether matching should ignore case
     * @return the compiled pattern
     * @throws java.util.regex.PatternSyntaxException if the filter is a regex with invalid syntax
     */
    public static Pattern toPattern(String filter, boolean caseInsensitive)
    {
        int flags = 0;
        if (caseInsensitive)
        {
            flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        }
        String regex = isRegex(filter) ? filter : Pattern.quote(filter);
        return Pattern.compile(regex, flags);
    }

    /**
     * Compiles a collection of regular expressions.
     *
     * @param regexes the regular expressions (may be empty)
     * @param caseInsensitive whether matching should ignore case
     * @return a list of compiled patterns; empty if {@code regexes} is empty
     */
    public static List<Pattern> compileAll(Collection<String> regexes, boolean caseInsensitive)
    {
        List<Pattern> patterns = new ArrayList<>(regexes.size());
        for (String regex : regexes)
        {
            patterns.add(toPattern(regex, caseInsensitive));
        }
        return patterns;
    }

    /**
     * Tests a value against a list of compiled patterns.
     *
     * @param patterns compiled patterns; an empty list means "no filter"
     * @param value the value to test
     * @return true if {@code patterns} is empty, or {@code value} matches at least one pattern
     */
    public static boolean matchesAny(List<Pattern> patterns, String value)
    {
        boolean result = patterns.isEmpty();
        if (!result)
        {
            for (Pattern pattern : patterns)
            {
                if (pattern.matcher(value).matches())
                {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
}
