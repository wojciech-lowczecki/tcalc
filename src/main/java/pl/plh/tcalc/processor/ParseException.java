package pl.plh.tcalc.processor;

public class ParseException extends RuntimeException {
    public ParseException() {
        super("incorrect expression");
    }

    public ParseException(String message) {
        super(message);
    }
}