package com.linbit.linstor.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class SetUtilsTest
{
    @Test
    @SuppressWarnings("unchecked")
    public void testMergeIntoTreeSet()
    {
        Set<Integer> setA = new HashSet<>(Arrays.asList(3, 1));
        Set<Integer> setB = new HashSet<>(Arrays.asList(2, 3));

        TreeSet<Integer> merged = SetUtils.mergeIntoTreeSet(setA, setB);
        assertThat(merged).containsExactly(1, 2, 3);
    }

    @Test
    public void testMergeIntoTreeSetWithoutInputSets()
    {
        assertTrue(SetUtils.<String>mergeIntoTreeSet().isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMergeIntoHashSet()
    {
        Set<String> setA = new HashSet<>(Arrays.asList("a", "b"));
        Set<String> setB = new HashSet<>(Arrays.asList("b", "c"));

        HashSet<String> merged = SetUtils.mergeIntoHashSet(setA, setB);
        assertThat(merged).containsExactlyInAnyOrder("a", "b", "c");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMergeReturnsTargetInstance()
    {
        LinkedHashSet<String> target = new LinkedHashSet<>(Arrays.asList("x"));
        Set<String> additional = new HashSet<>(Arrays.asList("y"));

        LinkedHashSet<String> result = SetUtils.merge(target, additional);
        assertSame(target, result);
        assertThat(result).containsExactlyInAnyOrder("x", "y");
    }

    @Test
    public void testMergeWithoutInputSetsLeavesTargetUnchanged()
    {
        HashSet<String> target = new HashSet<>(Arrays.asList("x"));

        HashSet<String> result = SetUtils.merge(target);
        assertSame(target, result);
        assertThat(result).containsExactlyInAnyOrder("x");
    }

    @Test
    public void testConvertPathsToStrings()
    {
        Set<Path> paths = new HashSet<>(Arrays.asList(Paths.get("/a/b"), Paths.get("rel/c")));

        Set<String> result = SetUtils.convertPathsToStrings(paths);
        assertThat(result).containsExactlyInAnyOrder("/a/b", "rel/c");
    }

    @Test
    public void testConvertPathsToStringsEmpty()
    {
        assertTrue(SetUtils.convertPathsToStrings(new HashSet<>()).isEmpty());
    }
}
