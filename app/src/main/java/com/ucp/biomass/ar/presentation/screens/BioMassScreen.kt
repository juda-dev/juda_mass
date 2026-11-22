package com.ucp.biomass.ar.presentation.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ucp.biomass.ar.domain.model.Somatotype
import com.ucp.biomass.ar.presentation.components.ArCameraViewport
import com.ucp.biomass.ar.presentation.components.HeightCalibrationDialog
import com.ucp.biomass.ar.presentation.components.MeshOverlayCanvas
import com.ucp.biomass.ar.presentation.components.SegmentDetailsSheet
import com.ucp.biomass.ar.presentation.components.SensorConfidenceGauge
import com.ucp.biomass.ar.presentation.components.ShimmerMeshOverlay
import com.ucp.biomass.ar.presentation.components.SomatotypeGuideDialog
import com.ucp.biomass.ar.presentation.components.SomatotypeSelector
import com.ucp.biomass.ar.presentation.components.TelemetryHud
import com.ucp.biomass.ar.presentation.theme.BioMassAmberOchre
import com.ucp.biomass.ar.presentation.theme.BioMassBorderOutline
import com.ucp.biomass.ar.presentation.theme.BioMassBorderSubtle
import com.ucp.biomass.ar.presentation.theme.BioMassChampagneGold
import com.ucp.biomass.ar.presentation.theme.BioMassJadeSuccess
import com.ucp.biomass.ar.presentation.theme.BioMassSapphirePrimary
import com.ucp.biomass.ar.presentation.theme.BioMassSurfaceDark
import com.ucp.biomass.ar.presentation.theme.BioMassSurfaceElevated
import com.ucp.biomass.ar.presentation.theme.BioMassTextPrimary
import com.ucp.biomass.ar.presentation.theme.BioMassTextSecondary
import com.ucp.biomass.ar.presentation.theme.BioMassVoidBlack
import com.ucp.biomass.ar.presentation.viewmodel.BioMassUiState
import com.ucp.biomass.ar.presentation.viewmodel.BioMassViewModel
import java.util.Locale

/**
 * Pantalla principal de JuDaMass con diseño ejecutivo Obsidian & Titanium Sapphire.
 *
 * Características principales:
 * - Nombre oficial: JuDaMass (sin versiones visuales en la interfaz).
 * - Desglose matemático unificado, sin desbordes y 100% responsivo.
 * - Función "Guardar Recuerdo" con todos los datos y firma "by: JuDa Dev".
 * - Calibración de estatura y asistente interactivo de somatotipo.
 */
@Composable
fun BioMassScreen(
    viewModel: BioMassViewModel,
    onRequestCameraPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Notificaciones de guardado de recuerdo
    LaunchedEffect(uiState.snapshotMessage) {
        uiState.snapshotMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearSnapshotMessage()
        }
    }

    // Diálogos modales
    if (uiState.isSomatotypeGuideVisible) {
        SomatotypeGuideDialog(
            currentSomatotype = uiState.activeSomatotype,
            suggestedSomatotype = uiState.suggestedSomatotype,
            onSelectSomatotype = viewModel::selectSomatotype,
            onDismiss = viewModel::closeSomatotypeGuide
        )
    }

    if (uiState.isHeightDialogVisible) {
        HeightCalibrationDialog(
            currentHeightM = uiState.effectiveHeightMeters,
            isManual = uiState.userCalibratedHeightM != null,
            onSaveHeight = viewModel::setCalibratedHeight,
            onDismiss = viewModel::closeHeightDialog
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(BioMassVoidBlack)
    ) {
        val isTabletLayout = maxWidth >= 600.dp

        if (isTabletLayout) {
            TabletLayout(
                uiState = uiState,
                onSelectSomatotype = viewModel::selectSomatotype,
                onToggleDetails = viewModel::toggleDetailsSheet,
                onToggleHud = viewModel::toggleHudVisibility,
                onOpenSomatotypeGuide = viewModel::openSomatotypeGuide,
                onOpenHeightDialog = viewModel::openHeightDialog,
                onCaptureSnapshot = { viewModel.captureSnapshot(context) },
                onRequestCameraPermission = onRequestCameraPermission,
                onProcessFrame = viewModel::processCameraFrame
            )
        } else {
            PhoneLayout(
                uiState = uiState,
                onSelectSomatotype = viewModel::selectSomatotype,
                onToggleDetails = viewModel::toggleDetailsSheet,
                onToggleHud = viewModel::toggleHudVisibility,
                onOpenSomatotypeGuide = viewModel::openSomatotypeGuide,
                onOpenHeightDialog = viewModel::openHeightDialog,
                onCaptureSnapshot = { viewModel.captureSnapshot(context) },
                onRequestCameraPermission = onRequestCameraPermission,
                onProcessFrame = viewModel::processCameraFrame
            )
        }
    }
}

