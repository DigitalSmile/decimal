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
package com.github.quickdecimal;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static com.github.quickdecimal.AbstractDecimal.NaN;

public class AbstractDecimalTest {
    private final Decimal quantity = DecimalFactory.of(0,2);
    private final Decimal price = DecimalFactory.of(0,8);

    @Test
    public void testNaN() {
        assertEquals(NaN, quantity.set(NaN).getRaw());
        assertTrue(quantity.isNaN());
        try {
            quantity.checkNotNaN();
            fail("Exception expected");
        } catch (ArithmeticException e) {
        }
    }

    @Test
    public void testNegate() {
        assertEquals(-123, quantity.set(123).negate().getRaw());
        assertEquals(123, quantity.negate().getRaw());
        assertEquals(-123, quantity.negate().getRaw());
        assertEquals(NaN, quantity.set(NaN).negate().getRaw());
        assertEquals(0, quantity.set(0).negate().getRaw());
    }

    @Test
    public void testSet()  {
        assertEquals("123.00", quantity.set(price.fromDouble(123.0)).toString());
        assertEquals("123.00", quantity.set(price.fromDouble(123.005)).toString());
        assertEquals("123.01", quantity.set(price.fromDouble(123.005), RoundingMode.UP).toString());

        assertEquals("123.00", quantity.set(123).toString());
        assertEquals("-123.00", quantity.set(-123).toString());
        assertEquals("NaN", quantity.set(Long.MAX_VALUE).toString());
        assertEquals("NaN", quantity.set(-Long.MAX_VALUE).toString());

        assertEquals("123.00000000", price.set(price.fromDouble(123.0)).toString());
        assertEquals("123.00000000", price.set(quantity.fromDouble(123.0)).toString());
        assertEquals("92233720368547758.00", quantity.fromLong(92233720368547758L).toString());
        assertEquals("NaN", price.set(quantity).toString());
        assertEquals("-92233720368547758.00", quantity.fromLong(-92233720368547758L).toString());
        assertEquals("NaN", price.set(quantity).toString());
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("123", DecimalFactory.of(123,0).toString());
        assertEquals("12.3", DecimalFactory.of(12.3,1).toString());
        assertEquals("1.23", DecimalFactory.of(1.23,2).toString());
        assertEquals("0.123", DecimalFactory.of(.123,3).toString());
        assertEquals("0.0123", DecimalFactory.of(.0123,4).toString());

        assertEquals("-123", DecimalFactory.of(-123,0).toString());
        assertEquals("-12.3", DecimalFactory.of(-12.3,1).toString());
        assertEquals("-1.23",DecimalFactory.of(-1.23,2).toString());
        assertEquals("-0.123", DecimalFactory.of(-.123,3).toString());
        assertEquals("-0.0123", DecimalFactory.of(-.0123,4).toString());

        assertEquals("0", DecimalFactory.of(0,0).toString());
        assertEquals("0.0", DecimalFactory.of(0,1).toString());
        assertEquals("0.00", DecimalFactory.of(0,2).toString());
        assertEquals("1.00", DecimalFactory.of(1.00,2).toString());

        assertEquals("NaN", DecimalFactory.of(AbstractDecimal.NaN,2).toString());

        StringBuilder sb = new StringBuilder();
        assertEquals("0.0123", DecimalFactory.of(.0123,4).toStringBuilder(sb).toString());
        assertEquals("0.01230.0123", DecimalFactory.of(.01230,4).toStringBuilder(sb).toString());
        assertEquals("0.01230.01230.0123", DecimalFactory.of(.01230,4).toStringBuilder(sb).toString());
    }

    @Test
    public void testParse() throws Exception {
        assertEquals(123, DecimalFactory.of(0,0).parse("123").getRaw());
        assertEquals(123, DecimalFactory.of(0,1).parse("12.3").getRaw());
        assertEquals(123, DecimalFactory.of(0,2).parse("1.23").getRaw());
        assertEquals(123, DecimalFactory.of(0,3).parse("0.123").getRaw());
        assertEquals(123, DecimalFactory.of(0,4).parse("0.0123").getRaw());

        assertEquals(1230, DecimalFactory.of(0,4).parse("0.123").getRaw());
        assertEquals(12300, DecimalFactory.of(0,4).parse("1.23").getRaw());
        assertEquals(123000, DecimalFactory.of(0,4).parse("12.3").getRaw());
        assertEquals(1230000, DecimalFactory.of(0,4).parse("123").getRaw());
        assertEquals(1230000, DecimalFactory.of(0,4).parse("000000000000000123.00000000000000000").getRaw());

        assertEquals(-1230000,  DecimalFactory.of(0,4).parse("-123").getRaw());
        assertEquals(0,  DecimalFactory.of(0,4).parse("0").getRaw());

        assertEquals(Long.MAX_VALUE, DecimalFactory.of(0,0).parse("9223372036854775807").getRaw());
        assertEquals(-Long.MAX_VALUE, DecimalFactory.of(0,0).parse("-9223372036854775807").getRaw());
        assertEquals(Long.MAX_VALUE, DecimalFactory.of(0,0).parse("9223372036854775807.0").getRaw());
        assertEquals(-Long.MAX_VALUE, DecimalFactory.of(0,0).parse("-9223372036854775807.0").getRaw());
        assertEquals(Long.MAX_VALUE, DecimalFactory.of(0,0).parse("000009223372036854775807.0").getRaw());
        assertEquals(-Long.MAX_VALUE, DecimalFactory.of(0,0).parse("-0000009223372036854775807.0").getRaw());
        assertEquals(NaN, DecimalFactory.of(0,0).parse("NaN").getRaw());
        assertEquals(NaN, DecimalFactory.of(0,0).parse("nAn").getRaw());

        assertExceptionWhileParsing("9223372036854775808");
        assertExceptionWhileParsing("9223372036854775809");
        assertExceptionWhileParsing("9223372036854775810");
        assertExceptionWhileParsing("92233720368547758100000000");
        assertExceptionWhileParsing("-9223372036854775808"); // must be represented as 'NaN'
        assertExceptionWhileParsing("-9223372036854775809");
        assertExceptionWhileParsing("-9223372036854775810");
        assertExceptionWhileParsing("-92233720368547758100000000");
        assertExceptionWhileParsing("1.23.34");
        assertExceptionWhileParsing("rubbish");
        assertExceptionWhileParsing("");
        assertExceptionWhileParsing("none");
        assertExceptionWhileParsing("1.");

        try {
            DecimalFactory.of(0,1).parse("9223372036854775807");
            fail("Exception expected");
        } catch (ParseException e) {
        }
        try {
            DecimalFactory.of(0,1).parse("1", 1, 0);
            fail("Exception expected");
        } catch (ParseException e) {
        }
        try {
            DecimalFactory.of(0,1).parse("-1", 0, 1);
            fail("Exception expected");
        } catch (ParseException e) {
        }
    }

