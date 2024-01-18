package zspace.zulieferer.rest;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import zspace.zulieferer.entity.Filiale;
import zspace.zulieferer.entity.Zulieferer;

import java.util.UUID;

/**
 * Model-Klasse für Spring HATEOAS. @lombok.Data fasst die Annotationsn @ToString, @EqualsAndHashCode, @Getter, @Setter
 * und @RequiredArgsConstructor zusammen.
 *
 */
@JsonPropertyOrder({
    "name", "baeckerId", "baeckerNachname", "baeckerVorname", "email", "filiale"
})
@Relation(collectionRelation = "Zulieferern", itemRelation = "Zulieferer")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@Setter
@ToString(callSuper = true)
class ZuliefererModel extends RepresentationModel<ZuliefererModel> {
    private final String name;

    private final UUID baeckerId;

    private final String baeckerNachname;

    private final String baeckerVorname;

    @EqualsAndHashCode.Include
    private final String email;

    private final Filiale filiale;

    ZuliefererModel(final Zulieferer zulieferer) {
        name = zulieferer.getName();
        baeckerId = zulieferer.getBaeckerId();
        baeckerNachname = zulieferer.getBaeckerNachname();
        baeckerVorname = zulieferer.getBaeckerVorname();
        email = zulieferer.getEmail();
        filiale = zulieferer.getFiliale();
    }
}