/**
 * Layout optimizado para Smartphones con padding defensivo y diseño simétrico.
 */
@Composable
private fun PhoneLayout(
    uiState: BioMassUiState,
    onSelectSomatotype: (Somatotype) -> Unit,
    onToggleDetails: () -> Unit,
    onToggleHud: () -> Unit,
    onOpenSomatotypeGuide: () -> Unit,
    onOpenHeightDialog: () -> Unit,
    onCaptureSnapshot: () -> Unit,
    onRequestCameraPermission: () -> Unit,
    onProcessFrame: (androidx.camera.core.ImageProxy) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Capa de Fondo: Flujo de Cámara en Vivo con análisis ML Kit
        ArCameraViewport(
            hasCameraPermission = uiState.hasCameraPermission,
            onFrameAvailable = onProcessFrame,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Malla Alámbrica 3D proyectada sobre el cuerpo
        MeshOverlayCanvas(
            landmarks = uiState.poseLandmarks,
            geometries = uiState.segmentGeometries,
            isScanningActive = uiState.isScanningActive && uiState.isBodyDetected,
            modifier = Modifier.fillMaxSize()
        )

        // 3. Shimmer Effect holográfico
        ShimmerMeshOverlay(
            isActive = uiState.isScanningActive && uiState.isBodyDetected,
            modifier = Modifier.fillMaxSize()
        )

        // 4. Barra Superior Flotante: Nombre "JuDa Mass", Estatura, Recuerdo y Ocultar/Ver
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BioMassSurfaceDark.copy(alpha = 0.94f))
                    .border(1.dp, BioMassBorderOutline, RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Título oficial JuDa Mass y estado de conexión (acotado para no desplazar botones)
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(end = 4.dp)
                ) {
                    Text(
                        text = "JuDa Mass",
                        color = BioMassTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 1.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isBodyDetected) BioMassJadeSuccess else BioMassAmberOchre)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (uiState.isBodyDetected) "Cuerpo bloqueado" else "Buscando sujeto...",
                            color = if (uiState.isBodyDetected) BioMassJadeSuccess else BioMassAmberOchre,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Acciones de la barra superior (diseño compacto protegido contra saltos de línea)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!uiState.hasCameraPermission) {
                        Button(
                            onClick = onRequestCameraPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = BioMassSapphirePrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(text = "CÁMARA", color = BioMassSurfaceDark, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    } else {
                        // Botón de Captura de Recuerdo (con fecha, datos y "by: JuDa Dev")
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BioMassSurfaceElevated)
                                .border(1.dp, BioMassBorderSubtle, RoundedCornerShape(8.dp))
                                .clickable { onCaptureSnapshot() }
                                .padding(horizontal = 7.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Guardar Recuerdo",
                                tint = BioMassSapphirePrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Recuerdo",
                                color = BioMassTextPrimary,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        // Selector de Estatura
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BioMassSurfaceElevated)
                                .border(1.dp, BioMassBorderSubtle, RoundedCornerShape(8.dp))
                                .clickable { onOpenHeightDialog() }
                                .padding(horizontal = 7.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Height,
                                contentDescription = "Estatura",
                                tint = BioMassChampagneGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = String.format(Locale.US, "%.2fm", uiState.effectiveHeightMeters),
                                color = BioMassTextPrimary,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        // Conmutador de Visibilidad (Ocultar / Ver)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BioMassSurfaceElevated)
                                .border(1.dp, BioMassSapphirePrimary.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                                .clickable { onToggleHud() }
                                .padding(horizontal = 7.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (uiState.isHudVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (uiState.isHudVisible) "Ocultar panel" else "Mostrar panel",
                                tint = BioMassSapphirePrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (uiState.isHudVisible) "Ocultar" else "Ver",
                                color = BioMassSapphirePrimary,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }

        // 5. Panel Inferior de Telemetría (Ocultable o Expandido)
        if (uiState.isHudVisible) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // HUD de Telemetría Principal
                TelemetryHud(
                    result = uiState.calculationResult,
                    onOpenHeightDialog = onOpenHeightDialog
                )

                // Selector de Somatotipo y Acceso a la Guía
                SomatotypeSelector(
                    selectedSomatotype = uiState.activeSomatotype,
                    onSomatotypeSelected = onSelectSomatotype,
                    onOpenGuide = onOpenSomatotypeGuide
                )

                // Tarjeta Unificada y Responsiva de Desglose Matemático
                SegmentDetailsSheet(
                    segmentResults = uiState.calculationResult.segmentResults,
                    isExpanded = uiState.isDetailsSheetExpanded,
                    onToggleExpand = onToggleDetails
                )

                // Margen defensivo inferior para nunca colisionar con la barra de navegación del sistema
                Spacer(modifier = Modifier.height(20.dp))
            }
        } else {
            // Modo Minimalista Inmersivo (95% de pantalla libre para la cámara)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(BioMassSurfaceDark.copy(alpha = 0.94f))
                        .border(1.dp, BioMassSapphirePrimary.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        .clickable { onToggleHud() }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp)
                    ) {
                        val massText = if (uiState.isBodyDetected) "${uiState.calculationResult.meanMassFormatted} kg" else "JuDa Mass listo"
                        val rangeText = if (uiState.isBodyDetected) "Rango: ${uiState.calculationResult.weightRangeFormatted}" else "Apunte al cuerpo completo"

                        Text(
                            text = massText,
                            color = BioMassTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = rangeText,
                            color = BioMassSapphirePrimary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BioMassSurfaceElevated)
                            .border(1.dp, BioMassJadeSuccess.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "MOSTRAR PANEL",
                                color = BioMassJadeSuccess,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = null,
                                tint = BioMassJadeSuccess,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Layout optimizado para Tablets (Split View).
 */
@Composable
private fun TabletLayout(
    uiState: BioMassUiState,
    onSelectSomatotype: (Somatotype) -> Unit,
    onToggleDetails: () -> Unit,
    onToggleHud: () -> Unit,
    onOpenSomatotypeGuide: () -> Unit,
    onOpenHeightDialog: () -> Unit,
    onCaptureSnapshot: () -> Unit,
    onRequestCameraPermission: () -> Unit,
    onProcessFrame: (androidx.camera.core.ImageProxy) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Columna Izquierda: Viewport AR con cámara en vivo y malla
        Box(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, BioMassBorderOutline, RoundedCornerShape(18.dp))
        ) {
            ArCameraViewport(
                hasCameraPermission = uiState.hasCameraPermission,
                onFrameAvailable = onProcessFrame,
                modifier = Modifier.fillMaxSize()
            )

            MeshOverlayCanvas(
                landmarks = uiState.poseLandmarks,
                geometries = uiState.segmentGeometries,
                isScanningActive = uiState.isScanningActive && uiState.isBodyDetected,
                modifier = Modifier.fillMaxSize()
            )

            ShimmerMeshOverlay(
                isActive = uiState.isScanningActive && uiState.isBodyDetected,
                modifier = Modifier.fillMaxSize()
            )

            // Encabezado en el viewport
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "JuDa Mass • Visor Biomecánico",
                    color = BioMassSapphirePrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BioMassSurfaceElevated)
                            .clickable { onCaptureSnapshot() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Guardar Recuerdo",
                            tint = BioMassSapphirePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Recuerdo", color = BioMassTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (!uiState.hasCameraPermission) {
                        Button(
                            onClick = onRequestCameraPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = BioMassSapphirePrimary)
                        ) {
                            Text(text = "CONCEDER CÁMARA", color = BioMassSurfaceDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            SensorConfidenceGauge(
                trackingStatus = uiState.trackingStatus,
                confidence = uiState.sensorConfidence,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .align(Alignment.BottomCenter)
            )
        }

        // Columna Derecha: Panel de Control Matemático
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "PANEL DE TELEMETRÍA (INTEGRALES TRIPLES)",
                color = BioMassTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            TelemetryHud(
                result = uiState.calculationResult,
                onOpenHeightDialog = onOpenHeightDialog
            )

            SomatotypeSelector(
                selectedSomatotype = uiState.activeSomatotype,
                onSomatotypeSelected = onSelectSomatotype,
                onOpenGuide = onOpenSomatotypeGuide
            )

            SegmentDetailsSheet(
                segmentResults = uiState.calculationResult.segmentResults,
                isExpanded = uiState.isDetailsSheetExpanded,
                onToggleExpand = onToggleDetails,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
