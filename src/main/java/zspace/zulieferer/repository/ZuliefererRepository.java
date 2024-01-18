package zspace.zulieferer.repository;

import com.querydsl.core.types.Predicate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import zspace.zulieferer.entity.Zulieferer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static zspace.zulieferer.entity.Zulieferer.FILIALE_GRAPH;

/**
 * repo class.
 */

@Repository
public interface ZuliefererRepository extends JpaRepository<Zulieferer, UUID>, QuerydslPredicateExecutor<Zulieferer> {

    @EntityGraph(FILIALE_GRAPH)
    @Override
    List<Zulieferer> findAll();

    @EntityGraph(FILIALE_GRAPH)
    @Override
    List<Zulieferer> findAll(Predicate predicate);

    @EntityGraph(FILIALE_GRAPH)
    @Override
    Optional<Zulieferer> findById(UUID id);

    /**
     * Abfrage, ob es einen zulieferer mit gegebener Emailadresse gibt.
     *
     * @param email Emailadresse für die Suche
     * @return true, falls es einen solchen zulieferern gibt, sonst false
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInQuestionForm")
    boolean existsByEmail(String email);

    /**
     * Zulieferer zu gegebener Emailadresse aus der DB ermitteln.
     *
     * @param email Emailadresse für die Suche
     * @return Optional mit dem gefundenen Zulieferer oder leeres Optional
     */
    @Query("""
        SELECT z
        FROM   Zulieferer z
        WHERE  lower(z.email) LIKE concat(lower(:email), '%')
        """)
    @EntityGraph(FILIALE_GRAPH)
    Optional<Zulieferer> findByEmail(String email);

    /**
     * Abfrage zu einem bestimmten Baecker.
     *
     * @param id id
     * @return baecker
     */
    @EntityGraph(FILIALE_GRAPH)
    List<Zulieferer> findByBaeckerId(UUID id);
}
