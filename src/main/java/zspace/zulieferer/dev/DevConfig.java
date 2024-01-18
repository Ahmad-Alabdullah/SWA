package zspace.zulieferer.dev;

import org.springframework.context.annotation.Profile;

/**
 * Konfigurationsklasse für die Anwendung bzw. den Microservice, falls das Profile dev aktiviert ist.
 *
 */
@Profile(DevConfig.DEV)
@SuppressWarnings({"ClassNamePrefixedWithPackageName", "HideUtilityClassConstructor"})
public class DevConfig implements Flyway, K8s {
    /**
     * Konstante für das Spring-Profile "dev".
     */
    public static final String DEV = "dev";

    DevConfig() {
    }
}
