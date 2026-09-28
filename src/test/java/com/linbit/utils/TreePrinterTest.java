package com.linbit.utils;

import com.linbit.utils.TreePrinter.TreePrinterNode;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TreePrinterTest
{
    private static final String NL = System.lineSeparator();

    private static final String MARKER_SUB = "\u251C\u2500 ";
    private static final String MARKER_SUB_LAST = "\u2514\u2500 ";
    private static final String PREFIX_VLINE = "\u2502  ";
    private static final String PREFIX_SPACE = "   ";

    @Test
    public void testSingleRootNode()
    {
        String output = printToString(TreePrinter.builder("root"));
        assertEquals("root" + NL, output);
    }

    @Test
    public void testRootFormatArguments()
    {
        String output = printToString(TreePrinter.builder("node %s (%d)", "alpha", 3));
        assertEquals("node alpha (3)" + NL, output);
    }

    @Test
    public void testLeafFormatArguments()
    {
        String output = printToString(TreePrinter.builder("root").leaf("%s-%02d", "x", 5));
        assertEquals(
            "root" + NL +
            MARKER_SUB_LAST + "x-05" + NL,
            output
        );
    }

    @Test
    public void testLeavesAndBranch()
    {
        TreePrinter.Builder builder = TreePrinter.builder("root")
            .leaf("leaf1")
            .branch("branch1")
            .leaf("leaf2")
            .leaf("leaf3")
            .endBranch()
            .leaf("leaf4");

        assertEquals(
            "root" + NL +
            MARKER_SUB + "leaf1" + NL +
            MARKER_SUB + "branch1" + NL +
            PREFIX_VLINE + MARKER_SUB + "leaf2" + NL +
            PREFIX_VLINE + MARKER_SUB_LAST + "leaf3" + NL +
            MARKER_SUB_LAST + "leaf4" + NL,
            printToString(builder)
        );
    }

    @Test
    public void testNestedBranchesUseSpacePrefixForLastChildren()
    {
        TreePrinter.Builder builder = TreePrinter.builder("root")
            .branch("b1")
            .branch("b2")
            .leaf("x")
            .endBranch()
            .endBranch();

        assertEquals(
            "root" + NL +
            MARKER_SUB_LAST + "b1" + NL +
            PREFIX_SPACE + MARKER_SUB_LAST + "b2" + NL +
            PREFIX_SPACE + PREFIX_SPACE + MARKER_SUB_LAST + "x" + NL,
            printToString(builder)
        );
    }

    @Test
    public void testBranchHideEmptyHidesBranchWithoutChildren()
    {
        TreePrinter.Builder builder = TreePrinter.builder("root")
            .branchHideEmpty("empty")
            .endBranch()
            .leaf("leaf");

        assertEquals(
            "root" + NL +
            MARKER_SUB_LAST + "leaf" + NL,
            printToString(builder)
        );
    }

    @Test
    public void testBranchHideEmptyKeepsBranchWithChildren()
    {
        TreePrinter.Builder builder = TreePrinter.builder("root")
            .branchHideEmpty("filled")
            .leaf("x")
            .endBranch();

        assertEquals(
            "root" + NL +
            MARKER_SUB_LAST + "filled" + NL +
            PREFIX_SPACE + MARKER_SUB_LAST + "x" + NL,
            printToString(builder)
        );
    }

    @Test
    public void testBranchWithoutHideEmptyIsKeptWhenEmpty()
    {
        TreePrinter.Builder builder = TreePrinter.builder("root")
            .branch("empty")
            .endBranch();

        assertEquals(
            "root" + NL +
            MARKER_SUB_LAST + "empty" + NL,
            printToString(builder)
        );
    }

    @Test
    public void testEndBranchOnRootThrows()
    {
        try
        {
            TreePrinter.builder("root").endBranch();
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException exc)
        {
            assertEquals("Cannot end root branch", exc.getMessage());
        }
    }

    @Test
    public void testPrintWithOpenBranchPrintsCurrentBranchAsRoot()
    {
        // Surprising behavior: Builder.print prints the current (innermost open) branch,
        // so without endBranch the actual root node is not part of the output
        TreePrinter.Builder builder = TreePrinter.builder("root")
            .branch("open")
            .leaf("x");

        assertEquals(
            "open" + NL +
            MARKER_SUB_LAST + "x" + NL,
            printToString(builder)
        );
    }

    @Test
    public void testPrintNodeDirectly()
    {
        TreePrinterNode leaf = new TreePrinterNode("child", Collections.emptyList());
        TreePrinterNode root = new TreePrinterNode("top", Arrays.asList(leaf));

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outStream, true, StandardCharsets.UTF_8);
        TreePrinter.print(printStream, root);

        assertEquals(
            "top" + NL +
            MARKER_SUB_LAST + "child" + NL,
            outStream.toString(StandardCharsets.UTF_8)
        );
        assertEquals("top", root.getName());
        assertEquals(Arrays.asList(leaf), root.getChildren());
    }

    private String printToString(TreePrinter.Builder builder)
    {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outStream, true, StandardCharsets.UTF_8);
        builder.print(printStream);
        return outStream.toString(StandardCharsets.UTF_8);
    }
}