    @Test
    public void testToFromLong() throws Exception {
        assertEquals("123.0", DecimalFactory.of(0,1).fromLong(123).toString());
        assertEquals("1000000000000000000", DecimalFactory.of(0,0).fromLong(1000000000000000000L).toString());
        assertEquals("NaN", DecimalFactory.of(0,1).fromLong(1000000000000000000L).toString());
        assertEquals("NaN", DecimalFactory.of(0,0).fromLong(Long.MIN_VALUE).toString());

        assertEquals("123.0", DecimalFactory.of(0,1).fromLong(1230, 1, RoundingMode.DOWN).toString());
        assertEquals("123.0", DecimalFactory.of(0,1).fromLong(123, 0, RoundingMode.DOWN).toString());
        assertEquals("120.0", DecimalFactory.of(0,1).fromLong(12, -1, RoundingMode.DOWN).toString());
        assertEquals("100.0", DecimalFactory.of(0,1).fromLong(1, -2, RoundingMode.DOWN).toString());

        assertEquals("123.4", DecimalFactory.of(0,1).fromLong(12341, 2, RoundingMode.DOWN).toString());
        assertEquals("123.4", DecimalFactory.of(0,1).fromLong(123411, 3, RoundingMode.DOWN).toString());
        assertEquals("123.5", DecimalFactory.of(0,1).fromLong(12341, 2, RoundingMode.UP).toString());
        assertEquals("123.5", DecimalFactory.of(0,1).fromLong(123411, 3, RoundingMode.UP).toString());
        assertEquals("-123.5", DecimalFactory.of(0,1).fromLong(-12341, 2, RoundingMode.UP).toString());
        assertEquals("-123.5", DecimalFactory.of(0,1).fromLong(-123411, 3, RoundingMode.UP).toString());

        // huge positive scales
        assertEquals("9223372036.9", DecimalFactory.of(0,1).fromLong(Long.MAX_VALUE, 9, RoundingMode.UP).toString());
        assertEquals("922337203.7", DecimalFactory.of(0,1).fromLong(Long.MAX_VALUE, 10, RoundingMode.UP).toString());
        assertEquals("9.3", DecimalFactory.of(0,1).fromLong(Long.MAX_VALUE, 18, RoundingMode.UP).toString());
        assertEquals("1.0", DecimalFactory.of(0,1).fromLong(Long.MAX_VALUE, 19, RoundingMode.UP).toString());
        assertEquals("0.1", DecimalFactory.of(0,1).fromLong(Long.MAX_VALUE, 20, RoundingMode.UP).toString());
        assertEquals("0.1", DecimalFactory.of(0,1).fromLong(Long.MAX_VALUE, 21, RoundingMode.UP).toString());

        // huge negative scales
        assertEquals("1000000000.0", DecimalFactory.of(0,1).fromLong(1, -9).toString());
        assertEquals("10000000000.0", DecimalFactory.of(0,1).fromLong(1, -10).toString());
        assertEquals("100000000000000000.0", DecimalFactory.of(0,1).fromLong(1, -17).toString());
        assertEquals("NaN", DecimalFactory.of(0,1).fromLong(1, -18).toString());
        assertEquals("NaN", DecimalFactory.of(0,1).fromLong(1, -19).toString());
        //FIXME:
//        assertEquals(123L, DecimalFactory.of(0,1).set(1230).toLong(RoundingMode.UNNECESSARY));
//        assertEquals(124L, DecimalFactory.of(0,1).set(1235).toLong(RoundingMode.UP));
//        assertEquals(-123L, DecimalFactory.of(0,1).set(-1235).toLong(RoundingMode.CEILING));
//        assertEquals(-124L, DecimalFactory.of(0,1).set(-1235).toLong(RoundingMode.UP));
    }

    @Test
    public void testToFromDouble() throws Exception {
        for (RoundingMode roundingMode : new RoundingMode[] {RoundingMode.DOWN, RoundingMode.FLOOR, RoundingMode.CEILING, RoundingMode.HALF_EVEN}) {
            assertEquals("123.0", DecimalFactory.of(0,1).fromDouble(123.0, roundingMode).toString());
            assertEquals("1000000000000000000", DecimalFactory.of(0,0).fromDouble(1e18, roundingMode).toString());
            assertEquals("NaN", DecimalFactory.of(0,1).fromDouble(1e18, roundingMode).toString());
            assertEquals("NaN", DecimalFactory.of(0,1).fromDouble(Double.POSITIVE_INFINITY, roundingMode).toString());
            assertEquals("NaN", DecimalFactory.of(0,1).fromDouble(Double.NEGATIVE_INFINITY, roundingMode).toString());
            assertEquals("NaN", DecimalFactory.of(0,1).fromDouble(Double.NaN, roundingMode).toString());
        }

        assertEquals("0", DecimalFactory.of(0,0).fromDouble(0.0, RoundingMode.DOWN).toString());
        assertEquals("0", DecimalFactory.of(0,0).fromDouble(0.5, RoundingMode.DOWN).toString());
        assertEquals("0", DecimalFactory.of(0,0).fromDouble(-0.5, RoundingMode.DOWN).toString());
        assertEquals("1", DecimalFactory.of(0,0).fromDouble(1.5, RoundingMode.DOWN).toString());
        assertEquals("-1", DecimalFactory.of(0,0).fromDouble(-1.5, RoundingMode.DOWN).toString());
        assertEquals("1", DecimalFactory.of(0,0).fromDouble(1.7, RoundingMode.DOWN).toString());
        assertEquals("-1", DecimalFactory.of(0,0).fromDouble(-1.7, RoundingMode.DOWN).toString());

        assertEquals("0", DecimalFactory.of(0,0).fromDouble(0.0, RoundingMode.UP).toString());
        assertEquals("1", DecimalFactory.of(0,0).fromDouble(0.5, RoundingMode.UP).toString());
        assertEquals("-1", DecimalFactory.of(0,0).fromDouble(-0.5, RoundingMode.UP).toString());
        assertEquals("2", DecimalFactory.of(0,0).fromDouble(1.5, RoundingMode.UP).toString());
        assertEquals("-2", DecimalFactory.of(0,0).fromDouble(-1.5, RoundingMode.UP).toString());
        assertEquals("2", DecimalFactory.of(0,0).fromDouble(1.7, RoundingMode.UP).toString());
        assertEquals("-2", DecimalFactory.of(0,0).fromDouble(-1.7, RoundingMode.UP).toString());

        assertEquals("0", DecimalFactory.of(0,0).fromDouble(0.0, RoundingMode.FLOOR).toString());
        assertEquals("0", DecimalFactory.of(0,0).fromDouble(0.5, RoundingMode.FLOOR).toString());
        assertEquals("-1", DecimalFactory.of(0,0).fromDouble(-0.5, RoundingMode.FLOOR).toString());
        assertEquals("1", DecimalFactory.of(0,0).fromDouble(1.5, RoundingMode.FLOOR).toString());
        assertEquals("-2", DecimalFactory.of(0,0).fromDouble(-1.5, RoundingMode.FLOOR).toString());
        assertEquals("1", DecimalFactory.of(0,0).fromDouble(1.7, RoundingMode.FLOOR).toString());
        assertEquals("-2", DecimalFactory.of(0,0).fromDouble(-1.7, RoundingMode.FLOOR).toString());

        assertEquals("0", DecimalFactory.of(0,0).fromDouble(0.0, RoundingMode.CEILING).toString());
        assertEquals("1", DecimalFactory.of(0,0).fromDouble(0.5, RoundingMode.CEILING).toString());
        assertEquals("0", DecimalFactory.of(0,0).fromDouble(-0.5, RoundingMode.CEILING).toString());
        assertEquals("2", DecimalFactory.of(0,0).fromDouble(1.5, RoundingMode.CEILING).toString());
        assertEquals("-1", DecimalFactory.of(0,0).fromDouble(-1.5, RoundingMode.CEILING).toString());
        assertEquals("2", DecimalFactory.of(0,0).fromDouble(1.7, RoundingMode.CEILING).toString());
        assertEquals("-1", DecimalFactory.of(0,0).fromDouble(-1.7, RoundingMode.CEILING).toString());

        assertEquals("0", DecimalFactory.of(0,0).fromDouble(0.0, RoundingMode.HALF_EVEN).toString());
        assertEquals("0", DecimalFactory.of(0,0).fromDouble(0.5, RoundingMode.HALF_EVEN).toString());
        assertEquals("0", DecimalFactory.of(0,0).fromDouble(-0.5, RoundingMode.HALF_EVEN).toString());
        assertEquals("2", DecimalFactory.of(0,0).fromDouble(1.5, RoundingMode.HALF_EVEN).toString());
        assertEquals("-2", DecimalFactory.of(0,0).fromDouble(-1.5, RoundingMode.HALF_EVEN).toString());
        assertEquals("2", DecimalFactory.of(0,0).fromDouble(1.7, RoundingMode.HALF_EVEN).toString());
        assertEquals("-2", DecimalFactory.of(0,0).fromDouble(-1.7, RoundingMode.HALF_EVEN).toString());
        assertEquals("1", DecimalFactory.of(0,0).fromDouble(1.3, RoundingMode.HALF_EVEN).toString());
        assertEquals("-1", DecimalFactory.of(0,0).fromDouble(-1.3, RoundingMode.HALF_EVEN).toString());
        //FIXME:
//        assertEquals(123.0, DecimalFactory.of(0, 1).set(1230).toDouble(), 0.0);
//        assertEquals(123.5, DecimalFactory.of(0, 1).set(1235).toDouble(), 0.0);
//        assertEquals(12.30, DecimalFactory.of(0, 2).set(1230).toDouble(), 0.0);
//        assertEquals(12.35, DecimalFactory.of(0, 2).set(1235).toDouble(), 0.0);
        assertTrue(Double.isNaN(DecimalFactory.of(0,2).set(NaN).toDouble()));
    }

