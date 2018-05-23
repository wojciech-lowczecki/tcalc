package pl.plh.tcalc.processor;

import static pl.plh.tcalc.GenericValidator.*;

public class Processor {
    private final InfixPostfixConverter ipConverter;
    private final PostfixCalculator pCalculator;
    private final ResultFormatter formatter;

    public Processor(InfixPostfixConverter converter, PostfixCalculator calculator) {
        checkNotNull(converter);
        checkNotNull(calculator);
        this.ipConverter = converter;
        this.pCalculator = calculator;
        this.formatter = new ResultFormatter();
    }

    public String process(String inputData) {
        checkNotBlank(inputData, "missing input data");
        try {
            return formatter.format(pCalculator.calculate(ipConverter.convert(inputData)), pCalculator.getLimit());
        } catch (ParseException | ArithmeticException e) {
            return e.getMessage();
        } catch (Exception e) {
            System.err.println("unexpected error");
            System.err.println();
            throw e;
        }
    }
}
