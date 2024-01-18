package zspace.zulieferer.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zspace.zulieferer.entity.QZulieferer;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Locale.GERMAN;

/**
 * Singleton-Klasse, um Prädikate durch QueryDSL für eine WHERE-Klausel zu bauen.
 *
 */
@Component
@Slf4j
public class PredicateBuilder {
    /**
     * Prädikate durch QueryDSL für eine WHERE-Klausel zu bauen.
     *
     * @param queryParams als MultiValueMap
     * @return Predicate in QueryDSL für eine WHERE-Klausel
     */
    @SuppressWarnings("ReturnCount")
    public Optional<Predicate> build(final Map<String, ? extends List<String>> queryParams) {
        log.debug("build: queryParams={}", queryParams);

        final var qZulieferer = QZulieferer.zulieferer;
        final var booleanExprList = queryParams
            .entrySet()
            .stream()
            .map(entry -> toBooleanExpression(entry.getKey(), entry.getValue(), qZulieferer))
            .toList();
        if (booleanExprList.isEmpty() || booleanExprList.contains(null)) {
            return Optional.empty();
        }

        final var result = booleanExprList
            .stream()
            .reduce(booleanExprList.get(0), BooleanExpression::and);
        return Optional.of(result);
    }

    @SuppressWarnings({"CyclomaticComplexity", "UnnecessaryParentheses"})
    private BooleanExpression toBooleanExpression(
        final String paramName,
        final List<String> paramValues,
        final QZulieferer qZulieferer
    ) {
        log.trace("toSpec: paramName={}, paramValues={}", paramName, paramValues);

        if (paramValues == null || (!Objects.equals(paramName, "interesse") && paramValues.size() != 1)) {
            return null;
        }

        final var value = paramValues.get(0);
        return switch (paramName) {
            case "name" -> name(value, qZulieferer);
            case "email" ->  email(value, qZulieferer);
            default -> null;
        };
    }

    private BooleanExpression name(final String teil, final QZulieferer qZulieferer) {
        return qZulieferer.name.toLowerCase().matches("%" + teil.toLowerCase(GERMAN) + '%');
    }

    private BooleanExpression email(final String teil, final QZulieferer qZulieferer) {
        return qZulieferer.email.toLowerCase().matches("%" + teil.toLowerCase(GERMAN) + '%');
    }
}
