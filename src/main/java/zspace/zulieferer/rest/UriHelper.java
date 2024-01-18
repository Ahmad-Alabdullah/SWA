package zspace.zulieferer.rest;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static zspace.zulieferer.rest.ZuliefererGetController.REST_PATH;

/**
 * Hilfsklasse um URIs für HATEOAS oder für URIs in ProblemDetail zu ermitteln, falls ein API-Gateway verwendet wird.
 */
@Component
@Slf4j
class UriHelper {

    private static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
    private static final String X_FORWARDED_HOST = "x-forwarded-host";
    private static final String X_FORWARDED_PREFIX = "x-forwarded-prefix";
    //private static final String ZULIEFERER_PREFIX = "/zulieferer";
    private static final String BAECKERS_PREFIX = "/BAECKERS";

    /**
     * Basis-URI ermitteln, d.h. ohne Query-Parameter.
     *
     * @param request Servlet-Request
     * @return Die Basis-URI als String
     */
    URI getBaseUri(final HttpServletRequest request) {
        final var forwardedHost = request.getHeader(X_FORWARDED_HOST);
        if (forwardedHost != null) {
            // Forwarding durch Kubernetes Ingress Controller oder Spring Cloud Gateway
            return getBaseUriForwarded(request, forwardedHost);
        }

        // KEIN Forwarding von einem API-Gateway
        // URI aus Schema, Host, Port und Pfad
        final var uriComponents = ServletUriComponentsBuilder.fromRequestUri(request).build();
        final var baseUri = uriComponents.getScheme() + "://" + uriComponents.getHost() + ':' +
            uriComponents.getPort() + REST_PATH;
        log.debug("getBaseUri (ohne Forwarding): baseUri={}", baseUri);
        return URI.create(baseUri);
    }

    private URI getBaseUriForwarded(final HttpServletRequest request, final String forwardedHost) {
        // x-forwarded-host = Hostname des API-Gateways

        // "https" oder "http"
        final var forwardedProto = request.getHeader(X_FORWARDED_PROTO);
        if (forwardedProto == null) {
            throw new IllegalStateException("Kein \"" + X_FORWARDED_PROTO + "\" im Header");
        }

        var forwardedPrefix = request.getHeader(X_FORWARDED_PREFIX);
        // x-forwarded-prefix: null bei Kubernetes Ingress Controller bzw. "/zulieferer" bei Spring Cloud Gateway
        if (forwardedPrefix == null) {
            log.trace("getBaseUriForwarded: Kein \"" + X_FORWARDED_PREFIX + "\" im Header");
            forwardedPrefix = BAECKERS_PREFIX;
        }
        final var baseUri = forwardedProto + "://" + forwardedHost + forwardedPrefix + REST_PATH;
        log.debug("getBaseUriForwarded: baseUri={}", baseUri);
        return URI.create(baseUri);
    }
}
