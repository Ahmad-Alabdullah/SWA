package zspace.zulieferer.service;

/**
 * Exception, falls die Versionsnummer nicht aktuell ist.
 *
 */
public class VersionOutdatedException extends RuntimeException {
    private final int version;

    VersionOutdatedException(final int version) {
        super("Die Versionsnummer " + version + " ist veraltet.");
        this.version = version;
    }
}
