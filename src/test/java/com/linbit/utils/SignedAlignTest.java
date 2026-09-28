package com.linbit.utils;

import java.util.Arrays;
import java.util.Collection;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(JUnitParamsRunner.class)
public class SignedAlignTest
{
    @Test
    @Parameters(method = "generateParamsValid")
    public void testValid(AlignConfiguration data)
    {
        SignedAlign test = new SignedAlign(data.base);
        assertEquals(data.floor, test.floor(data.value));
        assertEquals(data.ceiling, test.ceiling(data.value));
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    @Parameters(method = "generateParamsInvalidBasis")
    public void testInvalidBasis(Long base)
    {
        SignedAlign test = new SignedAlign(base);
    }

    @Test(expected = ArithmeticException.class)
    @Parameters(method = "generateParamsFloorOutOfRange")
    public void testFloorOutOfRange(AlignConfiguration data)
    {
        new SignedAlign(data.base).floor(data.value);
    }

    @Test(expected = ArithmeticException.class)
    @Parameters(method = "generateParamsCeilingOutOfRange")
    public void testCeilingOutOfRange(AlignConfiguration data)
    {
        new SignedAlign(data.base).ceiling(data.value);
    }

    @Test
    @SuppressWarnings("checkstyle:magicnumber")
    public void testAlignedValueBounds()
    {
        SignedAlign test = new SignedAlign(10);
        assertEquals(10L, test.alignBase);
        assertEquals(-9223372036854775800L, test.minAlignedValue);
        assertEquals(9223372036854775800L, test.maxAlignedValue);
    }

    @Test
    @SuppressWarnings("checkstyle:magicnumber")
    public void testFloorOfMaxValueDoesNotThrow()
    {
        // Only negative values can be out of range for floor alignment
        assertEquals(9223372036854775800L, new SignedAlign(10).floor(Long.MAX_VALUE));
    }

    @Test
    @SuppressWarnings("checkstyle:magicnumber")
    public void testCeilingOfMinValueDoesNotThrow()
    {
        // Only positive values can be out of range for ceiling alignment
        assertEquals(-9223372036854775800L, new SignedAlign(10).ceiling(Long.MIN_VALUE));
    }

    @Test
    @SuppressWarnings("checkstyle:magicnumber")
    public void testExceptionMessages()
    {
        SignedAlign test = new SignedAlign(10);
        try
        {
            test.floor(Long.MIN_VALUE);
            fail("Expected ArithmeticException");
        }
        catch (ArithmeticException exc)
        {
            assertEquals(
                "Input value is out of range for floor alignment, value = -9223372036854775808, base = 10",
                exc.getMessage()
            );
        }
        try
        {
            test.ceiling(Long.MAX_VALUE);
            fail("Expected ArithmeticException");
        }
        catch (ArithmeticException exc)
        {
            assertEquals(
                "Input value is out of range for ceiling alignment, value = 9223372036854775807, base = 10",
                exc.getMessage()
            );
        }
    }

    @Test
    public void testInvalidBasisMessage()
    {
        try
        {
            new SignedAlign(0);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException exc)
        {
            assertEquals("Invalid base value for alignment, base = 0", exc.getMessage());
        }
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    private Collection<AlignConfiguration> generateParamsValid()
    {
        AlignConfiguration[] data = new AlignConfiguration[]
        {
            // positive values behave like the unsigned Align class
            new AlignConfiguration(10, 5, 0, 10),
            new AlignConfiguration(10, 0, 0, 0),
            new AlignConfiguration(10, 10, 10, 10),
            new AlignConfiguration(10, 12354, 12350, 12360),
            new AlignConfiguration(7, 54563, 54558, 54565),
            new AlignConfiguration(3, 4, 3, 6),
            new AlignConfiguration(3, 95367, 95367, 95367),
            new AlignConfiguration(1, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE),
            // negative values
            new AlignConfiguration(10, -5, -10, 0),
            new AlignConfiguration(10, -10, -10, -10),
            new AlignConfiguration(3, -7, -9, -6),
            new AlignConfiguration(1000, -7, -1000, 0),
            new AlignConfiguration(1000, -854870, -855000, -854000),
            new AlignConfiguration(1, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE),
            // boundary values around minAlignedValue / maxAlignedValue for base 10
            new AlignConfiguration(10, -9223372036854775800L, -9223372036854775800L, -9223372036854775800L),
            new AlignConfiguration(10, -9223372036854775799L, -9223372036854775800L, -9223372036854775790L),
            new AlignConfiguration(10, 9223372036854775800L, 9223372036854775800L, 9223372036854775800L),
            new AlignConfiguration(Long.MAX_VALUE, -1325437654L, -Long.MAX_VALUE, 0)
        };
        return Arrays.asList(data);
    }

    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    private Collection<Long> generateParamsInvalidBasis()
    {
        Long[] data = new Long[]
        {
            0L, -1L, -543245L, Long.MIN_VALUE
        };
        return Arrays.asList(data);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    private Collection<AlignConfiguration> generateParamsFloorOutOfRange()
    {
        AlignConfiguration[] data = new AlignConfiguration[]
        {
            new AlignConfiguration(10, Long.MIN_VALUE, 0, 0),
            new AlignConfiguration(10, -9223372036854775801L, 0, 0),
            new AlignConfiguration(Long.MAX_VALUE / 2, Long.MIN_VALUE, 0, 0)
        };
        return Arrays.asList(data);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    private Collection<AlignConfiguration> generateParamsCeilingOutOfRange()
    {
        AlignConfiguration[] data = new AlignConfiguration[]
        {
            new AlignConfiguration(10, Long.MAX_VALUE, 0, 0),
            new AlignConfiguration(10, 9223372036854775801L, 0, 0),
            new AlignConfiguration(Long.MAX_VALUE / 2, Long.MAX_VALUE, 0, 0)
        };
        return Arrays.asList(data);
    }

    private static class AlignConfiguration
    {
        public final long base;
        public final long value;
        public final long floor;
        public final long ceiling;

        AlignConfiguration(long baseRef, long valueRef, long floorRef, long ceilingRef)
        {
            base = baseRef;
            value = valueRef;
            floor = floorRef;
            ceiling = ceilingRef;
        }
    }
}