    @Test
    public void testCompareTo() throws ParseException {
        assertEquals(0, DecimalFactory.of(0, 1).parse("123").compareTo(DecimalFactory.of(0, 1).parse("123")));
        assertTrue(DecimalFactory.of(0,1).parse("123").compareTo(DecimalFactory.of(0,1).parse("124")) < 0);
        assertTrue(DecimalFactory.of(0,1).parse("124").compareTo(DecimalFactory.of(0,1).parse("123")) > 0);

        assertEquals(0, DecimalFactory.of(0, 1).parse("123").compareTo(DecimalFactory.of(0, 3).parse("123")));
        assertTrue(DecimalFactory.of(0,1).parse("123").compareTo(DecimalFactory.of(0,3).parse("124")) < 0);
        assertTrue(DecimalFactory.of(0,1).parse("124").compareTo(DecimalFactory.of(0,3).parse("123")) > 0);

        assertEquals(0, DecimalFactory.of(0, 3).parse("123").compareTo(DecimalFactory.of(0, 1).parse("123")));
        assertTrue(DecimalFactory.of(0,3).parse("123").compareTo(DecimalFactory.of(0,1).parse("124")) < 0);
        assertTrue(DecimalFactory.of(0,3).parse("124").compareTo(DecimalFactory.of(0,1).parse("123")) > 0);

        assertEquals(0, DecimalFactory.of(0, 3).parse("0.0").compareTo(DecimalFactory.of(0, 1).parse("0")));
        assertTrue(DecimalFactory.of(0,3).parse("-0.1").compareTo(DecimalFactory.of(0,1).parse("0")) < 0);
        assertTrue(DecimalFactory.of(0,3).parse("0.1").compareTo(DecimalFactory.of(0,1).parse("0")) > 0);

        assertTrue(DecimalFactory.of(0,3).parse("0.123").compareTo(DecimalFactory.of(0,1).parse("12.0")) < 0);
        assertTrue(DecimalFactory.of(0,3).parse("1.123").compareTo(DecimalFactory.of(0,0).parse("1")) > 0);
        assertEquals(0, DecimalFactory.of(0, 3).parse("1.000").compareTo(DecimalFactory.of(0, 0).parse("1")));

        assertTrue(DecimalFactory.of(0,9).parse("NaN").compareTo(DecimalFactory.of(0,0).parse("-1000000000000000000")) < 0);
        assertTrue(DecimalFactory.of(0,9).parse("-1000000000").compareTo(DecimalFactory.of(0,0).parse("NaN")) > 0);
        assertEquals(0, DecimalFactory.of(0, 9).parse("NaN").compareTo(DecimalFactory.of(0, 0).parse("NaN")));

        assertEquals(0, DecimalFactory.of(0, 0).parse("9223372036854775807").compareTo(DecimalFactory.of(0, 0).parse("9223372036854775807")));
        assertTrue(DecimalFactory.of(0,0).parse("9223372036854775807").compareTo(DecimalFactory.of(0,0).parse("-9223372036854775807")) > 0);
        assertTrue(DecimalFactory.of(0,0).parse("-9223372036854775807").compareTo(DecimalFactory.of(0,0).parse("9223372036854775807")) < 0);
        assertEquals(0, DecimalFactory.of(0, 0).parse("-9223372036854775807").compareTo(DecimalFactory.of(0, 0).parse("-9223372036854775807")));

        assertTrue(DecimalFactory.of(0,0).parse("9223372036854775806").compareTo(DecimalFactory.of(0,1).parse("922337203685477580.0")) > 0);
        assertEquals(0, DecimalFactory.of(0, 0).parse("922337203685477580").compareTo(DecimalFactory.of(0, 1).parse("922337203685477580.0")));
        assertTrue(DecimalFactory.of(0,0).parse("922337203685477579").compareTo(DecimalFactory.of(0,1).parse("922337203685477580.0")) < 0);

        assertTrue(DecimalFactory.of(0,1).parse("922337203685477580.0").compareTo(DecimalFactory.of(0,0).parse("9223372036854775806")) < 0);
        assertEquals(0, DecimalFactory.of(0, 1).parse("922337203685477580.0").compareTo(DecimalFactory.of(0, 0).parse("922337203685477580")));
        assertTrue(DecimalFactory.of(0,1).parse("922337203685477580.0").compareTo(DecimalFactory.of(0,0).parse("922337203685477579")) > 0);

        assertTrue(DecimalFactory.of(0,0).parse("-9223372036854775806").compareTo(DecimalFactory.of(0,1).parse("-922337203685477580.0")) < 0);
        assertEquals(0, DecimalFactory.of(0, 0).parse("-922337203685477580").compareTo(DecimalFactory.of(0, 1).parse("-922337203685477580.0")));
        assertTrue(DecimalFactory.of(0,0).parse("-922337203685477579").compareTo(DecimalFactory.of(0,1).parse("-922337203685477580.0")) > 0);

        assertTrue(DecimalFactory.of(0,1).parse("-922337203685477580.0").compareTo(DecimalFactory.of(0,0).parse("-9223372036854775806")) > 0);
        assertEquals(0, DecimalFactory.of(0, 1).parse("-922337203685477580.0").compareTo(DecimalFactory.of(0, 0).parse("-922337203685477580")));
        assertTrue(DecimalFactory.of(0,1).parse("-922337203685477580.0").compareTo(DecimalFactory.of(0,0).parse("-922337203685477579")) < 0);

    }

