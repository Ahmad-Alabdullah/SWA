package zspace.zulieferer.entity;

import com.querydsl.core.annotations.QueryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.FetchType.LAZY;
import static zspace.zulieferer.entity.Filiale.NAME_PATTERN;
import static zspace.zulieferer.entity.Zulieferer.FILIALE_GRAPH;

/**
 * zulieferer class.
 * <img src="C:\Users\Student\IdeaProjects\Zulieferer\extras\doc\Zulieferer.svg" alt="Klassendiagramm">
 */
@Entity
@QueryEntity
@Table(name = "zulieferer")
@NamedEntityGraph(name = FILIALE_GRAPH, attributeNodes = @NamedAttributeNode("filiale"))
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@Setter
@SuppressWarnings({
    "ClassFanOutComplexity",
    "RequireEmptyLineBeforeBlockTagGroup",
    "DeclarationOrder",
    "MagicNumber",
    "JavadocDeclaration",
    "MissingSummary",
    "RedundantSuppression"})
public class Zulieferer {

    /**
     * NamedEntityGraph für das Attribut "adresse".
     */
    public static final String FILIALE_GRAPH = "Zulieferer.filiale";

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private UUID id;

    @Pattern(regexp = NAME_PATTERN)
    @NotNull
    @Size(max = 40)
    private String name;

    @Email
    @NotNull
    @Size(max = 40)
    private String email;

    /**
     * Versionsnummer für optimistische Synchronisation.
     */
    @Version
    private int version;

    @OneToOne(mappedBy = "zulieferer", optional = false, cascade = {PERSIST, REMOVE},
        fetch = LAZY, orphanRemoval = true)
    @Valid
    @ToString.Exclude
    private Filiale filiale;

    @CreationTimestamp
    private LocalDateTime erzeugt;

    @UpdateTimestamp
    private LocalDateTime aktualisiert;

    @Column(name = "baecker_id")
    // @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.CHAR)
    private UUID baeckerId;

    @Transient
    private String baeckerNachname;

    @Transient
    private String baeckerVorname;

    /**
     * Zuliefererdaten überschreiben.
     *
     * @param zulieferer Neue Zuliefererdaten.
    */
    public void set(final Zulieferer zulieferer) {
        name = zulieferer.name;
        email = zulieferer.email;
    }
}
