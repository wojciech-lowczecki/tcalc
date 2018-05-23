package pl.plh.tcalc.processor;

import java.util.List;

public interface InfixPostfixConverter {
    // Throws pl.plh.tcalc.processor.ParseException if infix is malformed or division by zero occurs
    // or decimal point is not suitable to localization
    // Returns a List of String postfix tokens where decimals have standard java form with '.' as a decimal point
    List<String> convert(String infix);
}
