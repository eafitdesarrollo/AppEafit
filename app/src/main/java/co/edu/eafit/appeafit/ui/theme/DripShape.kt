package co.edu.eafit.appeafit.ui.theme

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Silueta de "pintura derritiéndose": el borde inferior recto se reemplaza por
 * `dripCount` goterones redondeados de largo variable, como pintura o cera goteando.
 * Pensado para [co.edu.eafit.appeafit.ui.components.GradientHeroBox] en vez de una
 * esquina simplemente redondeada — pedido explícito de Santiago Guerrero Parrado (una
 * imagen de referencia de "melting drip" en azules) porque tanto el corte recto como la
 * esquina redondeada seguían viéndose "cuadrados".
 *
 * `dripZoneHeight` es una medida fija en dp (no un porcentaje del alto total) para que el
 * goteo se vea igual de grande en una cabecera corta (Home) que en una pantalla completa
 * (Splash/Carnet) — cae dentro del padding inferior que ya tenían esas pantallas, así que
 * no hace falta agregar espacio extra.
 *
 * `dripDepthPattern` fija el largo relativo (0..1) de cada goterón en orden fijo (no
 * aleatorio en cada recomposición) para que el resultado sea siempre el mismo; se repite
 * (`% size`) si `dripCount` es mayor que el patrón.
 */
class DripShape(
    private val dripCount: Int = 7,
    private val dripZoneHeight: Dp = 32.dp,
    private val dripDepthPattern: FloatArray = floatArrayOf(0.35f, 1f, 0.5f, 0.8f, 0.3f, 0.95f, 0.55f)
) : Shape {

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val width = size.width
        val height = size.height
        val zonePx = with(density) { dripZoneHeight.toPx() }
        val baseY = (height - zonePx).coerceAtLeast(0f)
        val segmentWidth = width / dripCount

        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(width, 0f)
            lineTo(width, baseY)

            // Se recorre de derecha a izquierda porque el trazo ya viene de (width, baseY).
            for (i in dripCount - 1 downTo 0) {
                val xLeft = segmentWidth * i
                val xRight = segmentWidth * (i + 1)
                val xMid = (xLeft + xRight) / 2f
                val depth = dripDepthPattern[i % dripDepthPattern.size]
                val tipY = baseY + zonePx * depth
                val shoulder = segmentWidth * 0.12f
                val curve = segmentWidth * 0.20f

                // Hombro derecho -> punta redondeada del goterón.
                cubicTo(
                    xRight - shoulder, baseY,
                    xMid + curve, tipY,
                    xMid, tipY
                )
                // Punta -> hombro izquierdo del siguiente goterón.
                cubicTo(
                    xMid - curve, tipY,
                    xLeft + shoulder, baseY,
                    xLeft, baseY
                )
            }

            lineTo(0f, baseY)
            close()
        }
        return Outline.Generic(path)
    }
}
