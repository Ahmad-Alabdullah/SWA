package zspace.zulieferer.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import zspace.zulieferer.entity.Zulieferer;
import zspace.zulieferer.service.ZuliefererReadService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NOT_MODIFIED;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;
import static zspace.zulieferer.rest.ZuliefererGetController.REST_PATH;

/**
 * eine Klasse für GetController.
 * <img src="C:\Users\Student\IdeaProjects\Zulieferer\extras\doc\ZuliefererGetController.svg"
 * alt="Klassendiagramm">
 */
@Controller
@RequestMapping(REST_PATH)
@ResponseBody
@OpenAPIDefinition(info = @Info(title = "Zulieferer API", version = "v1"))
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings({"checkstyle:CyclomaticComplexity", "DeclarationOrder"})
public class ZuliefererGetController {

    /**
     * Ein Pfad für rest.
     */
    static final String REST_PATH = "/rest";

    /**
     * problem-pfad.
     */
    @SuppressWarnings("TrailingComment")
    static final String PROBLEM_PATH = "/problem/";

    private final ZuliefererReadService service;

    private final UriHelper uriHelper;

    /**
     * findbyid methode.
     *
     * @param id die gesuchte id
     * @param request http-request
     * @param version version
     * @return zulieferer mit der gesuchten id
     */
    @GetMapping(path = "{id}", produces = HAL_JSON_VALUE)
    @Operation(summary = "Suche mit der Zulieferer-ID", tags = "Suchen")
    @ApiResponse(responseCode = "200", description = "Zulieferer gefunden")
    @ApiResponse(responseCode = "404", description = "Zulieferer nicht gefunden")
    ResponseEntity<ZuliefererModel> getById(@PathVariable final UUID id,
                                            @RequestHeader("If-None-Match") final Optional<String> version,
                                            final HttpServletRequest request) {
        log.debug("findById: id={}", id);
        log.debug("findById: Thread={}", Thread.currentThread().getName());

        // Geschaeftslogik bzw. Anwendungskern
        final var zulieferer = service.findById(id);

        final var currentVersion = "\"" + zulieferer.getVersion() + '"';
        if (Objects.equals(version.orElse(null), currentVersion)) {
            return status(NOT_MODIFIED).build();
        }

        final var model = zuliefererToModel(zulieferer, request);
        log.debug("getById: model={}", model);
        return ok().eTag(currentVersion).body(model);
    }

    private ZuliefererModel zuliefererToModel(final Zulieferer zulieferer, final HttpServletRequest request) {
        // HATEOAS
        final var model = new ZuliefererModel(zulieferer);
        // evtl. Forwarding von einem API-Gateway
        final var baseUri = uriHelper.getBaseUri(request).toString();
        final var idUri = baseUri + '/' + zulieferer.getId();
        final var selfLink = Link.of(idUri);
        final var listLink = Link.of(baseUri, LinkRelation.of("list"));
        final var addLink = Link.of(baseUri, LinkRelation.of("add"));
        final var updateLink = Link.of(idUri, LinkRelation.of("update"));
        final var removeLink = Link.of(idUri, LinkRelation.of("remove"));
        model.add(selfLink, listLink, addLink, updateLink, removeLink);

        log.debug("findById: {}", model);
        return model;
    }

    @GetMapping(produces = HAL_JSON_VALUE)
    @Operation(summary = "Suche mit Suchkriterien", tags = "Suchen")
    @ApiResponse(responseCode = "200", description = "CollectionModel mid den zulieferern")
    @ApiResponse(responseCode = "404", description = "Keine zulieferer gefunden")
    ResponseEntity<CollectionModel<? extends ZuliefererModel>> get(
        @RequestParam final Map<String, String> suchkriterien,
        final HttpServletRequest request
    ) {
        log.debug("get: queryParams={}", suchkriterien);
        if (suchkriterien.size() > 1) {
            return notFound().build();
        }

        final Collection<Zulieferer> zulieferers;
        if (suchkriterien.isEmpty()) {
            zulieferers = service.findAll();
        } else {
            final var baeckerIdStr = suchkriterien.get("baeckerId");
            if (baeckerIdStr == null) {
                return notFound().build();
            }
            final var baeckerId = UUID.fromString(baeckerIdStr);
            zulieferers = service.findByBaeckerId(baeckerId);
        }

        final var baseUri = uriHelper.getBaseUri(request).toString();
        @SuppressWarnings("LambdaBodyLength")
        final var models = zulieferers
            .stream()
            .map(zulieferer -> {
                final var model = new ZuliefererModel(zulieferer);
                model.add(Link.of(baseUri + '/' + zulieferer.getId()));
                return model;
            })
            .toList();
        log.trace("get: {}", models);

        return ok(CollectionModel.of(models));
    }

    @ExceptionHandler
    ProblemDetail onNotFound(final NotFoundException ex, final HttpServletRequest request) {
        log.debug("onNotFound: {}", ex.getMessage());
        final var problemDetail = ProblemDetail.forStatusAndDetail(NOT_FOUND, ex.getMessage());
        problemDetail.setType(URI.create(PROBLEM_PATH + ProblemType.NOT_FOUND.getValue()));
        problemDetail.setInstance(URI.create(request.getRequestURL().toString()));
        return problemDetail;
    }

}
