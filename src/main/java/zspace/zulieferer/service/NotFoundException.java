package zspace.zulieferer.service;

import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * RuntimeException, falls kein zulieferer gefunden wurde.
 */
@Getter
@SuppressWarnings("unchecked")
public final class NotFoundException extends RuntimeException {
    /**
     * Nicht-vorhandene ID.
     */
    private final UUID id;

    /**
     * Suchkriterien, zu denen nichts gefunden wurde.
     */
    private final Map<String, List<String>> suchkriterien;

    NotFoundException(final UUID id) {
        super("Kein zulieferer mit der ID " + id + " gefunden.");
        this.id = id;
        suchkriterien = null;
    }

    NotFoundException(final Map<String, ? extends List<String>> suchkriterien) {
        super("Keine zulieferern gefunden.");
        id = null;
        this.suchkriterien = (Map<String, List<String>>) suchkriterien;
    }

    @SuppressWarnings("unused")
    NotFoundException() {
        super("Keine zulieferern gefunden.");
        id = null;
        suchkriterien = null;
    }
}
