package pl.plh.tcalc;

public class GenericValidator {
    public static <T> void checkNotNull(T t) {
        if (t == null) {
            throw new IllegalArgumentException("null " + t.getClass().getSimpleName());
        }
    }

    public static void checkNotBlank(String s, String message) {
        if (s == null || s.trim().isEmpty()) {
            if (message == null) {
                throw new IllegalArgumentException();
            }
            throw new IllegalArgumentException(message);
        }
    }

    public static void checkMin(int n, int min) {
        if (n < min) {
            throw new IllegalArgumentException(String.format("value %1$d is less than %2$d", n, min));
        }
    }
}
