package com.linbit.utils;

import com.linbit.GenericName;
import com.linbit.InvalidNameException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CollectionUtilsTest
{
    @Test
    public void testIsEmpty()
    {
        assertTrue(CollectionUtils.isEmpty(null));
        assertTrue(CollectionUtils.isEmpty(Collections.emptyList()));
        assertTrue(CollectionUtils.isEmpty(Collections.emptySet()));
        assertFalse(CollectionUtils.isEmpty(Collections.singletonList("x")));
    }

    @Test
    public void testLazyCreateWithNullUsesSupplier()
    {
        List<String> created = new ArrayList<>();
        List<String> result = CollectionUtils.lazyCreate(null, () -> created);
        assertSame(created, result);
    }

    @Test
    public void testLazyCreateWithExistingCollectionIgnoresSupplier()
    {
        List<String> existing = new ArrayList<>(Arrays.asList("a"));
        Supplier<List<String>> failingSupplier = () ->
        {
            throw new AssertionError("Supplier must not be called");
        };
        List<String> result = CollectionUtils.lazyCreate(existing, failingSupplier);
        assertSame(existing, result);
    }

    @Test
    public void testCreateOrWrapWithNullUsesSupplier()
    {
        Map<String, Integer> created = new HashMap<>();
        Map<String, Integer> result = CollectionUtils.createOrWrap(null, () -> created, TreeMap::new);
        assertSame(created, result);
    }

    @Test
    public void testCreateOrWrapWithNonNullAppliesWrapper()
    {
        Map<String, Integer> input = new HashMap<>();
        input.put("a", 1);
        input.put("b", 2);
        TreeMap<String, Integer> result = CollectionUtils.createOrWrap(
            input,
            () ->
            {
                throw new AssertionError("Supplier must not be called");
            },
            TreeMap::new
        );
        assertEquals(input, result);
    }

    @Test
    public void testNonNullOrEmptyList()
    {
        assertSame(Collections.emptyList(), CollectionUtils.nonNullOrEmptyList(null));

        List<String> list = Arrays.asList("a", "b");
        assertSame(list, CollectionUtils.nonNullOrEmptyList(list));
    }

    @Test
    public void testContainsStringInNameCollection()
        throws InvalidNameException
    {
        List<TestName> names = Arrays.asList(new TestName("alpha"), new TestName("Beta"));

        // GenericName upper-cases its value and the searched string is upper-cased as well,
        // making the lookup case-insensitive
        assertTrue(CollectionUtils.contains("alpha", names));
        assertTrue(CollectionUtils.contains("ALPHA", names));
        assertTrue(CollectionUtils.contains("beTA", names));
        assertFalse(CollectionUtils.contains("gamma", names));
        assertFalse(CollectionUtils.contains("alpha", (List<TestName>) null));
    }

    @Test
    public void testContainsNameInStringCollection()
        throws InvalidNameException
    {
        TestName name = new TestName("alpha");

        assertTrue(CollectionUtils.contains(name, Arrays.asList("Alpha", "x")));
        assertTrue(CollectionUtils.contains(name, Arrays.asList("ALPHA")));
        assertFalse(CollectionUtils.contains(name, Arrays.asList("gamma")));
        assertFalse(CollectionUtils.contains(name, Collections.emptyList()));
        assertFalse(CollectionUtils.contains(name, null));
    }

    @Test
    public void testAsUpperStringSetFromGenericName()
        throws InvalidNameException
    {
        Set<String> result = CollectionUtils.asUpperStringSetFromGenericName(
            Arrays.asList(new TestName("alpha"), new TestName("ALPHA"), new TestName("beta"))
        );
        assertThat(result).containsExactlyInAnyOrder("ALPHA", "BETA");

        assertThat(CollectionUtils.asUpperStringSetFromGenericName(null)).isEmpty();
    }

    @Test
    public void testAsUpperStringSet()
    {
        Set<String> result = CollectionUtils.asUpperStringSet(Arrays.asList("a", "B", "A"));
        assertThat(result).containsExactlyInAnyOrder("A", "B");

        assertThat(CollectionUtils.asUpperStringSet(null)).isEmpty();
    }

    private static final class TestName extends GenericName
    {
        TestName(String name)
            throws InvalidNameException
        {
            super(name);
        }
    }
}
