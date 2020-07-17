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

import java.text.ParseException;

/**
 * Reference implementation of {@link AbstractDecimal} for maximum supported precision (9 dp).
 * Values from -9223372036.854775807 to 9223372036.854775807 (inclusive), which should be good enough for small numbers.
 */
public class Decimal extends AbstractDecimal<Decimal> {

    private int scale = 9;

    private Decimal() {}

    private Decimal(int scale) {
        this.scale = scale;
    }

    @Override
    protected int getScale() {
        return scale; // must be constant
    }

    public static Decimal of(double value) {
        return new Decimal().fromDoubleRD(value);
    }

    public static Decimal of(String value) throws ParseException {
        return new Decimal().parse(value);
    }

    public static Decimal of(long value) {
        return new Decimal().fromLong(value);
    }

    public static Decimal of(double value, int scale) {
        return new Decimal(scale).fromDoubleRD(value);
    }

    public static Decimal of(String value, int scale) throws ParseException {
        return new Decimal(scale).parse(value);
    }

    public static Decimal of(long value, int scale) {
        return new Decimal(scale).fromLong(value);
    }



}
