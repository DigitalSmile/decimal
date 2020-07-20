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

/**
 * Reference implementation of {@link AbstractDecimal} for maximum supported precision (9 dp).
 * Values from -9223372036.854775807 to 9223372036.854775807 (inclusive), which should be good enough for small numbers.
 */
class Decimal extends AbstractDecimal<Decimal> {

    private int scale = 9;
    private RoundingMode defaultRoundingMode = RoundingMode.DOWN;

    Decimal() {}

    Decimal(int scale, RoundingMode roundingMode) {
        this.scale = scale;
        this.defaultRoundingMode = roundingMode;
    }

    Decimal(int scale) {
        this.scale = scale;
    }

    Decimal(RoundingMode roundingMode) {
        this.defaultRoundingMode = roundingMode;
    }

    @Override
    protected int getScale() {
        return scale; // must be constant
    }

    @Override
    public RoundingMode getDefaultRoundingMode() {
        return defaultRoundingMode;
    }


    /**
     * Add 2 numbers of the scale same to this
     * No rounding required.
     */
    public Decimal plus(Decimal firstNumber, Decimal secondNumber) {
        checkCurrentScale(firstNumber, secondNumber);
        setRaw(doPlus(firstNumber, secondNumber));
        return this;
    }

    /**
     * Add 2 numbers of the scale same to this
     * No rounding required.
     */
    public Decimal plus(long a, long b) {
        var firstNumber = new Decimal(getScale()).fromLong(a);
        var secondNumber = new Decimal(getScale()).fromLong(b);
        checkCurrentScale(firstNumber, secondNumber);
        setRaw(doPlus(firstNumber, secondNumber));
        return this;
    }

    /**
     * Add 2 numbers of the same scale and puts result to this
     * Rounding is required if the arguments' scale is greater than this scale.
     */
    public Decimal plus(Decimal firstNumber, Decimal secondNumber, RoundingMode roundingMode) {
        checkScale(firstNumber, secondNumber);

        int scale = getScale() - firstNumber.getScale();
        return plus(firstNumber.getRaw(), secondNumber.getRaw(), roundingMode, scale);
    }

    /**
     * Adds 2 longs and multiply the result by (possibly negative) power of 10
     */
    private Decimal plus(long firstNumber, long secondNumber, RoundingMode roundingMode, int scale) {
        if (scale >= 0 || firstNumber == NaN || secondNumber == NaN) {
            setRaw(plusAndScale(firstNumber, secondNumber, scale));
            return this;
        }

        if (firstNumber >= 0 && secondNumber >= 0) { // unsigned overflow is not possible, ok with signed one
            firstNumber = unsignedDownScale_64_31(firstNumber + secondNumber, -scale);
            secondNumber = getRaw();
        } else if (firstNumber < 0 && secondNumber < 0) { // same as above, but negate everything before and after
            firstNumber = -unsignedDownScale_64_31(-firstNumber - secondNumber, -scale);
            secondNumber = -getRaw();
        } else { // no overflow is possible
            firstNumber = downScale_63_31(firstNumber + secondNumber, -scale);
            secondNumber = getRaw();
        }
        setRaw(round(firstNumber, secondNumber, POW10[-scale], roundingMode));
        return this;
    }

    /**
     * Add a number to this.
     * Rounding is required if the argument scale is greater than this scale.
     */
    public Decimal add(Decimal number, RoundingMode roundingMode) {
        int scale = getScale() - number.getScale();
        return add(number.getRaw(), roundingMode, scale);
    }

    /**
     * Add a number of the same scale to this.
     * No rounding required.
     */
    public Decimal add(Decimal number) {
        return add(number, getDefaultRoundingMode());
    }

    public Decimal add(long number) {
        return plus(this, new Decimal(getScale()).fromLong(number));
    }

    /**
     * Adds a value multiplied by (possibly negative)  power of 10
     */
    private Decimal add(long number, RoundingMode roundingMode, int scale) {
        if (scale < 0 && !isNaN() && number != NaN) {
            long self = getRaw();
            long other = downScale_63_31(number, -scale);
            long remainder = getRaw();

            // have to inline plusWithOverflow here to avoid extra "if NaN then return immediately"
            long result = self + other;
            if (self == NaN || other == NaN || (result < 0) != (self < 0) && (result < 0) != (other < 0)) {
                setRaw(NaN);
                return this;
            }

            int denominator = POW10[-scale];
            if (result < 0 && remainder > 0) {
                remainder -= denominator;
                ++result;
            } else if (result > 0 && remainder < 0) {
                remainder += denominator;
                --result;
            }
            setRaw(round(result, remainder, denominator, roundingMode));
            return this;
        }
        setRaw(scaleAndPlus(number, getRaw(), scale));
        return this;
    }

