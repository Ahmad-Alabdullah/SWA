/*
 * Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package zspace.zulieferer.graphql;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import zspace.zulieferer.entity.Zulieferer;
import zspace.zulieferer.service.ZuliefererReadService;

import static java.util.Collections.emptyMap;

/**
 * Eine Controller-Klasse für das Lesen mit der GraphQL-Schnittstelle und den Typen aus dem GraphQL-Schema.
 *
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ZuliefererQueryController {
    private final ZuliefererReadService service;

    /**
     * Suche anhand der Zulieferer-ID.
     *
     * @param id ID des zu suchenden Zulieferern
     * @return Der gefundene Zulieferer
     */
    @QueryMapping
    Zulieferer zulieferer(@Argument final UUID id) {
        log.debug("zulieferer: id={}", id);
        final var zulieferer = service.findById(id);
        log.debug("zulieferer: {}", zulieferer);
        return zulieferer;
    }

    /**
     * Suche mit diversen Suchkriterien.
     *
     * @param input Suchkriterien und ihre Werte, z.B. `nachname` und `Alpha`
     * @return Die gefundenen Zulieferern als Collection
     */
    @QueryMapping
    Collection<Zulieferer> zulieferern(@Argument final Optional<Suchkriterien> input) {
        log.debug("Zulieferern: suchkriterien={}", input);
        final var suchkriterien = input.map(Suchkriterien::toMap).orElse(emptyMap());
        final var zulieferern = service.find(suchkriterien);
        log.debug("Zulieferern: {}", zulieferern);
        return zulieferern;
    }
}
