package pl.plh.tcalc.processor;

import java.math.BigDecimal;
import java.util.List;

public interface PostfixCalculator {
    // Decimal point of number tokens has to be a dot '.'
    // as in BigDecimal(String) constructor
    BigDecimal calculate(List<String> postfix) throws NumberFormatException;

    // Returns a limit used to determine
    // - precision of division,
    // - maximal precision of final result
    int getLimit();
}
