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

import java.math.RoundingMode;
import java.text.ParseException;

/**
 * Fixed point com.github.decimal, represented as a long mantissa and integer implied com.github.decimal points (dp) from 0 to 9, which is constant
 * for a concrete class (instance of same class must always have the same scale). Multiple subclases with different dps
 * can be created, e.g. Quantity with 2 dp and Price with 8 dp.
 * Supports basic arithmetic operations with full control of overflow and rounding.
 * Rounding must be explicitly provided if required with the exception of "RD" methods that round DOWN (fastest).
 * Non-allocating (unless explicitly specified).
 * <p>
 * Special value {@link #NaN} is used to represent an invalid operation, including<ul>
 *     <li>Overflow</li>
 *     <li>Unexpected rounding (in particular, underflow)</li>
 *     <li>Division by zero</li>
 * </ul>
 * Any operations involving {@link #NaN} return {@link #NaN}.
 * <p>
 * All operations can involve at most 2 different scales as arguments and result, they can be divided into:<ul>
 *     <li>Unary operations, taking a single argument and modifying this object, e.g. a.add(5)</li>
 *     <li>Binary operations, taking a 2 argument of the <b>same</b> scale and putting result to this object, e.g. a.plus(3, 4)</li>
 * </ul>
 * A long value can be used instead of a Decimal argument with 0 dp.
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals, see {@link #compareTo}
 *
 * @param <T>
 */
public abstract class AbstractDecimal<T extends AbstractDecimal> extends BaseDecimal implements Comparable<T>, Cloneable {
    public static final long NaN = Long.MIN_VALUE;

    /**
     * Implied com.github.decimal points, must be constant for the class, must be between 0 and 9.
     */
    protected abstract int getScale();
    protected abstract RoundingMode getDefaultRoundingMode();

    /**
     * true if the value is NaN.
     * All arithmetic operations with NaN returns NaN.
     */
    public boolean isNaN() {
        return getRaw() == NaN;
    }

    /**
     * Throws an exception (allocating) if the value is NaN
     */
    public boolean checkNotNaN() {
        if (isNaN()) {
            throw new ArithmeticException("Last operation was invalid (overflow or division by zero)");
        }
        return true;
    }

    /**
     * Change the sign of the number.
     */
    public void doNegate() {
        if (!isNaN()) {
            setRaw(-getRaw());
        }
    }

//    /**
//     * Copy the value from another com.github.decimal
//     * Rounding is required if the argument scale is greater than this scale.
//     */
//    public void set(AbstractDecimal<?> number, RoundingMode roundingMode) {
//
//    }

//    /**
//     * Copy the value from long (considering scale)
//     * No rounding required.
//     */
//    public void set(long number) {
//
//    }

//    /**
//     * Copy the value from another Decimal with the same scale.
//     * No rounding required.
//     */
//    public void set(AbstractDecimal<?> number) {
//        set(number, RoundingMode.UNNECESSARY);
//    }



    /**
     * Created a copy of the class with the same raw number.
     */
    @SuppressWarnings("unchecked")
    @Override
    public T clone() {
        try {
            return (T) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Unexpected", e);
        }
    }

