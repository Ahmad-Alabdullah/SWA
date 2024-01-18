package zspace.zulieferer.graphql;

import graphql.GraphQLError;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import zspace.zulieferer.entity.Zulieferer;
import zspace.zulieferer.service.ConstraintViolationsException;
import zspace.zulieferer.service.EmailExistsException;
import zspace.zulieferer.service.ZuliefererWriteService;

import static org.springframework.graphql.execution.ErrorType.BAD_REQUEST;

/**
 * Eine Controller-Klasse für das Schreiben mit der GraphQL-Schnittstelle und den Typen aus dem GraphQL-Schema.
 *
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class ZuliefererMutationController {
    private final ZuliefererWriteService service;

    /**
     * Einen neuen Zulieferer anlegen.
     *
     * @param input Die Eingabedaten für einen neuen Zulieferer
     * @return Die generierte ID für den neuen Zulieferer als Payload
     */
    @MutationMapping
    CreatePayload create(@Argument final ZuliefererInput input) {
        log.debug("create: input={}", input);
        final var id = service.create(input.toZulieferer()).getId();
        log.debug("create: id={}", id);
        return new CreatePayload(id);
    }

    @GraphQlExceptionHandler
    GraphQLError handleEmailExists(final EmailExistsException ex) {
        return GraphQLError.newError()
            .errorType(BAD_REQUEST)
            .message("Die Emailadresse " + ex.getEmail() + " existiert bereits.")
            .build();
    }

    @GraphQlExceptionHandler
    Collection<GraphQLError> handleConstraintViolations(final ConstraintViolationsException ex) {
        return ex.getViolations()
            .stream()
            .map(this::violationToGraphQLError)
            .collect(Collectors.toList());
    }

    private GraphQLError violationToGraphQLError(final ConstraintViolation<Zulieferer> violation) {
        final List<Object> path = new ArrayList<>(5);
        path.add("input");
        for (final Path.Node node: violation.getPropertyPath()) {
            path.add(node.toString());
        }
        return GraphQLError.newError()
            .errorType(BAD_REQUEST)
            .message(violation.getMessage())
            .path(path)
            .build();
    }
}