    @Test
    public void testPlus() throws Exception {
        assertEquals("125.00", DecimalFactory.of(0,2).plus(DecimalFactory.of(123, 2), DecimalFactory.of(2, 2)).toString());
        assertEquals("121.00", DecimalFactory.of(0,2).plus(DecimalFactory.of(123, 2), DecimalFactory.of(-2, 2)).toString());
        assertEquals("-121.00", DecimalFactory.of(0,2).plus(DecimalFactory.of(-123, 2), DecimalFactory.of(2, 2)).toString());
        assertEquals("-125.00", DecimalFactory.of(0,2).plus(DecimalFactory.of(-123, 2), DecimalFactory.of(-2, 2)).toString());

        assertEquals("125.00000000", DecimalFactory.of(0, 8).plus(DecimalFactory.of(123, 2), DecimalFactory.of(2, 2), RoundingMode.UNNECESSARY).toString());
        assertEquals("121.00000000", DecimalFactory.of(0, 8).plus(DecimalFactory.of(123, 2), DecimalFactory.of(-2, 2), RoundingMode.UNNECESSARY).toString());
        assertEquals("-121.00000000", DecimalFactory.of(0, 8).plus(DecimalFactory.of(-123, 2), DecimalFactory.of(2, 2), RoundingMode.UNNECESSARY).toString());
        assertEquals("-125.00000000", DecimalFactory.of(0, 8).plus(DecimalFactory.of(-123, 2), DecimalFactory.of(-2, 2), RoundingMode.UNNECESSARY).toString());

        assertEquals("125.00", DecimalFactory.of(0,2).plus(DecimalFactory.of(123, 8), DecimalFactory.of(2, 8), RoundingMode.UNNECESSARY).toString());
        assertEquals("121.00", DecimalFactory.of(0,2).plus(DecimalFactory.of(123, 8), DecimalFactory.of(-2, 8), RoundingMode.UNNECESSARY).toString());
        assertEquals("-121.00", DecimalFactory.of(0,2).plus(DecimalFactory.of(-123, 8), DecimalFactory.of(2, 8), RoundingMode.UNNECESSARY).toString());
        assertEquals("-125.00", DecimalFactory.of(0,2).plus(DecimalFactory.of(-123, 8), DecimalFactory.of(-2, 8), RoundingMode.UNNECESSARY).toString());

        assertEquals("125.00", DecimalFactory.of(0,2).plus(123, 2).toString());
        assertEquals("121.00", DecimalFactory.of(0,2).plus(123, -2).toString());
        assertEquals("-121.00", DecimalFactory.of(0,2).plus(-123, 2).toString());
        assertEquals("-125.00", DecimalFactory.of(0,2).plus(-123, -2).toString());

        // Addition overflows:  2 * 92233720368.54775807 = 184467440737.09551614
        assertEquals("184467440737.10", DecimalFactory.of(0,2).plus(DecimalFactory.of(92233720368.54775807, 8), DecimalFactory.of(92233720368.54775807, 8), RoundingMode.UP).toString());
        //FIXME:
//        assertEquals("0.00", DecimalFactory.of(0,2).plus(DecimalFactory.of(92233720368.54775807, 8), DecimalFactory.of(-92233720368.54775807, 8), RoundingMode.UNNECESSARY).toString());
//        assertEquals("0.00", DecimalFactory.of(0,2).plus(DecimalFactory.of(-92233720368.54775807, 8), DecimalFactory.of(92233720368.54775807, 8), RoundingMode.UNNECESSARY).toString());
//        assertEquals("-184467440737.10", DecimalFactory.of(0,2).plus(DecimalFactory.of(-92233720368.54775807, 8), DecimalFactory.of(-92233720368.54775807, 8), RoundingMode.UP).toString());
        // same, round DOWN
//        assertEquals("184467440737.09", DecimalFactory.of(0,2).plus(DecimalFactory.of(92233720368.54775807, 8), DecimalFactory.of(92233720368.54775807, 8)).toString());
//        assertEquals("-184467440737.09", DecimalFactory.of(0,2).plus(DecimalFactory.of(-92233720368.54775807, 8), DecimalFactory.of(-92233720368.54775807, 8)).toString());

        // Scaling overflows (can not be prevented)
//        assertEquals("NaN", DecimalFactory.of(0, 8).plus(DecimalFactory.of(92233720368547758.07, 2), DecimalFactory.of(92233720368547758.07, 2), RoundingMode.UP).toString());
//        assertEquals("0.00000000", DecimalFactory.of(0, 8).plus(DecimalFactory.of(92233720368547758.07, 2), DecimalFactory.of(-92233720368547758.07, 2), RoundingMode.UP).toString());
//        assertEquals("0.00000000", DecimalFactory.of(0, 8).plus(DecimalFactory.of(-92233720368547758.07, 2), DecimalFactory.of(92233720368547758.07, 2), RoundingMode.UP).toString());
//        assertEquals("NaN", DecimalFactory.of(0, 8).plus(DecimalFactory.of(-92233720368547758.07, 2), DecimalFactory.of(-92233720368547758.07, 2), RoundingMode.UP).toString());
//
//        // Addition AFTER Scaling overflows (can not be prevented)
//        assertEquals("92233720368.54000000", DecimalFactory.of(0, 8).plus(DecimalFactory.of(92233720368.54, 2), DecimalFactory.of(0, 2), RoundingMode.UP).toString());
//        assertEquals("92233720368.54000000", DecimalFactory.of(0, 8).plus(DecimalFactory.of(0, 2), DecimalFactory.of(92233720368.54, 2), RoundingMode.UP).toString());
//        assertEquals("NaN", DecimalFactory.of(0, 8).plus(DecimalFactory.of(92233720368.54, 2), DecimalFactory.of(92233720368.54, 2), RoundingMode.UP).toString());
//        assertEquals("0.00000000", DecimalFactory.of(0, 8).plus(DecimalFactory.of(92233720368.54, 2), DecimalFactory.of(-92233720368.54, 2), RoundingMode.UP).toString());
//        assertEquals("0.00000000", DecimalFactory.of(0, 8).plus(DecimalFactory.of(-92233720368.54, 2), DecimalFactory.of(92233720368.54, 2), RoundingMode.UP).toString());
//        assertEquals("NaN", DecimalFactory.of(0, 8).plus(DecimalFactory.of(-92233720368.54, 2), DecimalFactory.of(-92233720368.54, 2), RoundingMode.UP).toString());

        // "add" of the same type is the same as "plus"
        assertEquals("125.00", DecimalFactory.of(123, 2).add(DecimalFactory.of(2, 2)).toString());
        assertEquals("121.00", DecimalFactory.of(123, 2).add(DecimalFactory.of(-2, 2)).toString());
        assertEquals("-121.00", DecimalFactory.of(-123, 2).add(DecimalFactory.of(2, 2)).toString());
        assertEquals("-125.00", DecimalFactory.of(-123, 2).add(DecimalFactory.of(-2, 2)).toString());
    }

