package zspace.zulieferer.service;

import jakarta.validation.ConstraintViolation;
import lombok.Getter;
import zspace.zulieferer.entity.Zulieferer;

import java.util.Collection;

/**
 * Exception, falls es mindestens ein verletztes Constraint gibt.
 */
@Getter
public class ConstraintViolationsException extends RuntimeException {
    /**
     * Die verletzten Constraints.
     */
    private final Collection<ConstraintViolation<Zulieferer>> violations;

    ConstraintViolationsException(
        @SuppressWarnings("ParameterHidesMemberVariable")
        final Collection<ConstraintViolation<Zulieferer>> violations
    ) {
        super("Constraints sind verletzt");
        this.violations = violations;
    }
}
