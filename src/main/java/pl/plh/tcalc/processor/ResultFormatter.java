package pl.plh.tcalc.processor;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import static pl.plh.tcalc.GenericValidator.checkMin;
import static pl.plh.tcalc.GenericValidator.checkNotNull;


public class ResultFormatter {
    // decimal separator
    private static final char DS = new DecimalFormat().getDecimalFormatSymbols().getDecimalSeparator();

    // Format a BigDecimal according to default localization
    // limit - maximum digits number of resulted decimal
    // throws IllegalArgumentException, if reduction of digits
    // would cause the loss of the value
    public String format(BigDecimal number, int limit) {
        checkNotNull(number);
        checkMin(limit, 1);
        BigDecimal bd = number.stripTrailingZeros();
        if(number.precision() > limit) {
            throw new IllegalArgumentException("limit causes the loss of a value");
        }
        String plainStr = bd.toPlainString();
        int digits =  plainStr.length();
        if (bd.scale() > 0) { digits--; } // if the number is not integer
        String result = plainStr;
        if (digits > limit) { // very long 0.00000000000...
            result = bd.toEngineeringString();
        }
        return result.replace('.', DS);
    }
}
