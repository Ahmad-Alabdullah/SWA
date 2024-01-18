package zspace.zulieferer.rest;

import zspace.zulieferer.entity.Filiale;
import zspace.zulieferer.entity.Zulieferer;

import java.util.UUID;

/**
 * record für Zulieferer.
 *
 * @param name name des Zulieferers
 * @param filiale filialenname
 * @param email email
 * @param baeckerId id des Baeckers
 */
@SuppressWarnings("RecordComponentNumber")
record ZuliefererDTO(
    String name,

    String email,

    FilialeDTO filiale,

    UUID baeckerId
) {
    /**
     * Konvertierung in ein Objekt des Anwendungskerns.
     *
     * @return Zuliefererobjekt für den Anwendungskern
     */

    Zulieferer toZulieferer() {
        final var filialeEntity = filiale() == null
            ? null
            : Filiale
            .builder()
            .name(filiale().name())
            .standort(filiale().standort())
            .build();

        final var zulieferer = Zulieferer
            .builder()
            .id(null)
            .name(name)
            .email(email)
            .baeckerId(baeckerId)
            .filiale(filialeEntity)
            .build();

        if (filialeEntity != null) {
            filialeEntity.setZulieferer(zulieferer);
        }

        return zulieferer;
    }
}
