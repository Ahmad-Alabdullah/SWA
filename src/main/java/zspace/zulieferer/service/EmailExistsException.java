package zspace.zulieferer.service;

import lombok.Getter;

/**
 * Exception, falls die Emailadresse bereits existiert.
 */
@Getter
public class EmailExistsException extends RuntimeException {
    /**
     * Bereits vorhandene Emailadresse.
     */
    private final String email;

    EmailExistsException(@SuppressWarnings("ParameterHidesMemberVariable") final String email) {
        super("Die Emailadresse " + email + " existiert bereits");
        this.email = email;
    }
}
