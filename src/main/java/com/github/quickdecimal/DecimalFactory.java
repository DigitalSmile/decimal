package com.github.quickdecimal;

import java.math.RoundingMode;
import java.text.ParseException;

public class DecimalFactory {

    private static RoundingMode defaultRoundingMode = RoundingMode.DOWN;

    private DecimalFactory(){}

    public static Decimal of(double value) {
        return new Decimal().fromDouble(value, defaultRoundingMode);
    }

    public static Decimal of(String value) throws ParseException {
        return new Decimal().parse(value);
    }

    public static Decimal of(long value) {
        return new Decimal().fromLong(value);
    }

    public static Decimal of(double value, int scale) {
        return new Decimal(scale).fromDouble(value, defaultRoundingMode);
    }

    public static Decimal of(String value, int scale) throws ParseException {
        return new Decimal(scale).parse(value);
    }

    public static Decimal of(long value, int scale) {
        return new Decimal(scale).fromLong(value);
    }

    public static void setDefaultRoundingMode(RoundingMode roundingMode) {
        defaultRoundingMode = roundingMode;
    }
}
