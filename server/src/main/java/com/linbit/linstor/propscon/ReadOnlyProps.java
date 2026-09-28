package com.linbit.linstor.propscon;

import com.linbit.linstor.annotation.Nullable;
import com.linbit.linstor.api.prop.LinStorObject;
import com.linbit.utils.RegexMatcher;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public interface ReadOnlyProps extends Iterable<Map.Entry<String, String>>
{
    String PATH_SEPARATOR = "/";

    String getDescription();

    LinStorObject getType();

    @Nullable
    String getProp(String key)
        throws InvalidKeyException;

    String getPropWithDefault(String key, String defaultValue) throws InvalidKeyException;

    @Nullable
    String getProp(String key, @Nullable String namespace)
        throws InvalidKeyException;

    String getPropWithDefault(String key, @Nullable String namespace, String defaultValue)
        throws InvalidKeyException;

    int size();

    boolean isEmpty();

    String getPath();

    Map<String, String> map();

    Map<String, String> cloneMap();

    Set<Map.Entry<String, String>> entrySet();

    Set<String> keySet();

    Collection<String> values();

    @Override
    Iterator<Map.Entry<String, String>> iterator();

    Iterator<String> keysIterator();

    Iterator<String> valuesIterator();

    @Nullable
    ReadOnlyProps getNamespace(@Nullable String namespace);

    default ReadOnlyProps getNamespaceOrEmpty(String namespace)
    {
        @Nullable ReadOnlyProps roProps = getNamespace(namespace);
        if (roProps == null)
        {
            roProps = ReadOnlyPropsImpl.emptyRoProps();
        }
        return roProps;
    }

    Iterator<String> iterateNamespaces();


    /**
     * Checks if all propFilters (key value pairs e.g 'prop=value') are present in the given Props container.
     * It is also possible to just check if a property is set at all.
     *
     * <p>
     * Both the key and the value part of a filter may be regular expressions (see {@link RegexMatcher}); each is
     * matched against the whole key/value. A regex key (e.g. {@code Aux/.*}) matches if at least one property
     * whose full key matches it also satisfies the value part. Keys and values are matched case-sensitively.
     * Filters without regex metacharacters behave exactly like the previous exact-match check, so a plain
     * {@code site=a} still requires an exact key and value match.
     *
     * @param propFilters List of filter pairs, e.g.: ['site=a', 'Aux/.*', 'DrbdOptions/.*=yes']
     * @return True if all props match.
     */
    default boolean contains(List<String> propFilters)
    {
        boolean result = true;
        for (final String pFilter : propFilters)
        {
            final String[] split = pFilter.split("=", 2);
            final String keyFilter = split[0];
            final @Nullable String valueFilter = split.length > 1 ? split[1] : null;
            // a regex value is compiled once; a plain value is compared with equals()
            final @Nullable Pattern valuePattern =
                valueFilter != null && RegexMatcher.isRegex(valueFilter) ? RegexMatcher.toPattern(valueFilter, false) :
                null;

            final boolean matched;
            if (RegexMatcher.isRegex(keyFilter))
            {
                final Pattern keyPattern = RegexMatcher.toPattern(keyFilter, false);
                boolean anyMatch = false;
                for (final Map.Entry<String, String> entry : entrySet())
                {
                    if (keyPattern.matcher(entry.getKey()).matches() &&
                        valueMatches(valuePattern, valueFilter, entry.getValue()))
                    {
                        anyMatch = true;
                        break;
                    }
                }
                matched = anyMatch;
            }
            else
            {
                final @Nullable String value = getProp(keyFilter);
                matched = value != null && valueMatches(valuePattern, valueFilter, value);
            }
            if (!matched)
            {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * Helper for {@link #contains(List)}: checks the value part of a property filter against a property value.
     *
     * @param valuePattern precompiled regex pattern for the value, or {@code null} if {@code valueFilter} is not
     *     a regex (or there is no value part)
     * @param valueFilter the raw value filter, or {@code null} if the filter only checks for key existence
     * @param value the actual property value
     * @return true if the filter has no value part (existence-only), or the value matches
     */
    private static boolean valueMatches(
        @Nullable Pattern valuePattern,
        @Nullable String valueFilter,
        String value
    )
    {
        final boolean result;
        if (valueFilter == null)
        {
            result = true;
        }
        else if (valuePattern != null)
        {
            result = valuePattern.matcher(value).matches();
        }
        else
        {
            result = value.equals(valueFilter);
        }
        return result;
    }

    /**
     * Checks if a boolean property is considered true.
     *
     * @param key property key
     * @return true if the key value is "true" in any casing.
     */
    default boolean isPropTrue(String key) throws InvalidKeyException
    {
        return getPropWithDefault(key, "false").equalsIgnoreCase("true");
    }
}
