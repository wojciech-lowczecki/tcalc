package pl.plh.tcalc.processor;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.function.BinaryOperator;

import static java.math.RoundingMode.HALF_UP;
import static pl.plh.tcalc.GenericValidator.*;

public class BasicPostfixCalculator implements PostfixCalculator {
    private final MathContext mathContext;
    private final Map<String, BinaryOperator<BigDecimal>> operators = Collections.unmodifiableMap(
        new HashMap<String, BinaryOperator<BigDecimal>>() {
            {
                put("+", (x1, x2) -> x1.add(x2));
                put("-", (x1, x2) -> x1.subtract(x2));
                put("*", (x1, x2) -> x1.multiply(x2));
                put("/", (x1, x2) -> divide(x1, x2));
            }
        });

    // Limit value is used as:
    // - precision of division,
    // - maximal precision of final result
    // Values over limit are rounded with the round half up rule.
    // Default limit is 70
    public BasicPostfixCalculator(int limit) {
        checkMin(limit, 1);
        this.mathContext = new MathContext(limit, HALF_UP);
    }

    public BasicPostfixCalculator() {
        this(70);
    }

    // Decimal point of number tokens has to be a dot '.'
    // as in BigDecimal(String) constructor
    @Override
    public BigDecimal calculate(List<String> postfix) {
        checkNotNull(postfix);
        Deque<BigDecimal> stack = new ArrayDeque<>();
        Iterator<String> it = postfix.iterator();
        while (it.hasNext()) {
            String token = it.next();
            if (operators.containsKey(token)) {
                BigDecimal arg2 = stack.pop();
                BigDecimal arg1 = stack.pop();
                stack.push(operators.get(token).apply(arg1, arg2));
            } else {
                stack.push(new BigDecimal(token));
            }
        }
        if (stack.size() != 1) {
            throw new IllegalStateException("stack.size() != 1");
        }
        return stack.pop().round(mathContext);
    }

    @Override
    public int getLimit() {
        return mathContext.getPrecision();
    }

    private BigDecimal divide(BigDecimal arg1, BigDecimal arg2)  {
        return arg1.divide(arg2, mathContext);
    }
}
