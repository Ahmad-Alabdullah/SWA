package zspace.zulieferer.service;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zspace.zulieferer.entity.Zulieferer;
import zspace.zulieferer.repository.ZuliefererRepository;

import java.util.Objects;
import java.util.UUID;

/**
 * Anwendungslogik für Zulieferer.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ZuliefererWriteService {

    private final ZuliefererRepository repo;
    private final Validator validator;

    /**
     * create zulieferer.
     *
     * @param zulieferer zulieferer data
     * @return data from repo
     * @throws ConstraintViolationsException Falls mindestens ein Constraint verletzt ist.
     * @throws EmailExistsException Es gibt bereits einen zulieferern mit der Emailadresse.
     *
     */
    @Transactional
    @SuppressWarnings("NestedIfDepthCheck")
    public Zulieferer create(final Zulieferer zulieferer) {
        log.debug("create: {}", zulieferer);
        log.debug("create: {}", zulieferer.getFiliale());

        final var violations = validator.validate(zulieferer);
        if (!violations.isEmpty()) {
            log.debug("create: violations={}", violations);
            throw new ConstraintViolationsException(violations);
        }

        if (repo.existsByEmail(zulieferer.getEmail())) {
            throw new EmailExistsException(zulieferer.getEmail());
        }

        final var zuliefererDB = repo.save(zulieferer);
        log.debug("create: {}", zuliefererDB);
        return zuliefererDB;
    }

    /**
     * Einen vorhandenen zulieferer aktualisieren.
     *
     * @param zulieferer Das Objekt mit den neuen Daten (ohne ID)
     * @param id         ID des zu aktualisierenden zulieferer
     * @param version version
     * @return zuliefererDb Objekt des zuliefererDb
     * @throws ConstraintViolationsException Falls mindestens ein Constraint verletzt ist.
     * @throws NotFoundException             Kein zulieferer zur ID vorhanden.
     * @throws EmailExistsException          Es gibt bereits einen zulieferer mit der Emailadresse.
     */
    @Transactional
    public Zulieferer update(final Zulieferer zulieferer, final UUID id, final int version) {
        log.debug("update: {}", zulieferer);
        log.debug("update: id={}, update: id={}", id, version);

        final var violations = validator.validate(zulieferer);
        if (!violations.isEmpty()) {
            log.debug("update: violations={}", violations);
            throw new ConstraintViolationsException(violations);
        }
        log.trace("update: Keine Constraints verletzt");

        final var zuliefererDbOptional = repo.findById(id);
        if (zuliefererDbOptional.isEmpty()) {
            throw new NotFoundException(id);
        }

        var zuliefererDb = zuliefererDbOptional.get();
        log.trace("update: version={}, zuliefererDb={}", version, zuliefererDb);
        if (version != zuliefererDb.getVersion()) {
            throw new VersionOutdatedException(version);
        }

        final var email = zulieferer.getEmail();

        if (!Objects.equals(email, zuliefererDb.getEmail()) && repo.existsByEmail(email)) {
            log.debug("update: email {} existiert", email);
            throw new EmailExistsException(email);
        }
        log.trace("update: Kein Konflikt mit der Emailadresse");

        zuliefererDb.set(zulieferer);
        zuliefererDb = repo.save(zuliefererDb);
        log.debug("update: {}", zuliefererDb);
        return zuliefererDb;
    }
}
