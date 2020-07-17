/*
 MIT License

 Copyright (c) 2016 Maxim Tomin

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package com.github.decimal;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static com.github.decimal.AbstractDecimal.NaN;

public class BaseDecimalTest {
    private final BaseDecimal decimal = new BaseDecimal() {
        @Override
        public int intValue() {
            return 0;
        }

        @Override
        public long longValue() {
            return 0;
        }

        @Override
        public float floatValue() {
            return 0;
        }

        @Override
        public double doubleValue() {
            return 0;
        }
    };

    @Test
    public void testMulHi() throws Exception {
        testMulHi(1, 1);
        testMulHi(0, 123);
        testMulHi(123, 0);
        testMulHi(1000000000, 1000000000);
        testMulHi(1000000000000L, 1000000000);
        testMulHi(1000000000000L, 3000000000L);
        testMulHi(Long.MAX_VALUE, Integer.MAX_VALUE);

        testMulHi(1234568L, Integer.MAX_VALUE);
        testMulHi(449033535071450778L, 2147483647);
        testMulHi(303601908757L, Integer.MAX_VALUE);
        testMulHi(449033535071450778L, Integer.MAX_VALUE);
        testMulHi(4026532095L, 16777215);
        testMulHi(67553994662215680L, 16777215);
    }

    @Test
    public void testScaleDiv() throws Exception {
        testScaleDiv(1, 1, 1);
        testScaleDiv(1000, 1, 1);
        testScaleDiv(1, 1, 1000);
        testScaleDiv(10, 9, 7);
        testScaleDiv(Long.MAX_VALUE, 9, 1);
        testScaleDiv(Long.MAX_VALUE, 9, Integer.MAX_VALUE);
        testScaleDiv(Long.MAX_VALUE, 9, Long.MAX_VALUE);

        testScaleDiv(1234568L, 9, 1L);
        testScaleDiv(449033535071450778L, 9, 12L);
        testScaleDiv(303601908757L, 9, 659820219978L);
        testScaleDiv(449033535071450778L, 9, 659820219978L);
        testScaleDiv(0x400000000000L, 4, Long.MAX_VALUE);
        testScaleDiv(4026532095L, 4, 67553994662215680L);

        testScaleDiv(9223372036854775807L, 0, 1);
        testScaleDiv(922337203685477580L, 1, 1);
        testScaleDiv(9223372036L, 9, 1);

        testScaleDiv(2147483646, 0, 2147483647);
        testScaleDiv(2147483646, 9, 2147483647);
        testScaleDiv(9223372036854775807L, 9, 2147483647);
        testScaleDiv(9223372036854775807L, 9, 1073741824);

        // overflow when adding "o * 10^scale + v 10^scale / d"
        testScaleDiv(9223372027776627962L, 9, 999999999);
        testScaleDiv(8301034833169298228L, 1, 9);

        assertEquals(0x40000000ffffffffL, 4611686022722355199L);
        testScaleDiv(4611686022722355198L, 9, 4611686022722355199L); // (probably) worst case for down-correction
    }

    @Test
    public void testNegIf() throws Exception {
        assertEquals(10, BaseDecimal.negIf(10, 0));
        assertEquals(-10, BaseDecimal.negIf(-10, 0));
        assertEquals(0, BaseDecimal.negIf(0, 0));

        assertEquals(-10, BaseDecimal.negIf(10, -1));
        assertEquals(10, BaseDecimal.negIf(-10, -1));
        assertEquals(0, BaseDecimal.negIf(0, -1));
    }

    @Test
    public void testScaleDivRound() throws Exception {
        assertEquals(1, decimal.scaleDivRound(1, 0, 1, RoundingMode.UP));
        assertEquals(-1, decimal.scaleDivRound(1, 0, -1, RoundingMode.UP));
        assertEquals(-1, decimal.scaleDivRound(-1, 0, 1, RoundingMode.UP));
        assertEquals(1, decimal.scaleDivRound(-1, 0, -1, RoundingMode.UP));
        assertEquals(NaN, decimal.scaleDivRound(NaN, 0, 1, RoundingMode.UP));
        assertEquals(NaN, decimal.scaleDivRound(NaN, 0, -1, RoundingMode.UP));
        assertEquals(NaN, decimal.scaleDivRound(1, 0, NaN, RoundingMode.UP));
        assertEquals(NaN, decimal.scaleDivRound(NaN, 0, NaN, RoundingMode.UP));

        assertEquals(10, decimal.scaleDivRound(1, 1, 1, RoundingMode.UP));
        assertEquals(-10, decimal.scaleDivRound(1, 1, -1, RoundingMode.UP));
        assertEquals(-10, decimal.scaleDivRound(-1, 1, 1, RoundingMode.UP));
        assertEquals(10, decimal.scaleDivRound(-1, 1, -1, RoundingMode.UP));
        assertEquals(NaN, decimal.scaleDivRound(NaN, 1, 1, RoundingMode.UP));
        assertEquals(NaN, decimal.scaleDivRound(NaN, 1, -1, RoundingMode.UP));
        assertEquals(NaN, decimal.scaleDivRound(1, 1, NaN, RoundingMode.UP));
        assertEquals(NaN, decimal.scaleDivRound(NaN, 1, NaN, RoundingMode.UP));

        assertEquals(4, decimal.scaleDivRound(1, 1, 3, RoundingMode.UP));
        assertEquals(-4, decimal.scaleDivRound(1, 1, -3, RoundingMode.UP));
        assertEquals(-4, decimal.scaleDivRound(-1, 1, 3, RoundingMode.UP));
        assertEquals(4, decimal.scaleDivRound(-1, 1, -3, RoundingMode.UP));
        assertEquals(NaN, decimal.scaleDivRound(NaN, 1, 3, RoundingMode.UP));
        assertEquals(NaN, decimal.scaleDivRound(NaN, 1, -3, RoundingMode.UP));
        assertEquals(NaN, decimal.scaleDivRound(1, 1, NaN, RoundingMode.UP));
        assertEquals(NaN, decimal.scaleDivRound(NaN, 1, NaN, RoundingMode.UP));

        assertEquals(NaN, decimal.scaleDivRound(Long.MAX_VALUE, 9, 1, RoundingMode.UP)); // overflow
    }

    @Test
    public void testMulScaleRound() throws Exception {
        assertEquals(123, decimal.mulScaleRound(123, 1, 0, RoundingMode.UP));
        assertEquals(-123, decimal.mulScaleRound(123, -1, 0, RoundingMode.UP));
        assertEquals(-123, decimal.mulScaleRound(-123, 1, 0, RoundingMode.UP));
        assertEquals(123, decimal.mulScaleRound(-123, -1, 0, RoundingMode.UP));
        assertEquals(NaN, decimal.mulScaleRound(123, NaN, 0, RoundingMode.UP));
        assertEquals(NaN, decimal.mulScaleRound(-123, NaN, 0, RoundingMode.UP));
        assertEquals(NaN, decimal.mulScaleRound(NaN, 1, 0, RoundingMode.UP));
        assertEquals(NaN, decimal.mulScaleRound(NaN, NaN, 0, RoundingMode.UP));

        assertEquals(13, decimal.mulScaleRound(123, 1, 1, RoundingMode.UP));
        assertEquals(-13, decimal.mulScaleRound(123, -1, 1, RoundingMode.UP));
        assertEquals(-13, decimal.mulScaleRound(-123, 1, 1, RoundingMode.UP));
        assertEquals(13, decimal.mulScaleRound(-123, -1, 1, RoundingMode.UP));
        assertEquals(NaN, decimal.mulScaleRound(123, NaN, 1, RoundingMode.UP));
        assertEquals(NaN, decimal.mulScaleRound(-123, NaN, 1, RoundingMode.UP));
        assertEquals(NaN, decimal.mulScaleRound(NaN, 1, 1, RoundingMode.UP));
        assertEquals(NaN, decimal.mulScaleRound(NaN, NaN, 1, RoundingMode.UP));

        assertEquals(13, decimal.mulScaleRound(1, 123, 1, RoundingMode.UP));
        assertEquals(-13, decimal.mulScaleRound(-1, 123, 1, RoundingMode.UP));
        assertEquals(-13, decimal.mulScaleRound(1, -123, 1, RoundingMode.UP));
        assertEquals(13, decimal.mulScaleRound(-1, -123, 1, RoundingMode.UP));
        assertEquals(NaN, decimal.mulScaleRound(NaN, 123, 1, RoundingMode.UP));
        assertEquals(NaN, decimal.mulScaleRound(NaN, -123, 1, RoundingMode.UP));
        assertEquals(NaN, decimal.mulScaleRound(1, NaN, 1, RoundingMode.UP));
        assertEquals(NaN, decimal.mulScaleRound(NaN, NaN, 1, RoundingMode.UP));

        assertEquals(NaN, decimal.mulScaleRound(Long.MAX_VALUE, Long.MAX_VALUE, 1, RoundingMode.UP)); // overflow
    }

    @Test
    public void testMulScale() throws Exception {
        testMulScale(1, 1, 0);
        testMulScale(1000, 1, 0);
        testMulScale(1, 1, 3);
        testMulScale(10, 15, 7);
        testMulScale(Long.MAX_VALUE, Long.MAX_VALUE, 1);
        testMulScale(Long.MAX_VALUE, Long.MAX_VALUE, 9);
        testMulScale(Long.MAX_VALUE, Long.MAX_VALUE, 18);

        testMulScale(1234568L, Integer.MAX_VALUE, 1);
        testMulScale(449033535071450778L, 2147483647, 2);
        testMulScale(4984198405165151231L, 6132198419878046132L,7);
        testMulScale(1540173641653250113L, 1015059321913633968L, 8);
        testMulScale(449033535071450778L, 3155170653582908051L, 9);
        testMulScale(303601908757L, 829267376026L, 5);
        testMulScale(449033535071450778L, 829267376026L, 3);
        testMulScale(1234568, 829267376026L, 0);
        testMulScale(6991754535226557229L, 7798003721120799096L, 2);
        testMulScale(9223372036854775807L, 2147483648L, 4);
        testMulScale(9223372032559808512L, 9223372036854775807L, 6);
        testMulScale(9223372032559808512L, 9223372036854775807L, 2);
        testMulScale(9223372036854775807L, 8446744073709551615L, 3);

        testMulScale(1234568L, Integer.MAX_VALUE, 9);
        testMulScale(449033535071450778L, 2147483647, 9);
        testMulScale(4984198405165L, 6132198419878L, 9);
        testMulScale(1540173641653L, 1015059321913L, 9);
        testMulScale(44903353507145L, 3155170653582L, 9);
        testMulScale(303601908757L, 829267376026L, 9);
        testMulScale(4490335350714L, 829267376026L, 9);
        testMulScale(1234568, 829267376026L, 9);
        testMulScale(Integer.MAX_VALUE, 779800372112079L, 9);
        testMulScale(Integer.MAX_VALUE, 2147483648L, 9);
        testMulScale(Integer.MAX_VALUE, 922337203685477L, 9);
        testMulScale(Integer.MAX_VALUE, 844674407370955L, 9);

        testMulScale(1234568L, Integer.MAX_VALUE, 18);
        testMulScale(449033535071450778L, 2147483647, 18);
        testMulScale(4984198405165151231L, 6132198419878046132L, 18);
        testMulScale(1540173641653250113L, 1015059321913633968L, 18);
        testMulScale(449033535071450778L, 3155170653582908051L, 18);
        testMulScale(303601908757L, 829267376026L, 18);
        testMulScale(449033535071450778L, 829267376026L, 18);
        testMulScale(1234568, 829267376026L, 18);
        testMulScale(6991754535226557229L, 7798003721120799096L, 18);
        testMulScale(9223372036854775807L, 2147483648L, 18);
        testMulScale(9223372032559808512L, 9223372036854775807L, 18);
        testMulScale(9223372032559808512L, 9223372036854775807L, 18);
        testMulScale(9223372036854775807L, 8446744073709551615L, 18);
    }


    @Test
    public void testDownScale() throws Exception {
        testDownScale(9223372036854775807L, 0);
        testDownScale(922337203685477580L, 1);
        testDownScale(92233720368547758L, 2);
        testDownScale(9223372036854775L, 3);
        testDownScale(922337203685477L, 4);
        testDownScale(92233720368547L, 5);
        testDownScale(9223372036854L, 6);
        testDownScale(922337203685L, 7);
        testDownScale(92233720368L, 8);
        testDownScale(9223372036L, 9);
    }

    private void testDownScale(long value, int scale) {
        if (scale == 0) {
            assertThat(decimal.downScale_63_31(value, scale), is(value));
            assertThat(decimal.a, is(0L));

            value *= 2;
            assertThat(decimal.unsignedDownScale_64_31(value, scale), is(value));
            assertThat(decimal.a, is(0L));
        } else {
            long product = value * (long) Math.pow(10, scale) + 1;
            assertThat(decimal.downScale_63_31(product, scale), is(value));
            assertThat(decimal.a, is(1L));

            value *= 2;
            product = value * (long) Math.pow(10, scale) + 1;
            assertThat(decimal.unsignedDownScale_64_31(product, scale), is(value));
            assertThat(decimal.a, is(1L));
        }
    }

    @Test
    public void testRound() throws Exception {
        for (int whole = 0; whole <= 9; whole++) {
            for (int num = 0; num <= 9; num++) {
                for (RoundingMode mode : RoundingMode.values()) {
                    if (mode != RoundingMode.UNNECESSARY) {
                        assertThat(whole + " " + num + "/10 " + mode,
                                BaseDecimal.round(whole, num, 10, mode),
                                is(BigDecimal.valueOf(whole * 10 + num).divide(BigDecimal.TEN, mode).longValue()));
                        assertThat("-" + whole + " " + num + "/10 " + mode,
                                BaseDecimal.round(-whole, -num, 10, mode),
                                is(BigDecimal.valueOf(-whole * 10 - num).divide(BigDecimal.TEN, mode).longValue()));
                    }
                }
            }
        }

        // some overflows
        assertThat(BaseDecimal.round(0, Long.MAX_VALUE - 1, Long.MAX_VALUE, RoundingMode.DOWN), is(0L));
        assertThat(BaseDecimal.round(0, Long.MAX_VALUE - 1, Long.MAX_VALUE, RoundingMode.UP), is(1L));
        assertThat(BaseDecimal.round(0, Long.MAX_VALUE - 1, Long.MAX_VALUE, RoundingMode.FLOOR), is(0L));
        assertThat(BaseDecimal.round(0, Long.MAX_VALUE - 1, Long.MAX_VALUE, RoundingMode.CEILING), is(1L));

        assertThat(BaseDecimal.round(0, -Long.MAX_VALUE + 1, Long.MAX_VALUE, RoundingMode.DOWN), is(0L));
        assertThat(BaseDecimal.round(0, -Long.MAX_VALUE + 1, Long.MAX_VALUE, RoundingMode.UP), is(-1L));
        assertThat(BaseDecimal.round(0, -Long.MAX_VALUE + 1, Long.MAX_VALUE, RoundingMode.FLOOR), is(-1L));
        assertThat(BaseDecimal.round(0, -Long.MAX_VALUE + 1, Long.MAX_VALUE, RoundingMode.CEILING), is(0L));

        assertThat(BaseDecimal.round(0, Long.MAX_VALUE / 2, Long.MAX_VALUE - 1, RoundingMode.HALF_DOWN), is(0L));
        assertThat(BaseDecimal.round(0, Long.MAX_VALUE / 2, Long.MAX_VALUE - 1, RoundingMode.HALF_UP), is(1L));
        assertThat(BaseDecimal.round(0, Long.MAX_VALUE / 2, Long.MAX_VALUE - 1, RoundingMode.HALF_EVEN), is(0L));
        assertThat(BaseDecimal.round(1, Long.MAX_VALUE / 2, Long.MAX_VALUE - 1, RoundingMode.HALF_EVEN), is(2L));

        assertThat(BaseDecimal.round(0, -Long.MAX_VALUE / 2, Long.MAX_VALUE - 1, RoundingMode.HALF_DOWN), is(0L));
        assertThat(BaseDecimal.round(0, -Long.MAX_VALUE / 2, Long.MAX_VALUE - 1, RoundingMode.HALF_UP), is(-1L));
        assertThat(BaseDecimal.round(0, -Long.MAX_VALUE / 2, Long.MAX_VALUE - 1, RoundingMode.HALF_EVEN), is(0L));
        assertThat(BaseDecimal.round(-1, -Long.MAX_VALUE / 2, Long.MAX_VALUE - 1, RoundingMode.HALF_EVEN), is(-2L));
    }

    private void testMulScale(long a, long b, int scale) {
        BigInteger[] dAndR = BigInteger.valueOf(a)
                .multiply(BigInteger.valueOf(b))
                .divideAndRemainder(BigInteger.TEN.pow(scale));

        long q = decimal.mulscale_63_31(a, b, scale);
        long r = decimal.a;
        if (q != NaN) {
            assertEquals("Quantity", dAndR[0], BigInteger.valueOf(q));
            assertEquals("Remainder", dAndR[1], BigInteger.valueOf(r));
        } else {
            // overflow
            assertTrue(dAndR[0].compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0);
        }
    }

    private void testScaleDiv(long v, int s, long d) {
        testScaleDiv1(v, s, d);
        if (v < Long.MAX_VALUE) {
            testScaleDiv1(v + 1, s, d);
        }
        if (v > 0) {
            testScaleDiv1(v - 1, s, d);
        }
        if (s < 9) {
            testScaleDiv1(v, s + 1, d);
        }
        if (s > 0) {
            testScaleDiv1(v, s - 1, d);
        }
        if (d < Long.MAX_VALUE) {
            testScaleDiv1(v, s, d + 1);
        }
        if (d > 1) {
            testScaleDiv1(v, s, d - 1);
        }
    }

    private void testScaleDiv1(long v, int s, long d) {
        try {
            BigInteger[] dAndR = BigInteger.valueOf(v).multiply(BigInteger.TEN.pow(s)).divideAndRemainder(BigInteger.valueOf(d));

            long q = decimal.scalediv_63_63(v, s, d);
            long r = decimal.a;
            if (q != NaN) {
                assertEquals("Quantity", dAndR[0], BigInteger.valueOf(q));
                assertEquals("Remainder", dAndR[1], BigInteger.valueOf(r));
            } else {
                // zero dividend or overflow
                assertTrue(d == 0 || dAndR[0].compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0);
            }
        } catch (AssertionError e) {
            System.out.println("Failed for " + v + ", " + s + ", " + d);
            throw e;
        }
    }

    private void testMulHi(long a, long b) {
        long hi = decimal.mulhi_63_32(a, b);
        long lo = decimal.a;
        assertEquals(BigInteger.valueOf(hi).shiftLeft(BaseDecimal.WORD_BITS).add(BigInteger.valueOf(lo)),
                BigInteger.valueOf(a).multiply(BigInteger.valueOf(b)));
    }

    private static long replaceBitsWithF(int v) {
        long result = 0;
        for (int i = 0; i < 16; i++) {
            if ((v & (1 << i)) != 0) {
                result |= (0xfL << (i * 4));
            }
        }
        return result;
    }

    public static void main(String[] args) {
        // bulk test
        BaseDecimalTest test = new BaseDecimalTest();
        for (int i = 0; i < 65536; i++) {
            long v = replaceBitsWithF(i);
            if (v < 0) {
                continue;
            }
            System.out.println(i);
            for (int j = 0; j <= 9; j++) {
                for (int k = 0; k < 65536; k++) {
                    long d = replaceBitsWithF(k);
                    if (d <= 0) {
                        continue;
                    }
                    test.testScaleDiv(v, j, d);
                }
            }
        }
    }
}

