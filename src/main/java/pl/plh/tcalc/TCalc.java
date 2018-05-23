package pl.plh.tcalc;

import pl.plh.tcalc.processor.BasicPostfixCalculator;
import pl.plh.tcalc.processor.Processor;
import pl.plh.tcalc.processor.ShuntingYard;

import java.util.Scanner;
import java.util.regex.Pattern;

public class TCalc {
    private static final String INTRO =
        "\nTCalc - simple terminal processor\n" +
        "Program calculates values of arithmetic expressions given in the infix form,\n" +
        "for example: 2+3*4, (2+3)*4, ((1+2)/(3*4))-456, etc. It executes 4 basic\n" +
        "operations: +, -, * and /. Results over 70 digits are rounded.\n";
    private static final Pattern ESCAPE_PATTERN = Pattern.compile("(?i:q|quit)?");

    private final Processor calc = new Processor(new ShuntingYard(), new BasicPostfixCalculator());
    private final Scanner scan = new Scanner(System.in);

    public void run() {
        System.out.println(INTRO);
        while (true) {
            System.out.println("Enter an expression or exit with q or quit:");
            System.out.print("> ");
            String line = scan.nextLine().trim();
            if (line.length() == 0) {
                continue;
            }
            if (ESCAPE_PATTERN.matcher(line).matches()) {
                System.out.println("Goodbye!");
                break;
            }
            System.out.println("= " + calc.process(line));
        }
    }
}
