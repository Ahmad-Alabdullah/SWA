package zspace.zulieferer.graphql;

import graphql.GraphQLError;
import zspace.zulieferer.service.NotFoundException;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

import static org.springframework.graphql.execution.ErrorType.NOT_FOUND;

/**
 * Abbildung von Exceptions auf GraphQLError.
 */
@ControllerAdvice
final class ExceptionHandler {
    @GraphQlExceptionHandler
    GraphQLError handleNotFound(final NotFoundException ex) {
        final var id = ex.getId();
        final var message = id == null
            ? "Kein Zulieferer gefunden: suchkriterien=" + ex.getSuchkriterien()
            : "Kein Zulieferer mit der ID " + id + " gefunden";
        return GraphQLError.newError()
            .errorType(NOT_FOUND)
            .message(message)
            .build();
    }
}
