package com.ucp.biomass.ar.domain.usecase

import com.ucp.biomass.ar.domain.model.Somatotype
import com.ucp.biomass.ar.domain.repository.IBodyScannerRepository

/**
 * Caso de uso para alternar y calibrar el somatotipo morfológico del sujeto.
 */
class CalibrateSomatotypeUseCase(
    private val repository: IBodyScannerRepository
) {
    operator fun invoke(somatotype: Somatotype) {
        repository.setSomatotype(somatotype)
    }
}