    @Test
    public void testAdd() throws Exception {
        assertEquals("125.00", DecimalFactory.of(123, 2).add(DecimalFactory.of(2, 2)).toString());
        assertEquals("121.00", DecimalFactory.of(123, 2).add(DecimalFactory.of(-2, 2)).toString());
        assertEquals("-121.00", DecimalFactory.of(-123, 2).add(DecimalFactory.of(2, 2)).toString());
        assertEquals("-125.00", DecimalFactory.of(-123, 2).add(DecimalFactory.of(-2, 2)).toString());

        assertEquals("125.00000000", DecimalFactory.of(123, 8).add(DecimalFactory.of(2, 2), RoundingMode.UNNECESSARY).toString());
        assertEquals("121.00000000", DecimalFactory.of(123, 8).add(DecimalFactory.of(-2, 2), RoundingMode.UNNECESSARY).toString());
        assertEquals("-121.00000000", DecimalFactory.of(-123, 8).add(DecimalFactory.of(2, 2), RoundingMode.UNNECESSARY).toString());
        assertEquals("-125.00000000", DecimalFactory.of(-123, 8).add(DecimalFactory.of(-2, 2), RoundingMode.UNNECESSARY).toString());

        assertEquals("125.00", DecimalFactory.of(123, 2).add(DecimalFactory.of(2, 8), RoundingMode.UNNECESSARY).toString());
        assertEquals("121.00", DecimalFactory.of(123, 2).add(DecimalFactory.of(-2, 8), RoundingMode.UNNECESSARY).toString());
        assertEquals("-121.00", DecimalFactory.of(-123, 2).add(DecimalFactory.of(2, 8), RoundingMode.UNNECESSARY).toString());
        assertEquals("-125.00", DecimalFactory.of(-123, 2).add(DecimalFactory.of(-2, 8), RoundingMode.UNNECESSARY).toString());

        assertEquals("123.01", DecimalFactory.of(123, 2).add(DecimalFactory.of(0.00000001, 8), RoundingMode.UP).toString());
        assertEquals("123.00", DecimalFactory.of(123, 2).add(DecimalFactory.of(-0.00000001, 8), RoundingMode.UP).toString());
        assertEquals("-123.00", DecimalFactory.of(-123, 2).add(DecimalFactory.of(0.00000001, 8), RoundingMode.UP).toString());
        assertEquals("-123.01", DecimalFactory.of(-123, 2).add(DecimalFactory.of(-0.00000001, 8), RoundingMode.UP).toString());

        assertEquals("123.00", DecimalFactory.of(123, 2).add(DecimalFactory.of(0.00000001, 8)).toString());
        assertEquals("122.99", DecimalFactory.of(123, 2).add(DecimalFactory.of(-0.00000001, 8)).toString());
        assertEquals("-122.99", DecimalFactory.of(-123, 2).add(DecimalFactory.of(0.00000001, 8)).toString());
        assertEquals("-123.00", DecimalFactory.of(-123, 2).add(DecimalFactory.of(-0.00000001, 8)).toString());

        assertEquals("125.00000000", DecimalFactory.of(123, 8).add(2).toString());
        assertEquals("121.00000000", DecimalFactory.of(123, 8).add(-2).toString());
        assertEquals("-121.00000000", DecimalFactory.of(-123, 8).add(2).toString());
        assertEquals("-125.00000000", DecimalFactory.of(-123, 8).add(-2).toString());

        // overflow while down-scaling
        assertEquals("NaN", DecimalFactory.of(92233720368547758.07, 2).add(DecimalFactory.of(0.01, 8), RoundingMode.UNNECESSARY).toString());
        assertEquals("92233720368547758.06", DecimalFactory.of(92233720368547758.07, 2).add(DecimalFactory.of(-0.01, 8), RoundingMode.UNNECESSARY).toString());
        //FIXME: //assertEquals("-92233720368547758.06", DecimalFactory.of(-92233720368547758.07, 2).add(DecimalFactory.of(0.01, 8), RoundingMode.UNNECESSARY).toString());
        assertEquals("NaN", DecimalFactory.of(-92233720368547758.07, 2).add(DecimalFactory.of(-0.01, 8), RoundingMode.UNNECESSARY).toString());

        // overflow while rounding
        assertEquals("NaN", DecimalFactory.of(92233720368547758.07, 2).add(DecimalFactory.of(0.001, 8), RoundingMode.UP).toString());
        assertEquals("NaN", DecimalFactory.of(-92233720368547758.07, 2).add(DecimalFactory.of(-0.001, 8), RoundingMode.UP).toString());
        assertEquals("92233720368547758.07", DecimalFactory.of(92233720368547758.07, 2).add(DecimalFactory.of(0.001, 8), RoundingMode.DOWN).toString());
        //FIXME: //assertEquals("-92233720368547758.07", DecimalFactory.of(-92233720368547758.07, 2).add(DecimalFactory.of(-0.001, 8), RoundingMode.DOWN).toString());

        // overflow while adding after up-scale
        assertEquals("NaN", DecimalFactory.of(92233720368.54775807, 8).add(1).toString());
        assertEquals("92233720367.54775807", DecimalFactory.of(92233720368.54775807, 8).add(-1).toString());
        //FIXME: //assertEquals("-92233720367.54775807", DecimalFactory.of(-92233720368.54775807, 8).add(1).toString());
        assertEquals("NaN", DecimalFactory.of(-92233720368.54775807, 8).add(-1).toString());

        // overflow while up-scaling (not always NaN)
        assertEquals("NaN", DecimalFactory.of(92233720368.54775807, 8).add(100000000000L).toString());
        //FIXME: //assertEquals("-7766279631.45224193", DecimalFactory.of(92233720368.54775807, 8).add(-100000000000L).toString());
        //FIXME: //assertEquals("7766279631.45224193", DecimalFactory.of(-92233720368.54775807, 8).add(100000000000L).toString());
        assertEquals("NaN", DecimalFactory.of(-92233720368.54775807, 8).add(-100000000000L).toString());
    }

    @Test
    public void testMinus() throws Exception {
        assertEquals("125.00", DecimalFactory.of(0,2).subtract(DecimalFactory.of(123, 2), DecimalFactory.of(-2, 2)).toString());
        assertEquals("121.00", DecimalFactory.of(0,2).subtract(DecimalFactory.of(123, 2), DecimalFactory.of(2, 2)).toString());
        assertEquals("-121.00", DecimalFactory.of(0,2).subtract(DecimalFactory.of(-123, 2), DecimalFactory.of(-2, 2)).toString());
        assertEquals("-125.00", DecimalFactory.of(0,2).subtract(DecimalFactory.of(-123, 2), DecimalFactory.of(2, 2)).toString());

        assertEquals("125.00000000", DecimalFactory.of(0, 8).subtract(DecimalFactory.of(123, 2), DecimalFactory.of(-2, 2), RoundingMode.UNNECESSARY).toString());
        assertEquals("121.00000000", DecimalFactory.of(0, 8).subtract(DecimalFactory.of(123, 2), DecimalFactory.of(2, 2), RoundingMode.UNNECESSARY).toString());
        assertEquals("-121.00000000", DecimalFactory.of(0, 8).subtract(DecimalFactory.of(-123, 2), DecimalFactory.of(-2, 2), RoundingMode.UNNECESSARY).toString());
        assertEquals("-125.00000000", DecimalFactory.of(0, 8).subtract(DecimalFactory.of(-123, 2), DecimalFactory.of(2, 2), RoundingMode.UNNECESSARY).toString());

        assertEquals("125.00", DecimalFactory.of(0,2).subtract(DecimalFactory.of(123, 8), DecimalFactory.of(-2, 8), RoundingMode.UNNECESSARY).toString());
        assertEquals("121.00", DecimalFactory.of(0,2).subtract(DecimalFactory.of(123, 8), DecimalFactory.of(2, 8), RoundingMode.UNNECESSARY).toString());
        assertEquals("-121.00", DecimalFactory.of(0,2).subtract(DecimalFactory.of(-123, 8), DecimalFactory.of(-2, 8), RoundingMode.UNNECESSARY).toString());
        assertEquals("-125.00", DecimalFactory.of(0,2).subtract(DecimalFactory.of(-123, 8), DecimalFactory.of(2, 8), RoundingMode.UNNECESSARY).toString());

        // Addition overflows:  2 * 92233720368.54775807 = 184467440737.09551614
        //FIXME:
//        assertEquals("184467440737.10", DecimalFactory.of(0,2).subtract(DecimalFactory.of(92233720368.54775807, 8), DecimalFactory.of(-92233720368.54775807, 8), RoundingMode.UP).toString());
//        assertEquals("0.00", DecimalFactory.of(0,2).subtract(DecimalFactory.of(92233720368.54775807, 8), DecimalFactory.of(92233720368.54775807, 8), RoundingMode.UNNECESSARY).toString());
//        assertEquals("0.00", DecimalFactory.of(0,2).subtract(DecimalFactory.of(-92233720368.54775807, 8), DecimalFactory.of(-92233720368.54775807, 8), RoundingMode.UNNECESSARY).toString());
//        assertEquals("-184467440737.10", DecimalFactory.of(0,2).subtract(DecimalFactory.of(-92233720368.54775807, 8), DecimalFactory.of(92233720368.54775807, 8), RoundingMode.UP).toString());
//        // same, rounding DOWN
//        assertEquals("184467440737.09", DecimalFactory.of(0,2).subtract(DecimalFactory.of(92233720368.54775807, 8), DecimalFactory.of(-92233720368.54775807, 8)).toString());
//        assertEquals("-184467440737.09", DecimalFactory.of(0,2).subtract(DecimalFactory.of(-92233720368.54775807, 8), DecimalFactory.of(92233720368.54775807, 8)).toString());
//
//        // Scaling overflows (can not be prevented)
//        assertEquals("NaN", DecimalFactory.of(0, 8).subtract(DecimalFactory.of(92233720368547758.07, 2), DecimalFactory.of(-92233720368547758.07, 2), RoundingMode.UP).toString());
//        assertEquals("0.00000000", DecimalFactory.of(0, 8).subtract(DecimalFactory.of(92233720368547758.07, 2), DecimalFactory.of(92233720368547758.07, 2), RoundingMode.UP).toString());
//        assertEquals("0.00000000", DecimalFactory.of(0, 8).subtract(DecimalFactory.of(-92233720368547758.07, 2), DecimalFactory.of(-92233720368547758.07, 2), RoundingMode.UP).toString());
//        assertEquals("NaN", DecimalFactory.of(0, 8).subtract(DecimalFactory.of(-92233720368547758.07, 2), DecimalFactory.of(92233720368547758.07, 2), RoundingMode.UP).toString());
//
//        // Addition AFTER Scaling overflows (can not be prevented)
//        assertEquals("92233720368.54000000", DecimalFactory.of(0, 8).subtract(DecimalFactory.of(92233720368.54, 2), DecimalFactory.of(0, 2), RoundingMode.UP).toString());
//        assertEquals("92233720368.54000000", DecimalFactory.of(0, 8).subtract(DecimalFactory.of(0, 2), DecimalFactory.of(-92233720368.54, 2), RoundingMode.UP).toString());
//        assertEquals("NaN", DecimalFactory.of(0, 8).subtract(DecimalFactory.of(92233720368.54, 2), DecimalFactory.of(-92233720368.54, 2), RoundingMode.UP).toString());
//        assertEquals("0.00000000", DecimalFactory.of(0, 8).subtract(DecimalFactory.of(92233720368.54, 2), DecimalFactory.of(92233720368.54, 2), RoundingMode.UP).toString());
//        assertEquals("0.00000000", DecimalFactory.of(0, 8).subtract(DecimalFactory.of(-92233720368.54, 2), DecimalFactory.of(-92233720368.54, 2), RoundingMode.UP).toString());
//        assertEquals("NaN", DecimalFactory.of(0, 8).subtract(DecimalFactory.of(-92233720368.54, 2), DecimalFactory.of(92233720368.54, 2), RoundingMode.UP).toString());

        // "subtract" of the same type is the same as "minus"
        assertEquals("125.00", DecimalFactory.of(123, 2).subtract(DecimalFactory.of(-2, 2)).toString());
        assertEquals("121.00", DecimalFactory.of(123, 2).subtract(DecimalFactory.of(2, 2)).toString());
        assertEquals("-121.00", DecimalFactory.of(-123, 2).subtract(DecimalFactory.of(-2, 2)).toString());
        assertEquals("-125.00", DecimalFactory.of(-123, 2).subtract(DecimalFactory.of(2, 2)).toString());
    }

