package zspace.zulieferer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;

/**
 * Filiale class.
 */
@Entity
@QueryEntity
@Table(name = "filiale")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Filiale {

    /**
     * Regulärer Ausdruck für die Variable name.
     */
    public static final String NAME_PATTERN = "[A-ZÄÖÜ][a-zäöüß]+";

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private UUID id;

    @Pattern(regexp = NAME_PATTERN)
    @NotNull
    private String name;

    @NotBlank
    private String standort;

    @OneToOne(optional = false, fetch = LAZY)
    @JoinColumn(updatable = false)
    @JsonIgnore
    @ToString.Exclude
    private Zulieferer zulieferer;
}
