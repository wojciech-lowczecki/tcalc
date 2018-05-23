package pl.plh.tcalc.processor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.text.DecimalFormat;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class ProcessorTest {
    private static final Locale DEFAULT_FORMAT_LOCALE = Locale.getDefault(Locale.Category.FORMAT);

    @Before
    public void setFormatLocaleWithCommaAsDefaultDecimalSeparator() {
        Locale.setDefault(Locale.Category.FORMAT, new Locale("pl", "PL"));
        assertEquals(',', new DecimalFormat().getDecimalFormatSymbols().getDecimalSeparator());
    }

    @After
    public void restoreDefaultSystemFormatLocale() {
        Locale.setDefault(DEFAULT_FORMAT_LOCALE);
    }

    @Test
    public void testProcessSimple() {
        //GIVEN
        Processor proc = new Processor(new ShuntingYard(), new BasicPostfixCalculator());

        //WHEN & THEN
        assertEquals("0", proc.process("0"));
        assertEquals("0", proc.process("-0"));
        assertEquals("1", proc.process("1"));
        assertEquals("-1", proc.process("-1"));
        assertEquals("6", proc.process("2+4"));
        assertEquals("-2", proc.process("2-4"));
        assertEquals("8", proc.process("2*4"));
        assertEquals("0,5", proc.process("2/4"));
        assertEquals("1,23", proc.process("1,23"));
        assertEquals("1,23", proc.process("+1,23"));
        assertEquals( "-1,23", proc.process("-1,23"));
        assertEquals("46,23", proc.process("1,23+45"));
        assertEquals("43,77", proc.process("-1,23+45"));
        assertEquals("-43,77", proc.process("1,23-45"));
        assertEquals("-46,23", proc.process("-1,23-45"));
        assertEquals("55,35", proc.process("1,23*45"));
        assertEquals("-55,35", proc.process("-1,23*45"));
        assertEquals("0,0492", proc.process("1,23/25"));
        assertEquals("-0,0492", proc.process("-1,23/25"));
    }

    @Test
    public void testProcessSimplePrecedence() {
        //GIVEN
        Processor proc = new Processor(new ShuntingYard(), new BasicPostfixCalculator());

        //WHEN & THEN
        assertEquals("-2", proc.process("1+2-5"));
        assertEquals("4", proc.process("1-2+5"));
        assertEquals("2,5", proc.process("1/2*5"));
        assertEquals("0,4", proc.process("1*2/5"));
        assertEquals("11", proc.process("1+2*5"));
        assertEquals("7", proc.process("1*2+5"));
        assertEquals("1,4", proc.process("1+2/5"));
        assertEquals("5,5", proc.process("1/2+5"));
        assertEquals("-9", proc.process("1-2*5"));
        assertEquals("-3", proc.process("1*2-5"));
        assertEquals("0,6", proc.process("1-2/5"));
        assertEquals("-4,5", proc.process("1/2-5"));
    }

    @Test
    public void testProcessSimpleBrackets() {
        //GIVEN
        Processor proc = new Processor(new ShuntingYard(), new BasicPostfixCalculator());

        //WHEN & THEN
        assertEquals("1,23", proc.process("(1,23)"));
        assertEquals("-1,23", proc.process("(-1,23)"));
        assertEquals("1,23", proc.process("-(-(-(-(1,23))))"));
        assertEquals("-1,23", proc.process("-(-(-(1,23)))"));
        assertEquals("1,23", proc.process("(((1,23)))"));
        assertEquals("-1,23", proc.process("(((-1,23)))"));
        assertEquals("4,6", proc.process("1,2-(-3,4)"));
        assertEquals("2,2", proc.process("-1,2-(-3,4)"));
        assertEquals("9", proc.process("(1+2)*3"));
        assertEquals("-1", proc.process("(-1-2)/3"));
        assertEquals("-1", proc.process("(-1+2)/(3-4)"));
        assertEquals("7", proc.process("(1-2)*(-3-4)"));
    }

    @Test
    public void testProcessComplexData() {
        //GIVEN
        Processor proc = new Processor(new ShuntingYard(), new BasicPostfixCalculator());

        //WHEN & THEN
        assertEquals("-1", proc.process("1+2-3+4-5"));
        assertEquals("0,2", proc.process("1*2/5*4/8"));
        assertEquals("-82,45", proc.process("1-2*3+4/5+6/8-9*10+11"));
        assertEquals("-7,4", proc.process("1-2+3*4/5+6-7+8*9/10-15"));
        assertEquals("-1033", proc.process("((1+2)+(3-4)*(5+6))+((1+2)/(8-4)-(5+6))*100"));
        assertEquals("0,94", proc.process("((1+2)+(3-4)*(5+6))*((1+2)/(4-8)-(5+6))/100"));
    }

    @Test
    public void testProcessSpaces() {
        //GIVEN
        Processor proc = new Processor(new ShuntingYard(), new BasicPostfixCalculator());
        String spaced = " \t 1 \t + \t 8 \t * \t 3 \t / \t 4 \t ";
        String expected = "7";

        //WHEN & THEN
        assertEquals(expected, proc.process(spaced));
    }

    @Test
    public void testProcessMalformed() {
        //GIVEN
        Processor proc = new Processor(new ShuntingYard(), new BasicPostfixCalculator());
        String message = "incorrect expression";
        String[] inData = {"1++2", "1+-2", "1+*2", "1+/2", "1-+2", "1--2", "1-*2", "1-/2", "1*+2", "1*-2", "1**2",
                "1*/2", "1/+2", "1/-2", "1/*2", "1//2", "1(2+3)", "(1+2)3", "1 2", "1,2 3,4",
                "1,2,3", "a+2", "A", "0x1", "1+2%", "1%", "3$", "1^2", "1+(*2+3)", "1-(/2-3)",
                "1+(2+4+)", "1+(2+4-)", "1+(2+4*)", "1+(2+4/)", "(1+2)(3+4)", "()+1", "1+(2+3", "1+2)+3",
                "((1+2)-(3-4))*5)+6", "(((1+2)-(3-4))*5+6"};

        //WHEN & THEN
        for (int i = 0; i < inData.length; i++) {
            assertEquals(inData[i], message, proc.process(inData[i]));
        }
    }

    @Test
    public void testProcessResultOverLimit() {
        //GIVEN
        final int limit = 16;
        Processor proc = new Processor(new ShuntingYard(), new BasicPostfixCalculator(limit));

        //WHEN & THEN
        assertEquals("44,44444444444444E+15", proc.process("44444444444444444 * 1"));
        assertEquals("5555555555555555", proc.process("5555555555555555 * 1"));
        assertEquals("55,55555555555556E+15", proc.process("55555555555555555 * 1"));
        assertEquals("6666666666666666", proc.process("6666666666666666 * 1"));
        assertEquals("66,66666666666667E+15", proc.process("66666666666666666 * 1"));
        assertEquals("44444,44444444444", proc.process("44444,444444444444 * 1"));
        assertEquals("0,4444444444444444", proc.process("0,44444444444444444 * 1"));
        assertEquals("55555,55555555555", proc.process("55555,55555555555 * 1"));
        assertEquals("0,5555555555555555", proc.process("0,5555555555555555 * 1"));
        assertEquals("55555,55555555556", proc.process("55555,555555555555 * 1"));
        assertEquals("0,5555555555555556", proc.process("0,55555555555555555 * 1"));
        assertEquals("66666,66666666666", proc.process("66666,66666666666 * 1"));
        assertEquals("0,6666666666666666", proc.process("0,6666666666666666 * 1"));
        assertEquals("66666,66666666667", proc.process("66666,666666666666 * 1"));
        assertEquals("0,6666666666666667", proc.process("0,66666666666666666 * 1"));
        assertEquals("-44,44444444444444E+15", proc.process("-44444444444444444 * 1"));
        assertEquals("-5555555555555555", proc.process("-5555555555555555 * 1"));
        assertEquals("-55,55555555555556E+15", proc.process("-55555555555555555 * 1"));
        assertEquals("-6666666666666666", proc.process("-6666666666666666 * 1"));
        assertEquals("-66,66666666666667E+15", proc.process("-66666666666666666 * 1"));
        assertEquals("-44444,44444444444", proc.process("-44444,444444444444 * 1"));
        assertEquals("-0,4444444444444444", proc.process("-0,44444444444444444 * 1"));
        assertEquals("-55555,55555555555", proc.process("-55555,55555555555 * 1"));
        assertEquals("-0,5555555555555555", proc.process("-0,5555555555555555 * 1"));
        assertEquals("-55555,55555555556", proc.process("-55555,555555555555 * 1"));
        assertEquals("-0,5555555555555556", proc.process("-0,55555555555555555 * 1"));
        assertEquals("-66666,66666666666", proc.process("-66666,66666666666 * 1"));
        assertEquals("-0,6666666666666666", proc.process("-0,6666666666666666 * 1"));
        assertEquals("-66666,66666666667", proc.process("-66666,666666666666 * 1"));
        assertEquals("-0,6666666666666667", proc.process("-0,66666666666666666 * 1"));
    }

    @Test
    public void testProcessSmallDecimalResultOverLimit() {
        //GIVEN
        final int limit = 16;
        Processor proc = new Processor(new ShuntingYard(), new BasicPostfixCalculator(limit));
        String inDataDigitsEqualsMaxPrecision = "0,000000000000001";
        String expResultOfDataDigitsEqualsMaxPrecision = "0,000000000000001";
        String inDataDigitsOverMaxPrecision = "0,000000000000001234";
        String expResultOfDataDigitsOverMaxPrecision = "1,234E-15";

        // WHEN & THEN
        assertEquals(expResultOfDataDigitsEqualsMaxPrecision, proc.process(inDataDigitsEqualsMaxPrecision));
        assertEquals(expResultOfDataDigitsOverMaxPrecision, proc.process(inDataDigitsOverMaxPrecision));
    }

    @Test
    public void testProcessDivisionByZero() {
        //GIVEN
        Processor proc = new Processor(new ShuntingYard(), new BasicPostfixCalculator());
        String divisionZeroByZero = "0/0";
        String divisionOneByZero = "1/0";
        String divisionByZeroEmbeded = "1+2/(6-2*3)-7";

        //WHEN & THEN
        assertEquals("Division undefined", proc.process(divisionZeroByZero));
        assertEquals("Division by zero", proc.process(divisionOneByZero));
        assertEquals("Division by zero", proc.process(divisionByZeroEmbeded));
    }
}