    @Test
    public void testSubtract() throws Exception {
        assertEquals("125.00", DecimalFactory.of(123, 2).subtract(DecimalFactory.of(-2, 2)).toString());
        assertEquals("121.00", DecimalFactory.of(123, 2).subtract(DecimalFactory.of(2, 2)).toString());
        assertEquals("-121.00", DecimalFactory.of(-123, 2).subtract(DecimalFactory.of(-2, 2)).toString());
        assertEquals("-125.00", DecimalFactory.of(-123, 2).subtract(DecimalFactory.of(2, 2)).toString());

        assertEquals("125.00000000", DecimalFactory.of(123, 8).subtract(DecimalFactory.of(-2, 2), RoundingMode.UNNECESSARY).toString());
        assertEquals("121.00000000", DecimalFactory.of(123, 8).subtract(DecimalFactory.of(2, 2), RoundingMode.UNNECESSARY).toString());
        assertEquals("-121.00000000", DecimalFactory.of(-123, 8).subtract(DecimalFactory.of(-2, 2), RoundingMode.UNNECESSARY).toString());
        assertEquals("-125.00000000", DecimalFactory.of(-123, 8).subtract(DecimalFactory.of(2, 2), RoundingMode.UNNECESSARY).toString());

        assertEquals("125.00", DecimalFactory.of(123, 2).subtract(DecimalFactory.of(-2, 8), RoundingMode.UNNECESSARY).toString());
        assertEquals("121.00", DecimalFactory.of(123, 2).subtract(DecimalFactory.of(2, 8), RoundingMode.UNNECESSARY).toString());
        assertEquals("-121.00", DecimalFactory.of(-123, 2).subtract(DecimalFactory.of(-2, 8), RoundingMode.UNNECESSARY).toString());
        assertEquals("-125.00", DecimalFactory.of(-123, 2).subtract(DecimalFactory.of(2, 8), RoundingMode.UNNECESSARY).toString());

        assertEquals("123.01", DecimalFactory.of(123, 2).subtract(DecimalFactory.of(-0.00000001, 8), RoundingMode.UP).toString());
        assertEquals("123.00", DecimalFactory.of(123, 2).subtract(DecimalFactory.of(0.00000001, 8), RoundingMode.UP).toString());
        assertEquals("-123.00", DecimalFactory.of(-123, 2).subtract(DecimalFactory.of(-0.00000001, 8), RoundingMode.UP).toString());
        assertEquals("-123.01", DecimalFactory.of(-123, 2).subtract(DecimalFactory.of(0.00000001, 8), RoundingMode.UP).toString());

        //assertEquals("123.00", DecimalFactory.of(123, 2).subtract(DecimalFactory.of(-0.00000001, 8)).toString());
        assertEquals(122.99, DecimalFactory.of(123, 2).subtract(DecimalFactory.of(0.00000001, 8)).toDouble(), 0.0);
        assertEquals(-122.99, DecimalFactory.of(-123, 2).subtract(DecimalFactory.of(-0.00000001, 8)).toDouble(), 0.0);
        assertEquals(-123.00, DecimalFactory.of(-123, 2).subtract(DecimalFactory.of(0.00000001, 8)).toDouble(), 0.00);

        assertEquals("125.00000000", DecimalFactory.of(123, 8).subtract(-2).toString());
        assertEquals("121.00000000", DecimalFactory.of(123, 8).subtract(2).toString());
        assertEquals("-121.00000000", DecimalFactory.of(-123, 8).subtract(-2).toString());
        assertEquals("-125.00000000", DecimalFactory.of(-123, 8).subtract(2).toString());

        // overflow while down-scaling
        assertEquals("NaN", DecimalFactory.of(92233720368547758.07, 2).subtract(DecimalFactory.of(-0.01, 8), RoundingMode.UNNECESSARY).toString());
        assertEquals("92233720368547758.06", DecimalFactory.of(92233720368547758.07, 2).subtract(DecimalFactory.of(0.01, 8), RoundingMode.UNNECESSARY).toString());
        assertEquals("-92233720368547758.06", DecimalFactory.of(-92233720368547758.07, 2).subtract(DecimalFactory.of(-0.01, 8), RoundingMode.UNNECESSARY).toString());
        assertEquals("NaN", DecimalFactory.of(-92233720368547758.07, 2).subtract(DecimalFactory.of(0.01, 8), RoundingMode.UNNECESSARY).toString());

        // overflow while rounding
        assertEquals("NaN", DecimalFactory.of(92233720368547758.07, 2).subtract(DecimalFactory.of(-0.001, 8), RoundingMode.UP).toString());
        assertEquals("NaN", DecimalFactory.of(-92233720368547758.07, 2).subtract(DecimalFactory.of(0.001, 8), RoundingMode.UP).toString());
        assertEquals("92233720368547758.07", DecimalFactory.of(92233720368547758.07, 2).subtract(DecimalFactory.of(-0.001, 8), RoundingMode.DOWN).toString());
        assertEquals("-92233720368547758.07", DecimalFactory.of(-92233720368547758.07, 2).subtract(DecimalFactory.of(0.001, 8), RoundingMode.DOWN).toString());

        // overflow while subtracting after up-scale
        assertEquals("NaN", DecimalFactory.of(92233720368.54775807, 8).subtract(-1).toString());
        assertEquals("92233720367.54775807", DecimalFactory.of(92233720368.54775807, 8).subtract(1).toString());
        assertEquals("-92233720367.54775807", DecimalFactory.of(-92233720368.54775807, 8).subtract(-1).toString());
        assertEquals("NaN", DecimalFactory.of(-92233720368.54775807, 8).subtract(1).toString());

        // overflow while up-scaling (not always NaN)
        assertEquals("NaN", DecimalFactory.of(92233720368.54775807, 8).subtract(-100000000000L).toString());
        assertEquals("-7766279631.45224193", DecimalFactory.of(92233720368.54775807, 8).subtract(100000000000L).toString());
        assertEquals("7766279631.45224193", DecimalFactory.of(-92233720368.54775807, 8).subtract(-100000000000L).toString());
        assertEquals("NaN", DecimalFactory.of(-92233720368.54775807, 8).subtract(100000000000L).toString());

        assertEquals("0", DecimalFactory.of(0,0).parse("1").add(DecimalFactory.of(0,1).parse("-0.1"), RoundingMode.DOWN).toString());
    }

//    @Test
//    public void testProduct() throws Exception {
//        assertEquals("1230.00", quantity.product(DecimalFactory.of(123, 2), DecimalFactory.of(10, 2), RoundingMode.UNNECESSARY).toString());
//        assertEquals("-1230.00", quantity.product(DecimalFactory.of(123, 2), DecimalFactory.of(-10, 2), RoundingMode.UNNECESSARY).toString());
//        assertEquals("-1230.00", quantity.product(DecimalFactory.of(-123, 2), DecimalFactory.of(10, 2), RoundingMode.UNNECESSARY).toString());
//        assertEquals("1230.00", quantity.product(DecimalFactory.of(-123, 2), DecimalFactory.of(-10, 2), RoundingMode.UNNECESSARY).toString());
//
//        assertEquals("1230.00", quantity.product(DecimalFactory.of(123, 8), DecimalFactory.of(10, 8), RoundingMode.UNNECESSARY).toString());
//        assertEquals("-1230.00", quantity.product(DecimalFactory.of(123, 8), DecimalFactory.of(-10, 8), RoundingMode.UNNECESSARY).toString());
//        assertEquals("-1230.00", quantity.product(DecimalFactory.of(-123, 8), DecimalFactory.of(10, 8), RoundingMode.UNNECESSARY).toString());
//        assertEquals("1230.00", quantity.product(DecimalFactory.of(-123, 8), DecimalFactory.of(-10, 8), RoundingMode.UNNECESSARY).toString());
//
//        assertEquals("1230.00000000", price.product(DecimalFactory.of(123, 2), DecimalFactory.of(10, 2), RoundingMode.UNNECESSARY).toString());
//        assertEquals("-1230.00000000", price.product(DecimalFactory.of(123, 2), DecimalFactory.of(-10, 2), RoundingMode.UNNECESSARY).toString());
//        assertEquals("-1230.00000000", price.product(DecimalFactory.of(-123, 2), DecimalFactory.of(10, 2), RoundingMode.UNNECESSARY).toString());
//        assertEquals("1230.00000000", price.product(DecimalFactory.of(-123, 2), DecimalFactory.of(-10, 2), RoundingMode.UNNECESSARY).toString());
//
//        assertEquals("1230.01", quantity.product(DecimalFactory.of(123, 8), price("10.00000001"), RoundingMode.UP).toString());
//        assertEquals("-1230.01", quantity.product(DecimalFactory.of(123, 8), price("-10.00000001"), RoundingMode.UP).toString());
//        assertEquals("-1230.01", quantity.product(DecimalFactory.of(-123, 8), price("10.00000001"), RoundingMode.UP).toString());
//        assertEquals("1230.01", quantity.product(DecimalFactory.of(-123, 8), price("-10.00000001"), RoundingMode.UP).toString());
//
//        assertEquals("1230.00", quantity.productRD(DecimalFactory.of(123, 8), price("10.00000001")).toString());
//        assertEquals("-1230.00", quantity.productRD(DecimalFactory.of(123, 8), price("-10.00000001")).toString());
//        assertEquals("-1230.00", quantity.productRD(DecimalFactory.of(-123, 8), price("10.00000001")).toString());
//        assertEquals("1230.00", quantity.productRD(DecimalFactory.of(-123, 8), price("-10.00000001")).toString());
//
//        assertEquals("1230.00000000", price.product(123, 10).toString());
//        assertEquals("-1230.00000000", price.product(123, -10).toString());
//        assertEquals("-1230.00000000", price.product(-123, 10).toString());
//        assertEquals("1230.00000000", price.product(-123, -10).toString());
//
//        assertEquals("NaN", quantity.product(price("1000000000.00000000"), price("1000000000.00000000"), RoundingMode.UNNECESSARY).toString());
//        assertEquals("NaN", quantity.product(1000000000L, 1000000000L).toString());
//        assertEquals("10000000000000000.00", quantity.product(price("100000000.00000000"), price("100000000.00000000"), RoundingMode.UNNECESSARY).toString());
//        assertEquals("10000000000000000.00", quantity.product(100000000, 100000000).toString());
//        assertEquals("10000000000000000.00", quantity.product(10000000000L, 1000000).toString());
//        assertEquals("10000000000000000.00", quantity.product(1000000, 10000000000L).toString());
//        assertEquals("NaN", quantity.product(price("NaN"), price("100000000.00000000"), RoundingMode.UNNECESSARY).toString());
//        assertEquals("NaN", quantity.product(price("100000000.00000000"), price("NaN"), RoundingMode.UNNECESSARY).toString());
//    }

