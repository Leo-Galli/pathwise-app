package it.leogalli.pathwise.domain.usecase

import it.leogalli.pathwise.domain.engine.PathEvaluator
import it.leogalli.pathwise.domain.model.GroupProfile
import it.leogalli.pathwise.domain.model.PathEvaluation
import it.leogalli.pathwise.domain.model.TrailGeometry
import javax.inject.Inject

/**
 * Caso d'uso per la valutazione della fattibilità di un percorso.
 * Pur essendo un semplice wrapper, esplicita l'intento del dominio
 * e rende il motore sostituibile (es. telemetria, A/B test).
 */
class EvaluatePathUseCase @Inject constructor() {
    operator fun invoke(profile: GroupProfile, geometry: TrailGeometry): PathEvaluation =
        PathEvaluator.evaluate(profile, geometry)
}
