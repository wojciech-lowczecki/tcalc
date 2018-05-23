package pl.plh.tcalc.processor;

import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

import static pl.plh.tcalc.GenericValidator.*;

public class ShuntingYard implements InfixPostfixConverter {
    private static final char DS = new DecimalFormat().getDecimalFormatSymbols().getDecimalSeparator();
    // Scanner goes char by char skipping whitespaces
    private static final Pattern DELIMITER_PTRN = Pattern.compile("\\s*");
    private static final Pattern DIGIT_PTRN = Pattern.compile("\\d");
    private static final Pattern POSITIVE_DECIMAL_PTRN = Pattern.compile(String.format("\\d+(?:\\%s\\d+)?", DS));
    private static final Pattern OPERATOR_PTRN = Pattern.compile("[\\+\\-\\*\\/]");
    private static final Pattern LEFT_BRACKET_PTRN = Pattern.compile("\\(");
    private static final Pattern RIGHT_BRACKET_PTRN = Pattern.compile("\\)");
    private static final Pattern DIGIT_OR_LEFT_BRACKET_PTRN = Pattern.compile("[\\(\\d]");
    private static final Pattern OPERATOR_OR_RIGHT_BRACKET_PTRN = Pattern.compile("[\\)\\+\\-\\*\\/]");

    private static final class Precedence {
        static final Map<String, Integer> precedences = createPrecedences();

        static Map<String, Integer> createPrecedences() {
            Map<String, Integer> precedences =  new HashMap<String, Integer>();
            precedences.put("+", 0);
            precedences.put("-", 0);
            precedences.put("*", 1);
            precedences.put("/", 1);
            return Collections.unmodifiableMap(precedences);
        }

        private static boolean isHigherOrEqual(String op, String sub) {
            return precedences.containsKey(op) &&
                    (!precedences.containsKey(sub) ||
                            precedences.get(op) >= precedences.get(sub));
        }
    }

    private List<String> output;
    private Deque<String> stack;
    private Scanner scan;

    private void init(String infix) {
        output = new ArrayList<>();
        stack = new ArrayDeque<>();
        // scan uses only StringReader as a source, so scan.close() is unnecessary
        scan = new Scanner("(" + infix + ")").useDelimiter(DELIMITER_PTRN);
    }

    // Based on The Shunting Yard Algorithm
    // Throws pl.plh.tcalc.processor.ParseException if infix is malformed
    // or decimal point is not suitable to localization
    // Returns a List of String postfix tokens where decimals have standard java form with '.' as a decimal point
    @Override
    public List<String> convert(String infix) {
        checkNotBlank(infix, "empty infix");
        init(infix);
        while (isSomethingToShunt()) {
            if (shuntDecimal() || shuntBinaryOperator() || shuntLeftBracketWithUnaryOperatorIfPresent()
                || shuntRightBracket()) {
                continue;
            }
            // undesirable "carriage"
            throw new ParseException();
        }
        return getOutput();
    }

    private boolean isSomethingToShunt() {
        return scan.hasNext();
    }

    private boolean shuntDecimal() {
        if (scan.hasNext(DIGIT_PTRN)) {
            String decimal = scan.findInLine(POSITIVE_DECIMAL_PTRN);
            if (scan.hasNext(DIGIT_OR_LEFT_BRACKET_PTRN)) {
                throw new ParseException();
            }
            output.add(decimal.replace(DS, '.'));
            return true;
        }
        return false;
    }

    private boolean shuntLeftBracketWithUnaryOperatorIfPresent() {
        if(shuntLeftBracket()) {
            shuntUnaryOperator();
            return true;
        }
        return false;
    }

    // for use only inside shuntLeftBracketWithUnaryOperatorIfPresent()
    private boolean shuntUnaryOperator() {
        String operator = scanNextOperator();
        if(operator == null) {
            return false;
        }
        switch(operator) {
            case "+":
                break; // ignore unary "+"
            case "-":
                output.add("0"); // unary "-x" is the same as binary "0-x"
                stack.push("-");
                break;
            default: // unknown unary operator
                throw new ParseException();
        }
        return true;
    }

    // may be called unconditionally because unary operators are shunted with left brackets
    private boolean shuntBinaryOperator() {
        String operator = scanNextOperator();
        if(operator == null) {
            return false;
        }
        while (!stack.isEmpty() && Precedence.isHigherOrEqual(stack.peek(), operator)) {
            output.add(stack.pop());
        }
        stack.push(operator);
        return true;
    }

    private String scanNextOperator() {
        if(scan.hasNext(OPERATOR_PTRN)) {
            String op = scan.next(OPERATOR_PTRN);
            if (scan.hasNext(OPERATOR_OR_RIGHT_BRACKET_PTRN)) {
                throw new ParseException();
            }
            return op;
        }
        return null;
    }

    // for use only inside shuntLeftBracketWithUnaryOperatorIfPresent()
    private boolean shuntLeftBracket() {
        if (scan.hasNext(LEFT_BRACKET_PTRN)) {
            String lbracket = scan.next(LEFT_BRACKET_PTRN);
            if (scan.hasNext(RIGHT_BRACKET_PTRN)) {
                throw new ParseException();
            }
            stack.push(lbracket);
            return true;
        }
        return false;
    }

    private boolean shuntRightBracket() {
        if (scan.hasNext(RIGHT_BRACKET_PTRN)) {
            scan.next(RIGHT_BRACKET_PTRN);
            if (scan.hasNext(DIGIT_OR_LEFT_BRACKET_PTRN)) {
                throw new ParseException();
            }
            while (true) {
                if (stack.isEmpty()) {
                    throw new ParseException();
                }
                String popped = stack.pop();
                if (popped.equals("(")) {
                    break;
                }
                output.add(popped);
            }
            return true;
        }
        return false;
    }

    private List<String> getOutput() {
        // The first pushed token was '(' and the last one was ')' (see init(String) method),
        // so the stack should be empty now.
        if (!stack.isEmpty()) {
            throw new ParseException();
        }
        return output;
    }
}