    @Test
    public void testMul() throws Exception {
        assertEquals("1230.00", DecimalFactory.of(123, 2).multiply(DecimalFactory.of(10, 8), RoundingMode.UNNECESSARY).toString());
        assertEquals("-1230.00", DecimalFactory.of(123, 2).multiply(DecimalFactory.of(-10, 8), RoundingMode.UNNECESSARY).toString());
        assertEquals("-1230.00", DecimalFactory.of(-123, 2).multiply(DecimalFactory.of(10, 8), RoundingMode.UNNECESSARY).toString());
        assertEquals("1230.00", DecimalFactory.of(-123, 2).multiply(DecimalFactory.of(-10, 8), RoundingMode.UNNECESSARY).toString());

        assertEquals("1230.00000000", DecimalFactory.of(123, 8).multiply(DecimalFactory.of(10, 2), RoundingMode.UNNECESSARY).toString());
        assertEquals("-1230.00000000", DecimalFactory.of(123, 8).multiply(DecimalFactory.of(-10, 2), RoundingMode.UNNECESSARY).toString());
        assertEquals("-1230.00000000", DecimalFactory.of(-123, 8).multiply(DecimalFactory.of(10, 2), RoundingMode.UNNECESSARY).toString());
        assertEquals("1230.00000000", DecimalFactory.of(-123, 8).multiply(DecimalFactory.of(-10, 2), RoundingMode.UNNECESSARY).toString());

        assertEquals("1230.00", DecimalFactory.of(123, 2).multiply(10).toString());
        assertEquals("-1230.00", DecimalFactory.of(123, 2).multiply(-10).toString());
        assertEquals("-1230.00", DecimalFactory.of(-123, 2).multiply(10).toString());
        assertEquals("1230.00", DecimalFactory.of(-123, 2).multiply(-10).toString());

//        assertEquals("NaN", price("NaN").mulRD(DecimalFactory.of(10, 2)).toString());
//        assertEquals("NaN", DecimalFactory.of(123, 8).multiply(quantity("NaN")).toString());

        assertEquals("0.11", DecimalFactory.of(0.33, 2).multiply(DecimalFactory.of(0.33, 2), RoundingMode.UP).toString());
        assertEquals("0.10", DecimalFactory.of(0.33, 2).multiply(DecimalFactory.of(0.33, 2), RoundingMode.DOWN).toString());
    }

//    @Test
//    public void testQuotient() throws Exception {
//        assertEquals("123.00", quantity.quotient(DecimalFactory.of(1230, 2), DecimalFactory.of(10, 2), RoundingMode.UNNECESSARY).toString());
//        assertEquals("-123.00", quantity.quotient(DecimalFactory.of(1230, 2), DecimalFactory.of(-10, 2), RoundingMode.UNNECESSARY).toString());
//        assertEquals("-123.00", quantity.quotient(DecimalFactory.of(-1230, 2), DecimalFactory.of(10, 2), RoundingMode.UNNECESSARY).toString());
//        assertEquals("123.00", quantity.quotient(DecimalFactory.of(-1230, 2), DecimalFactory.of(-10, 2), RoundingMode.UNNECESSARY).toString());
//
//        assertEquals("123.00", quantity.quotient(1230, 10, RoundingMode.UNNECESSARY).toString());
//        assertEquals("-123.00", quantity.quotient(1230, -10, RoundingMode.UNNECESSARY).toString());
//        assertEquals("-123.00", quantity.quotient(-1230, 10, RoundingMode.UNNECESSARY).toString());
//        assertEquals("123.00", quantity.quotient(-1230, -10, RoundingMode.UNNECESSARY).toString());
//
//        assertEquals("111.81", quantity.quotientRD(1230, 11).toString());
//        assertEquals("-111.81", quantity.quotientRD(1230, -11).toString());
//        assertEquals("-111.81", quantity.quotientRD(-1230, 11).toString());
//        assertEquals("111.81", quantity.quotientRD(-1230, -11).toString());
//
//        assertEquals("NaN", quantity.quotientRD(DecimalFactory.of(10000000000000000.00, 2), DecimalFactory.of(0.01, 2)).toString());
//        assertEquals("NaN", quantity.quotientRD(quantity("NaN"), DecimalFactory.of(10, 2)).toString());
//        assertEquals("NaN", quantity.quotientRD(DecimalFactory.of(1230, 2), quantity("NaN")).toString());
//    }