    /**
     * Subtract 2 numbers of the scale same to this
     * No rounding required.
     */
    public Decimal subtract(Decimal firstNumber, Decimal secondNumber) {
        //checkScale(firstNumber, secondNumber);
        secondNumber.doNegate();
        setRaw(doPlus(firstNumber, secondNumber));
        return this;
    }

    /**
     * Subtract 2 numbers of the same scale and puts result to this
     * Rounding is required if the arguments' scale is greater than this scale.
     */
    public Decimal subtract(Decimal firstNumber, Decimal secondNumber, RoundingMode roundingMode) {
        //checkScale(firstNumber, secondNumber);
        int scale = secondNumber.getScale() - firstNumber.getScale();
        return plus(firstNumber.getRaw(), -secondNumber.getRaw(), roundingMode, scale);
    }

    /**
     * Subtract a number from this.
     * Rounding is required if the argument scale is greater than this scale.
     */
    public Decimal subtract(Decimal number, RoundingMode roundingMode) {
        int scale = getScale() - number.getScale();
        return add(-number.getRaw(), roundingMode, scale);
    }

    /**
     * Subtract a number of the same scale from this.
     * No rounding required.
     */
    public Decimal subtract(Decimal number) {
        return subtract(this, number, getDefaultRoundingMode());
    }

    public Decimal subtract(long number) {
        return subtract(this, new Decimal(getScale()).fromLong(number));
    }

    /**
     * Multiply 2 numbers of the same scale and put the result to this.
     * Rounding is required if the arguments scale combined is greater than this scale.
     */
    public Decimal multiply(Decimal firstNumber, Decimal secondNumber, RoundingMode roundingMode) {
        checkCurrentScale(firstNumber, secondNumber);
        int scale = firstNumber.getScale() + secondNumber.getScale() - getScale();
        if (scale >= 0) {
            setRaw(mulScaleRound(firstNumber.getRaw(), secondNumber.getRaw(), scale, roundingMode));
        } else {
            setRaw(doScale(doMultiply(firstNumber, secondNumber), -scale));
        }
        return this;
    }

    /**
     * Multiply this by the argument.
     * Rounding is required if argument scale is not zero.
     */
    public Decimal multiply(Decimal number, RoundingMode roundingMode) {
        setRaw(mulScaleRound(getRaw(), number.getRaw(), number.getScale(), roundingMode));
        return this;
    }

    public Decimal multiply(Decimal number) {
        return multiply(number, getDefaultRoundingMode());
    }

    public Decimal multiply(long number) {
        return multiply(new Decimal(getScale()).fromLong(number), getDefaultRoundingMode());
    }

    public Decimal divide(Decimal divisor) {
        return divide(divisor, getDefaultRoundingMode());
    }

    /**
     * Divide this by the argument and put result into this.
     * Rounding is always required.
     * Return {@link #NaN} if a is zero.
     */
    public Decimal divide(Decimal divisor, RoundingMode roundingMode) {
        setRaw(scaleDivRound(getRaw(), divisor.getScale(), divisor.getRaw(), roundingMode));
        return this;
    }

    public Decimal divide(long number) {
        return divide(new Decimal(getScale()).fromLong(number), getDefaultRoundingMode());
    }

    public Decimal divide(long number, RoundingMode roundingMode) {
        return divide(new Decimal(getScale()).fromLong(number), roundingMode);
    }

    public Decimal negate() {
        doNegate();
        return this;
    }

    public Decimal set(long number) {
        setRaw(doScale(number, getScale()));
        return this;
    }

    public Decimal set(Decimal number, RoundingMode roundingMode) {
        int scale = getScale() - number.getScale();
        if (scale == 0) {
            setRaw(number.getRaw());
        } else if (scale < 0 && !number.isNaN()) {
            long result = downScale_63_31(number.getRaw(), -scale);
            long remainder = getRaw();
            setRaw(round(result, remainder, POW10[-scale], roundingMode));
        } else {
            setRaw(doScale(number.getRaw(), scale));
        }
        return this;
    }

    public Decimal set(Decimal number) {
        return set(number, getDefaultRoundingMode());
    }
}
