package zspace.zulieferer.graphql;

import zspace.zulieferer.entity.Filiale;
import zspace.zulieferer.entity.Zulieferer;

import java.util.UUID;

/**
 * Eine Value-Klasse für Eingabedaten passend zu ZuliefererInput aus dem GraphQL-Schema.
 *
 * @param name Name
 * @param email Emailadresse
 * @param filiale Filiale
 */
@SuppressWarnings("RecordComponentNumber")
record ZuliefererInput(
    String name,
    String email,
    UUID baeckerId,
    FilialeInput filiale
) {
    /**
     * Konvertierung in ein Objekt der Entity-Klasse Zulieferer.
     *
     * @return Das konvertierte Zulieferer-Objekt
     */
    Zulieferer toZulieferer() {
        final var filialeEntity = Filiale.builder().name(filiale.name()).standort(filiale.standort()).build();

        return Zulieferer
            .builder()
            .id(null)
            .name(name)
            .email(email)
            .baeckerId(baeckerId)
            .filiale(filialeEntity)
            .build();
    }
}
