package zspace.zulieferer.service;

import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import org.springframework.graphql.client.FieldAccessException;
import org.springframework.graphql.client.GraphQlTransportException;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import zspace.zulieferer.entity.Zulieferer;
import zspace.zulieferer.repository.Baecker;
import zspace.zulieferer.repository.BaeckerRestRepository;
import zspace.zulieferer.repository.PredicateBuilder;
import zspace.zulieferer.repository.ZuliefererRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_MODIFIED;

/**
 * Eine Klasse Readservice.
 * <img src="C:\Users\Student\IdeaProjects\Zulieferer\extras\doc\ZuliefererReadService.svg"
 * alt="Klassendiagramm">
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ZuliefererReadService {

    private final ZuliefererRepository repo;
    private final PredicateBuilder predicateBuilder;
    private final HttpGraphQlClient graphQlClient;
    private final BaeckerRestRepository baeckerRestRepository;

    /**
     * methode findByid.
     *
     * @param id identifikator
     * @return zulieferer mit der gesuchten id
     */
    public @NonNull Zulieferer findById(final UUID id) {
        log.debug("findById: id={}", id);
        final var zuliefererOpt = repo.findById(id);

        final var zulieferer = zuliefererOpt.orElseThrow(() -> new NotFoundException(id));
        log.debug("findById: {}", zulieferer);
        return zulieferer;
    }

    /**
     * methode find.
     *
     * @param suchkriterien bestimmte suchkriterien
     * @return rueckgabeparameter von repo.fnd()
     */
    public @NotNull Collection<Zulieferer> find(final Map<String, ? extends List<String>> suchkriterien) {
        log.debug("find: suchkriterien={}", suchkriterien);

        if (suchkriterien.isEmpty()) {
            return repo.findAll();
        }

        if (suchkriterien.size() == 1) {
            final var emails = suchkriterien.get("email");
            if (emails != null && emails.size() == 1) {
                final var zulieferer = repo.findByEmail(emails.get(0));
                if (zulieferer.isEmpty()) {
                    throw new NotFoundException(suchkriterien);
                }
                final var zulieferern = List.of(zulieferer.get());
                log.debug("find (email): {}", zulieferern);
                return zulieferern;
            }
        }
        final var predicate = predicateBuilder
            .build(suchkriterien)
            .orElseThrow(() -> new NotFoundException(suchkriterien));
        final var zulieferern = repo.findAll(predicate);
        if (zulieferern.isEmpty()) {
            throw new NotFoundException(suchkriterien);
        }
        log.debug("find: {}", zulieferern);
        return zulieferern;
    }

    /**
     * Zulieferer zur Baecker-ID suchen.
     *
     * @param baeckerId Die Id des gegebenen baeckern.
     * @return Die gefundenen Bestellungen.
     * @throws NotFoundException Falls keine Bestellungen gefunden wurden.
     */
    public Collection<Zulieferer> findByBaeckerId(final UUID baeckerId) {
        log.debug("findBybaeckerId: baeckerId={}", baeckerId);

        final var zulieferers = repo.findByBaeckerId(baeckerId);
        if (zulieferers.isEmpty()) {
            throw new NotFoundException();
        }

        final var baecker = findBaeckerById(baeckerId);
        final var nachname = baecker == null ? null : baecker.nachname();
        final var vorname = findVornameById(baeckerId);
        log.trace("findByBaeckerId: nachname={}, vorname={}", nachname, vorname);
        zulieferers.forEach(zulieferer -> {
            zulieferer.setBaeckerNachname(nachname);
            zulieferer.setBaeckerVorname(vorname);
        });

        log.trace("findBybaeckerId: bestellungen={}", zulieferers);
        return zulieferers;
    }

    /**
     * Alle Zulieferer ermitteln.
     *
     * @return Alle Zulieferer.
     */
    public Collection<Zulieferer> findAll() {
        final var zulieferers = repo.findAll();
        zulieferers.forEach(zulieferer -> {
            final var baeckerId = zulieferer.getBaeckerId();
            final var baecker = findBaeckerById(baeckerId);
            final var vorname = findVornameById(baeckerId);
            zulieferer.setBaeckerNachname(baecker.nachname());
            zulieferer.setBaeckerVorname(vorname);
        });
        return zulieferers;
    }

    @SuppressWarnings("ReturnCount")
    private Baecker findBaeckerById(final UUID baeckerId) {
        log.debug("findbaeckerById: baeckerId={}", baeckerId);

        final ResponseEntity<Baecker> response;
        try {
            response = baeckerRestRepository.getBaecker(baeckerId);
        } catch (final WebClientResponseException.NotFound ex) {
            // Statuscode 404
            log.error("findBaeckerById: WebClientResponseException.NotFound");
            return new Baecker("N/A", "N/A");
        } catch (final WebClientException ex) {
            // sonstiger Statuscode 4xx oder 5xx
            // WebClientRequestException oder WebClientResponseException (z.B. ServiceUnavailable)
            log.error("findBaeckerById: {}", ex.getClass().getSimpleName());
            return new Baecker("Exception", "exception vorname");
        }

        final var statusCode = response.getStatusCode();
        log.debug("findBaeckerById: statusCode={}", statusCode);
        if (statusCode == NOT_MODIFIED) {
            return new Baecker("Not-Modified", "not.modified@acme.com");
        }

        final var baecker = response.getBody();
        log.debug("findBaeckerById: {}", baecker);
        return baecker;
    }

    private String findVornameById(final UUID baeckerId) {
        log.debug("findVornameById: baeckerId={}", baeckerId);

        final var query = """
            query {
                baecker(id: "%s") {
                    vorname
                }
            }
            """.formatted(baeckerId);

        final String vorname;
        try {
            vorname = graphQlClient
                .mutate()
                .build()
                .document(query)
                .retrieve("baecker")
                .toEntity(BaeckerVorname.class)
                .map(BaeckerVorname::vorname)
                .block();
        } catch (final FieldAccessException | GraphQlTransportException ex) {
            log.warn("findEmailById: {}", ex.getClass().getSimpleName());
            return "N/A";
        }

        log.debug("findEmailById: email={}", vorname);
        return vorname;
    }
}