    @Test
    public void testDiv() throws Exception {
        assertEquals("123.00", DecimalFactory.of(1230, 2).divide(DecimalFactory.of(10, 2), RoundingMode.UNNECESSARY).toString());
        assertEquals("-123.00", DecimalFactory.of(1230, 2).divide(DecimalFactory.of(-10, 2), RoundingMode.UNNECESSARY).toString());
        assertEquals("-123.00", DecimalFactory.of(-1230, 2).divide(DecimalFactory.of(10, 2), RoundingMode.UNNECESSARY).toString());
        assertEquals("123.00", DecimalFactory.of(-1230, 2).divide(DecimalFactory.of(-10, 2), RoundingMode.UNNECESSARY).toString());

        assertEquals("111.81", DecimalFactory.of(1230, 2).divide(DecimalFactory.of(11, 2)).toString());
        assertEquals("-111.81", DecimalFactory.of(1230, 2).divide(DecimalFactory.of(-11, 2)).toString());
        assertEquals("-111.81", DecimalFactory.of(-1230, 2).divide(DecimalFactory.of(11, 2)).toString());
        assertEquals("111.81", DecimalFactory.of(-1230, 2).divide(DecimalFactory.of(-11, 2)).toString());

        assertEquals("123.00", DecimalFactory.of(1230, 2).divide(10, RoundingMode.UNNECESSARY).toString());
        assertEquals("-123.00", DecimalFactory.of(1230, 2).divide(-10, RoundingMode.UNNECESSARY).toString());
        assertEquals("-123.00", DecimalFactory.of(-1230, 2).divide(10, RoundingMode.UNNECESSARY).toString());
        assertEquals("123.00", DecimalFactory.of(-1230, 2).divide(-10, RoundingMode.UNNECESSARY).toString());

        assertEquals("111.81", DecimalFactory.of(1230, 2).divide(11).toString());
        assertEquals("-111.81", DecimalFactory.of(1230, 2).divide(-11).toString());
        assertEquals("-111.81", DecimalFactory.of(-1230, 2).divide(11).toString());
        assertEquals("111.81", DecimalFactory.of(-1230, 2).divide(-11).toString());

        assertEquals("NaN", DecimalFactory.of(10000000000000000.00, 2).divide(DecimalFactory.of(0.01, 2), RoundingMode.UNNECESSARY).toString());
//        assertEquals("NaN", quantity("NaN").div(DecimalFactory.of(10, 2), RoundingMode.UNNECESSARY).toString());
//        assertEquals("NaN", DecimalFactory.of(1230, 2).divide(quantity("NaN"), RoundingMode.UNNECESSARY).toString());
    }

    @Test
    public void powersOf2() throws Exception {
        for (int scale1 = 0; scale1 <= 9; ++scale1) {
            System.out.println(scale1);
            for (int scale2 = 0; scale2 <= 9; ++scale2) {
                for (int power1 = 0; power1 < 63; ++power1) {
                    for (int power2 = 0; power2 < 63; ++power2) {
                        comboTest(DecimalFactory.of(0, scale1).set(1L << power1), DecimalFactory.of(0, scale2).set(1L << power2));
                        comboTest(DecimalFactory.of(0, scale1).set(-1L << power1), DecimalFactory.of(0, scale2).set(1L << power2));
                        comboTest(DecimalFactory.of(0, scale1).set(1L << power1), DecimalFactory.of(0, scale2).set(-1L << power2));
                        comboTest(DecimalFactory.of(0, scale1).set(-1L << power1), DecimalFactory.of(0, scale2).set(-1L << power2));
                    }
                }
            }
        }
    }

    @Test
    public void powersOf10() throws Exception {
        for (int scale1 = 0; scale1 <= 9; ++scale1) {
            System.out.println(scale1);
            for (int scale2 = 0; scale2 <= 9; ++scale2) {
                for (int power1 = 0; power1 < 19; ++power1) {
                    for (int power2 = 0; power2 < 19; ++power2) {
                        long value1 = (long) Math.pow(10, power1);
                        long value2 = (long) Math.pow(10, power2);
                        comboTest(DecimalFactory.of(0, scale1).set(value1), DecimalFactory.of(0, scale2).set(value2));
                        comboTest(DecimalFactory.of(0, scale1).set(-value1), DecimalFactory.of(0, scale2).set(value2));
                        comboTest(DecimalFactory.of(0, scale1).set(value1), DecimalFactory.of(0, scale2).set(-value2));
                        comboTest(DecimalFactory.of(0, scale1).set(-value1), DecimalFactory.of(0, scale2).set(-value2));
                    }
                }
            }
        }
    }

    private void comboTest(Decimal value1, Decimal value2) {
        try {
            BigDecimal bd1 = BigDecimal.valueOf(value1.longValue()).divide(BigDecimal.TEN.pow(value1.getScale()));
            BigDecimal bd2 = BigDecimal.valueOf(value2.longValue()).divide(BigDecimal.TEN.pow(value2.getScale()));
            assertEquals(round(bd1.add(bd2), value1.getScale()), value1.clone().add(value2, RoundingMode.DOWN).longValue());
            assertEquals(round(bd1.subtract(bd2), value1.getScale()), value1.clone().subtract(value2, RoundingMode.DOWN).longValue());
            assertEquals(round(bd1.multiply(bd2), value1.getScale()), value1.clone().multiply(value2, RoundingMode.DOWN).longValue());
            assertEquals(round(bd1.divide(bd2), value1.getScale()), value1.clone().divide(value2, RoundingMode.DOWN).longValue());
            assertEquals(Integer.signum(bd1.compareTo(bd2)), Integer.signum(value1.compareTo(value2)));

            Decimal value3 = value1.clone().set(value2.longValue());
            BigDecimal bd3 = BigDecimal.valueOf(value3.longValue()).divide(BigDecimal.TEN.pow(value3.getScale()));
            assertEquals(round(bd1.add(bd3), value1.getScale()), value1.clone().plus(value1, value3, RoundingMode.DOWN).longValue());
            assertEquals(round(bd1.subtract(bd3), value1.getScale()), value1.clone().subtract(value1, value3, RoundingMode.DOWN).longValue());
//            assertEquals(round(bd1.multiply(bd3), value1.getScale()), value1.clone().product(value1, value3, RoundingMode.DOWN).longValue());
//            assertEquals(round(bd1.divide(bd3), value1.getScale()), value1.clone().quotient(value1, value3, RoundingMode.DOWN).longValue());
        } catch (AssertionError e) {
            throw new RuntimeException("Failed for " + value1 + " and " + value2 + ": " + e.getMessage(), e);
        }
    }

    private long round(BigDecimal value, int scale) {
        value = value.multiply(BigDecimal.TEN.pow(scale));
        if (value.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) > 0 ||
            value.compareTo(BigDecimal.valueOf(-Long.MAX_VALUE)) < 0) {
            return AbstractDecimal.NaN;
        }
        return value.longValue();
    }


    
    private void assertExceptionWhileParsing(String s) {
        try {
            DecimalFactory.of(0,0).parse(s);
            fail("Exception expected");
        } catch (ParseException e) {
        }
    }
}
