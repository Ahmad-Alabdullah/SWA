/*
 * Copyright (C) 2018 - present Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package zspace.zulieferer;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.UriComponentsBuilder;
import zspace.zulieferer.repository.BaeckerRestRepository;

import java.time.Duration;

/**
 * Beans für die REST-Schnittstelle zu "Baecker" (WebClient) und für die GraphQL-Schnittstelle zu "Baecker"
 * (HttpGraphQlClient).
 *
 */
interface HttpClientConfig {
    String GRAPHQL_PATH = "/graphql";
    int BAECKER_DEFAULT_PORT = 8081;
    int TIMEOUT_IN_SECONDS = 10;

    @Bean
    default WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    @SuppressWarnings("CallToSystemGetenv")
    default UriComponentsBuilder uriComponentsBuilder() {
        // Umgebungsvariable in Kubernetes
        final var baeckerHostEnv = System.getenv("BAECKER_SERVICE_HOST");
        final var baeckerPortEnv = System.getenv("BAECKER_SERVICE_PORT");

        @SuppressWarnings("VariableNotUsedInsideIf")
        final var schema = baeckerHostEnv == null ? "https" : "http";
        final var baeckerHost = baeckerHostEnv == null ? "localhost" : baeckerHostEnv;
        final int baeckerPort = baeckerPortEnv == null ? BAECKER_DEFAULT_PORT : Integer.parseInt(baeckerPortEnv);

        final var log = LoggerFactory.getLogger(HttpClientConfig.class);
        log.error("baeckerHost: {}, baeckerPort: {}", baeckerHost, baeckerPort);

        return UriComponentsBuilder.newInstance()
            .scheme(schema)
            .host(baeckerHost)
            .port(baeckerPort);
    }

    // siehe org.springframework.web.reactive.function.client.DefaultWebClient
    @Bean
    default WebClient webClient(
        final WebClient.Builder webClientBuilder,
        final UriComponentsBuilder uriComponentsBuilder
    ) {
        final var baseUrl = uriComponentsBuilder.build().toUriString();
        return webClientBuilder
            .baseUrl(baseUrl)
            .build();
    }

    @Bean
    default BaeckerRestRepository baeckerRestRepository(final WebClient builder) {
        final var clientAdapter = WebClientAdapter.forClient(builder);
        final var proxyFactory = HttpServiceProxyFactory
            .builder(clientAdapter)
            .blockTimeout(Duration.ofSeconds(TIMEOUT_IN_SECONDS))
            .build();
        return proxyFactory.createClient(BaeckerRestRepository.class);
    }

    // siehe org.springframework.graphql.client.DefaultHttpGraphQlClientBuilder.DefaultHttpGraphQlClient
    @Bean
    default HttpGraphQlClient graphQlClient(
        final WebClient.Builder webClientBuilder,
        final UriComponentsBuilder uriComponentsBuilder
    ) {
        final var baseUrl = uriComponentsBuilder
            .path(GRAPHQL_PATH)
            .build()
            .toUriString();
        final var webclient = webClientBuilder
            .baseUrl(baseUrl)
            .build();
        return HttpGraphQlClient.builder(webclient).build();
    }
}
