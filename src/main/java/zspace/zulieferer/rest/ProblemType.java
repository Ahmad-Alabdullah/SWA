package zspace.zulieferer.rest;

/**
 * enum für ProblemType.
 */
@SuppressWarnings("unused")
public enum ProblemType {
    /**
     * Constraints als Fehlerursache.
     */
    CONSTRAINTS("constraints"),

    /**
     * Fehler, wenn z.B. Emailadresse bereits existiert.
     */
    UNPROCESSABLE("unprocessable"),

    /**
     * Fehler beim Header `If-Match`.
     */
    PRECONDITION("precondition"),

    /**
     * Fehler bei z.B. einer Patch-Operation.
     */
    BAD_REQUEST("badRequest"),

    /**
     * Fehler bei not found.
     */
    NOT_FOUND("notFound");

    private final String value;

    ProblemType(final String value) {
        this.value = value;
    }

    /**
     * methode getValue().
     *
     * @return value
     */
    String getValue() {
        return value;
    }
}
