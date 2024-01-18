package zspace.zulieferer.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import zspace.zulieferer.service.ConstraintViolationsException;
import zspace.zulieferer.service.EmailExistsException;
import zspace.zulieferer.service.VersionOutdatedException;
import zspace.zulieferer.service.ZuliefererWriteService;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static org.springframework.http.HttpStatus.PRECONDITION_REQUIRED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static zspace.zulieferer.rest.ZuliefererGetController.PROBLEM_PATH;
import static zspace.zulieferer.rest.ZuliefererGetController.REST_PATH;

/**
 * write controller klasse.
 *
 */
@Controller
@RequestMapping(REST_PATH)
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings({"ClassFanOutComplexity", "MethodCount", "DuplicatedCode", "UNUSED"})
public class ZuliefererWriteController {

    /**
     * Pattern für die UUID.
     */
    private static final String ID_PATTERN =
        "[\\dA-Fa-f]{8}-[\\dA-Fa-f]{4}-[\\dA-Fa-f]{4}-[\\dA-Fa-f]{4}-[\\dA-Fa-f]{12}";

    private static final String VERSIONSNUMMER_FEHLT = "Versionsnummer fehlt";

    @SuppressWarnings("TrailingComment")
    private final ZuliefererWriteService writeService;
    private final UriHelper uriHelper;
    private final ZuliefererWriteService service;

    /**
     * Einen neuen zulieferer-Datensatz anlegen.
     *
     * @param zuliefererDTO Das Zuliefererbjekt aus dem eingegangenen Request-Body.
     * @param request Das Request-Objekt, um `Location` im Response-Header zu erstellen.
     * @return Response mit Statuscode 201 einschließlich Location-Header oder Statuscode 422 falls Constraints verletzt
     *      sind oder Statuscode 400 falls syntaktische Fehler im Request-Body
     *      vorliegen.
     */
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Einen neuen Zulieferer anlegen", tags = "Neuanlegen")
    @ApiResponse(responseCode = "201", description = "Zulieferer neu angelegt")
    @SuppressWarnings("java:S1133")
    ResponseEntity<Void> post(@RequestBody final ZuliefererDTO zuliefererDTO, final HttpServletRequest request) {
        log.debug("post: {}", zuliefererDTO);

        final var zulieferer = writeService.create(zuliefererDTO.toZulieferer());
        final var baseUri = uriHelper.getBaseUri(request).toString();
        final var location = URI.create(baseUri + '/' + zulieferer.getId());
        return created(location).build();
    }

    /**
     * Einen vorhandenen zulieferer-Datensatz überschreiben.
     *
     * @param id ID des zu aktualisierenden zulieferern.
     * @param zuliefererDTO Das zulieferernobjekt aus dem eingegangenen Request-Body.
     * @param version version
     * @param request request
     * @return response-entity
     */
    @PutMapping(path = "{id:" + ID_PATTERN + "}", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(NO_CONTENT)
    @Operation(summary = "Einen zulieferern mit neuen Werten aktualisieren", tags = "Aktualisieren")
    @ApiResponse(responseCode = "204", description = "Aktualisiert")
    @ApiResponse(responseCode = "400", description = "Syntaktische Fehler im Request-Body")
    @ApiResponse(responseCode = "404", description = "zulieferer nicht vorhanden")
    @ApiResponse(responseCode = "422", description = "Ungültige Werte oder Email vorhanden")
    ResponseEntity<Void> put(
        @PathVariable final UUID id,
        @RequestBody final ZuliefererDTO zuliefererDTO,
        @RequestHeader("If-Match") final Optional<String> version,
        final HttpServletRequest request
    ) {
        log.debug("put: id={}, {}", id, zuliefererDTO);
        final int versionInt = getVersion(version, request);
        final var zulieferer = service.update(zuliefererDTO.toZulieferer(), id, versionInt);
        log.debug("put: {}", zulieferer);
        return noContent().eTag("\"" + zulieferer.getVersion() + '"').build();
    }

    @SuppressWarnings({"MagicNumber", "RedundantSuppression"})
    private int getVersion(final Optional<String> versionOpt, final HttpServletRequest request) {
        log.trace("getVersion: {}", versionOpt);
        if (versionOpt.isEmpty()) {
            throw new VersionInvalidException(
                PRECONDITION_REQUIRED,
                VERSIONSNUMMER_FEHLT,
                URI.create(request.getRequestURL().toString()));
        }

        final var versionStr = versionOpt.get();
        if (versionStr.length() < 3 ||
            versionStr.charAt(0) != '"' ||
            versionStr.charAt(versionStr.length() - 1) != '"') {
            throw new VersionInvalidException(
                PRECONDITION_FAILED,
                "Ungueltiges ETag " + versionStr,
                URI.create(request.getRequestURL().toString())
            );
        }

        final int version;
        try {
            version = Integer.parseInt(versionStr.substring(1, versionStr.length() - 1));
        } catch (final NumberFormatException ex) {
            throw new VersionInvalidException(
                PRECONDITION_FAILED,
                "Ungueltiges ETag " + versionStr,
                URI.create(request.getRequestURL().toString()),
                ex
            );
        }

        log.trace("getVersion: version={}", version);
        return version;
    }

    @ExceptionHandler
    ProblemDetail onConstraintViolations(
        final ConstraintViolationsException ex,
        final HttpServletRequest request
    ) {
        log.debug("onConstraintViolations: {}", ex.getMessage());

        final var zuliefererViolations = ex.getViolations()
            .stream()
            .map(violation -> violation.getPropertyPath() + ": " +
                violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName() + " " +
                violation.getMessage())
            .toList();
        log.trace("onConstraintViolations: {}", zuliefererViolations);
        final String detail;
        if (zuliefererViolations.isEmpty()) {
            detail = "N/A";
        } else {
            final var violationsStr = zuliefererViolations.toString();
            detail = violationsStr.substring(1, violationsStr.length() - 2);
        }

        final var problemDetail = ProblemDetail.forStatusAndDetail(UNPROCESSABLE_ENTITY, detail);
        problemDetail.setType(URI.create(PROBLEM_PATH + ProblemType.CONSTRAINTS.getValue()));
        problemDetail.setInstance(URI.create(request.getRequestURL().toString()));

        return problemDetail;
    }

    @ExceptionHandler
    ProblemDetail onEmailExists(final EmailExistsException ex, final HttpServletRequest request) {
        log.debug("onEmailExists: {}", ex.getMessage());
        final var problemDetail = ProblemDetail.forStatusAndDetail(UNPROCESSABLE_ENTITY, ex.getMessage());
        problemDetail.setType(URI.create(PROBLEM_PATH + ProblemType.CONSTRAINTS.getValue()));
        problemDetail.setInstance(URI.create(request.getRequestURL().toString()));
        return problemDetail;
    }

    @ExceptionHandler
    ProblemDetail onVersionOutdated(
        final VersionOutdatedException ex,
        final HttpServletRequest request
    ) {
        log.debug("onVersionOutdated: {}", ex.getMessage());
        final var problemDetail = ProblemDetail.forStatusAndDetail(PRECONDITION_FAILED, ex.getMessage());
        problemDetail.setType(URI.create(PROBLEM_PATH + ProblemType.PRECONDITION.getValue()));
        problemDetail.setInstance(URI.create(request.getRequestURL().toString()));
        return problemDetail;
    }
}