    /**
     * Compares 2 values considering the scale ("0.123" < "12.0" although its not true for raw values)
     * {@link #NaN} is smaller than any other number irrespective of scale. Two {@link #NaN}s are equal to each other.
     *
     * Comparison can work across different concrete classes, and therefore can be inconsistent with {@link #equals},
     * which always returns "false" for different classes.
     */
    @Override
    public int compareTo(AbstractDecimal o) {
        if (isNaN()) {
            return o.isNaN() ? 0 : -1;
        } else if (o.isNaN()) {
            return isNaN() ? 0 : 1;
        }

        int scale = getScale() - o.getScale();
        if (scale >= 0) {
            long first = getRaw();
            long second = doScale(o.getRaw(), scale);
            return first < second || second == NaN && o.getRaw() > 0 ? -1 :
                    first > second ? 1 : 0;
        } else {
            long first = doScale(getRaw(), -scale);
            long second = o.getRaw();
            return first > second || first == NaN && getRaw() > 0 ? 1 :
                    first < second ? -1 : 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte byteValue() {
        return (byte) toLong(getDefaultRoundingMode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short shortValue() {
        return (short) toLong(getDefaultRoundingMode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int intValue() {
        return (int) toLong(getDefaultRoundingMode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long longValue() {
        return toLong(getDefaultRoundingMode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float floatValue() {
        return (float) toDouble();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double doubleValue() {
        return toDouble();
    }

    /**
     * Returns the whole part of the value, throws exception if {@link #NaN}
     */
    public long toLong(RoundingMode roundingMode) {
        if (isNaN()) {
            throw new ArithmeticException("NaN");
        }

        long raw = getRaw(); // will be overridden by remainder

        int scale = getScale();
        long result = round(downScale_63_31(raw, scale), getRaw(), POW10[scale], roundingMode);

        setRaw(raw);

        return result;
    }

    /**
     * Converts long to Decimal value. Can result in {@link #NaN} (if overflow)
     */
    public T fromLong(long value) {
        setRaw(doScale(value, getScale()));
        return (T) this;
    }

    public T fromLong(long value, int scale) {
        return fromLong(value, scale, getDefaultRoundingMode());
    }

    /**
     * Converts scaled long to Decimal value. Can result in {@link #NaN} (if overflow)
     * The result will be equal to value * 10^scale
     *
     * In particular, if scale == {@link #getScale} then {@link #setRaw}(value)
     *
     * @param value mantissa
     * @param scale com.github.decimal exponent
     * @param roundingMode required if scale is greater than {@link #getScale}
     */
    public T fromLong(long value, int scale, RoundingMode roundingMode) {
        if (scale < -18) {
            setRaw(value == 0 ? 0 : NaN);
            return (T) this;
        }
        if (getScale() - scale < -18) {
            // rounded zero
            setRaw(round(0, Long.signum(value), Long.MAX_VALUE, roundingMode));
            return (T) this;
        }

        scale = getScale() - scale; // no overflow guaranteed
        if (scale >= 0) {
            setRaw(scale > 18 ? NaN : doScale(value, scale));
            return (T) this;
        } else {
            scale = -scale;
            assert scale <= 18;
            long remainder = 0;
            long divisor = LONG_POW10[scale];
            if (scale > 9) {
                // double division by constant
                // the formula for final remainder derived from:
                // v / d1 / d2 = (q1 + r1 / d1) / d2 = q1 / d2 + r1 / d1d2 = q2 + r2 / d2 + r1 / d1d2 = q2 + (r2d1 + r1) / d1d2
                value = downScale_63_31(value, 9);
                remainder = getRaw() * 1000000000;
                scale -= 9;
            }
            value = downScale_63_31(value, scale);
            remainder += getRaw();

            setRaw(round(value, remainder, divisor, roundingMode));
            return (T) this;
        }
    }

    /**
     * Converts Decimal to floating-point number, returns {@link Double#NaN} if {@link #NaN}
     */
    public double toDouble() {
        return !isNaN() ? (double) getRaw() / POW10[getScale()] : Double.NaN;
    }

    public T fromDouble(double value) {
        return fromDouble(value, getDefaultRoundingMode());
    }

    /**
     * Converts doublie to Decimal value with the following rounding modes supported:<ul>
     *     <li>DOWN - simple cast</li>
     *     <li>FLOOR - uses {@link Math#floor}</li>
     *     <li>CEILING - uses {@link Math#ceil}</li>
     *     <li>HALF_EVEN - uses {@link Math#rint}</li>
     * </ul>
     */
    public T fromDouble(double value, RoundingMode roundingMode) {
        value *= POW10[getScale()];
        switch (roundingMode) {
            case DOWN:
                return setIntDouble(value);
            case UP:
                return setIntDouble(value > 0 ? Math.ceil(value) : Math.floor(value));
            case FLOOR:
                return setIntDouble(Math.floor(value));
            case CEILING:
                return setIntDouble(Math.ceil(value));
            case HALF_EVEN:
                return setIntDouble(Math.rint(value));
            case UNNECESSARY:
                return setIntDouble(value);
            default:
                throw new IllegalArgumentException("Unsupported rounding mode for double: " + roundingMode);
        }
    }

    private T setIntDouble(double value) {
        setRaw(value >= -Long.MAX_VALUE && value <= Long.MAX_VALUE ? (long) value : NaN);
        return (T) this;
    }

    /**
     * Converts to ASCII string. Allocating.
     * @see #toStringBuilder
     */
    public String toString() {
        return toStringBuilder(new StringBuilder(21)).toString();
    }

    /**
     * Converts to ASCII string. Shows number of dps as well, e.g. "1.00"
     * NaN values are displayed as "NaN"
     */
    public StringBuilder toStringBuilder(StringBuilder sb) {
        long raw = getRaw();
        if (raw == NaN) {
            sb.append("NaN");
            return sb;
        }

        if (raw < 0) {
            sb.append('-');
            raw = -raw;
        }

        int length = stringSize(raw);
        int scale = getScale();
        if (scale >= length) {
            sb.append(ZEROES[scale - length + 1]);
        }
        sb.append(raw);

        assert sb.length() > scale : "sb.length() >= length + scale - length + 1 = scale + 1 > scale";

        if (scale > 0) {
            sb.insert(sb.length() - scale, '.');
        }

        return sb;
    }

    /**
     * Parse a string (including NaN) and creates a value from it.
     * Unlike other methods, does NOT use NaN to indicate an error, uses ParseException instead.
     */
    public T parse(CharSequence charSequence) throws ParseException {
        return parse(charSequence, 0, charSequence.length());
    }

    public T parse(CharSequence charSequence, int offset, int length) throws ParseException {
        if (length == 0) {
            throw new ParseException("Empty string", 0);
        }

        boolean negative = false;
        char ch = charSequence.charAt(offset);
        switch (ch) {
            case '-':
                if (length == 1) {
                    throw new ParseException("Single '-' is not expected", 0);
                }
                negative = true;
                offset++;
                break;
            case 'N':
            case 'n':
                if (length != 3 ||
                        charSequence.charAt(offset + 1) != 'a' && charSequence.charAt(offset + 1) != 'A' ||
                        charSequence.charAt(offset + 2) != 'n' && charSequence.charAt(offset + 2) != 'N') {
                    throw new ParseException("Unexpected alphanumeric value", 0);
                }
                setRaw(NaN);
                return (T) this;
            default:
                // go on
        }

        long result = 0;
        int fractionalStart = length;
        while (offset < length) {
            ch = charSequence.charAt(offset++);
            if (ch == '.') {
                if (fractionalStart != length) {
                    throw new ParseException("Double '.' found", offset);
                }
                if (offset == length) {
                    throw new ParseException("Last '.' found", offset);
                }
                fractionalStart = offset; // dot position incremented
                while (length > fractionalStart && charSequence.charAt(length - 1) == '0') {
                    length--;
                }
            } else if (ch >= '0' && ch <= '9') {
                if (result > Long.MAX_VALUE / 10) {
                    throw new ParseException("Overflow", 0);
                }
                result *= 10;
                result += ch - '0';
                if (result < 0) {
                    throw new ParseException("Overflow", 0);
                }
            } else {
                throw new ParseException("Unexpected " + ch, offset - 1);
            }
        }

        result = doScale(result, getScale() - (length - fractionalStart));
        if (result == NaN) {
            throw new ParseException("Overflow while scaling up", 0);
        }
        setRaw(negative ? -result : result);
        return (T) this;
    }

    private static final int MAX_LONG_SIZE = Long.toString(Long.MAX_VALUE).length();
    private static int stringSize(long value) {
        long product = 10;
        for (int size = 1; size < MAX_LONG_SIZE; size++) {
            if (value < product)
                return size;
            product *= 10;
        }
        return 19;
    }

    /**
     * Adds 2 longs and multiply the result by non-negative power of 10
     */
    protected long plusAndScale(long a, long b, int nonNegScale) {
        return doScale(doPlus(a, b), nonNegScale);
    }

    /**
     * Multiply a value to a non-negative power of 10, then add it to another value, i.e.
     * a * 10^scale + b
     * Tries to eliminate multiplication overflow in case of different sign addends
     */
    protected long scaleAndPlus(long a, long b, int nonNegScale) {
        if (nonNegScale > 0) {
            long scaled = doScale(a, nonNegScale);
            if (scaled == NaN && a != NaN && b != NaN) {
                // trying to eliminate overflow (possible when a and raw has different signs and abs(raw) is big enough)

                // another bigger limit for unsigned multiplication (if we are out of this as well - give up)
                long unsignedLimit = SCALE_OVERFLOW_LIMITS[nonNegScale] * 2;
                if (b < 0 && a > 0 && a <= unsignedLimit) {
                    a = a * POW10[nonNegScale];
                    assert a < 0 : "Overflow to sign expected";
                    a += b; // subtracting abs(raw)
                    if (a >= 0) {
                        // no more overflow
                        return a;
                    }
                } else if (b > 0 && a >= -unsignedLimit && a < 0) {
                    a = -a * POW10[nonNegScale]; // negate a before multiplying
                    assert a < 0 : "Overflow to sign expected";
                    a -= b; // adding raw (in negated terms)
                    if (a >= 0) {
                        // no more overflow
                        return -a; // don't forget to negate "a" back
                    }
                }
            }
            a = scaled;
        }

        return doPlus(b, a);
    }

    protected long doPlus(AbstractDecimal<?> firstNumber, AbstractDecimal<?> secondNumber) {
        return doPlus(firstNumber.getRaw(), secondNumber.getRaw());
    }

    private long doPlus(long a, long b) {
        long result = a + b;
        return a == NaN || b == NaN || (result < 0) != (a < 0) && (result < 0) != (b < 0) ? NaN : result;
    }

    protected long doScale(AbstractDecimal<?> firstNumber, int scale) {
        return doScale(firstNumber.getRaw(), scale);
    }

    protected long doScale(long value, int scale) {
         return value >= -SCALE_OVERFLOW_LIMITS[scale] && value <= SCALE_OVERFLOW_LIMITS[scale] ?
                 value * LONG_POW10[scale] : NaN;
    }

    protected long doMultiply(AbstractDecimal<?> firstNumber, AbstractDecimal<?> secondNumber) {
        return doMultiply(firstNumber.getRaw(), secondNumber.getRaw());
    }

    protected long doMultiply(long a, long b) {
        if (a > Integer.MIN_VALUE && a <= Integer.MAX_VALUE &&
                b > Integer.MIN_VALUE && b <= Integer.MAX_VALUE) { // and not NaN
            // can multiply without overflow
            return a * b;
        } else {
            return mulScaleRound(a, b, 0, getDefaultRoundingMode());
        }
    }

    protected void checkCurrentScale(AbstractDecimal<?> firstNumber, AbstractDecimal<?> secondNumber) {
        if (getScale() != firstNumber.getScale() || getScale() != secondNumber.getScale()) {
            throw new IllegalArgumentException("Scales must be the same");
        }
    }

    protected void checkScale(AbstractDecimal<?> firstNumber, AbstractDecimal<?> secondNumber) {
        if (firstNumber.getScale() != secondNumber.getScale()) {
            throw new IllegalArgumentException("Scales must be the same");
        }
    }
}
