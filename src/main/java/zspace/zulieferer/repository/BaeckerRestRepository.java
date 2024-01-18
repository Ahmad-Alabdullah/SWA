package zspace.zulieferer.repository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.UUID;

/**
 * baecker repo.
 */
@FunctionalInterface
@HttpExchange("/rest")
public interface BaeckerRestRepository {
    /**
     * Einen Baeckerdatensatz vom Microservice "Baecker".
     *
     * @param id ID des angeforderten Baecker
     * @return Gefundener Baecker
     */
    @GetExchange("/{id}")
    ResponseEntity<Baecker> getBaecker(
        @PathVariable UUID id
    );
}
