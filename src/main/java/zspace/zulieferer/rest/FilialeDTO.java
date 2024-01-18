package zspace.zulieferer.rest;

import zspace.zulieferer.entity.Zulieferer;

/**
 * record für Filiale.
 *
 * @param name Filialenname
 * @param standort ort
 * @param zulieferer zulieferer
 */
record FilialeDTO(
    String name,
    String standort,
    Zulieferer zulieferer
) {
}
