package zspace.zulieferer.graphql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Eine Value-Klasse für Eingabedaten passend zu Suchkriterien aus dem GraphQL-Schema.
 *
 * @param email Emailadresse
 */
record Suchkriterien(
    String email
) {
    /**
     * Konvertierung in eine Map.
     *
     * @return Das konvertierte Map-Objekt
     */
    Map<String, List<String>> toMap() {
        final Map<String, List<String>> map = new HashMap<>(2, 1);
        if (email != null) {
            map.put("email", List.of(email));
        }
        return map;
    }
}
