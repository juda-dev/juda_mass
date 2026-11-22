package com.ucp.biomass.ar.di

import com.ucp.biomass.ar.core.util.DefaultDispatcherProvider
import com.ucp.biomass.ar.core.util.DispatcherProvider
import com.ucp.biomass.ar.data.arcore.ArCoreSessionHelper
import com.ucp.biomass.ar.data.fallback.AnthropometricFallbackModel
import com.ucp.biomass.ar.data.fusion.SensorFusionEngine
import com.ucp.biomass.ar.data.mlkit.MlKitPoseDetectorHelper
import com.ucp.biomass.ar.data.repository.BodyScannerRepositoryImpl
import com.ucp.biomass.ar.domain.math.BioMassMathEngine
import com.ucp.biomass.ar.domain.math.SimpsonTripleIntegrator
import com.ucp.biomass.ar.domain.math.SphericalTripleIntegrator
import com.ucp.biomass.ar.domain.repository.IBodyScannerRepository
import com.ucp.biomass.ar.domain.usecase.CalibrateSomatotypeUseCase
import com.ucp.biomass.ar.domain.usecase.CheckSensorTrackingUseCase
import com.ucp.biomass.ar.domain.usecase.EstimateBiomassUseCase
import com.ucp.biomass.ar.presentation.viewmodel.BioMassViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // 1. Despachadores y Utilidades
    single<DispatcherProvider> { DefaultDispatcherProvider() }

    // 2. Sensores y Visión por Computadora (ARCore + ML Kit)
    single { ArCoreSessionHelper(androidContext()) }
    single { MlKitPoseDetectorHelper() }
    single { AnthropometricFallbackModel() }
    single { SensorFusionEngine(fallbackModel = get()) }

    // 3. Motor Matemático de Dominio (Matemáticas 3)
    single { SimpsonTripleIntegrator() }
    single { SphericalTripleIntegrator() }
    single { BioMassMathEngine(simpsonIntegrator = get(), sphericalIntegrator = get()) }

    // 4. Repositorio de Escaneo Biomecánico
    single<IBodyScannerRepository> {
        BodyScannerRepositoryImpl(
            context = androidContext(),
            arCoreHelper = get(),
            mlKitHelper = get(),
            fusionEngine = get(),
            fallbackModel = get(),
            dispatchers = get()
        )
    }

    // 5. Casos de Uso de Dominio
    factory { EstimateBiomassUseCase(mathEngine = get()) }
    factory { CalibrateSomatotypeUseCase(repository = get()) }
    factory { CheckSensorTrackingUseCase(repository = get()) }

    // 6. Presentación / ViewModel (MVVM)
    viewModel {
        BioMassViewModel(
            repository = get(),
            estimateBiomassUseCase = get(),
            calibrateSomatotypeUseCase = get(),
            checkSensorTrackingUseCase = get(),
            dispatchers = get()
        )
    }
}
