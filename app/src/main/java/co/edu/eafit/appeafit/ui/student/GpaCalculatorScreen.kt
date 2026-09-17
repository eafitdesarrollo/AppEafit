package co.edu.eafit.appeafit.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.eafit.appeafit.R
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.core.di.GenericViewModelFactory
import co.edu.eafit.appeafit.domain.model.User
import co.edu.eafit.appeafit.ui.components.EmptyState
import coil.compose.AsyncImage
import java.util.Locale

// Locale.US (punto decimal) a propósito, no Locale.getDefault(): en un celular en
// español String.format habría escrito "3,8" con coma, pero parseGrade()/toDoubleOrNull
// solo entienden punto -- con coma, notaFinal() SIEMPRE daba null aunque las casillas
// tuvieran texto visible (bug encontrado probando en un celular real con locale es-CO).
private fun formatGrade(value: Double, decimals: Int = 1): String =
    String.format(Locale.US, "%.${decimals}f", value)

// El teclado decimal de Android puede insertar "," en vez de "." según el locale del
// teclado del usuario -- se normaliza antes de parsear para aceptar cualquiera de los dos.
private fun parseGrade(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()

/** Calculadora/simulador de notas: muestra las materias REALES en las que el
 * estudiante está inscrito, con las notas de cada corte que el profesor ya publicó
 * (ver [GpaCalculatorViewModel]). El estudiante puede editar cualquier casilla (incluida
 * una que ya tenga nota real) para simular "en cuánto quedaría la nota final" — el botón
 * flotante (bote de basura) descarta la simulación y vuelve a mostrar solo lo
 * efectivamente publicado. Pedido explícito de Santiago Guerrero Parrado (2026-09-15),
 * con una captura de referencia para el layout (tarjeta de perfil arriba, una fila por
 * materia con C1/C2/C3 y un panel de "Nota final" al lado). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpaCalculatorScreen(container: AppContainer, user: User, onBack: () -> Unit) {
    val viewModel: GpaCalculatorViewModel = viewModel(factory = GenericViewModelFactory { GpaCalculatorViewModel(container, user.uid) })
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Clave "courseId:corte" -> texto en la casilla (nota real publicada, o lo que el
    // estudiante haya escrito para simular). `editedKeys` son las casillas que el
    // estudiante ya tocó a mano -- esas nunca se vuelven a pisar con el valor real que
    // llegue después de Firestore/Room (que llega async: la primera emisión de
    // `state.rows` suele tener las notas todavía vacías, antes de que termine el
    // refresh). Cualquier casilla NO editada sí se mantiene sincronizada con el valor
    // real más reciente cada vez que `state.rows` cambia.
    val cortesState = remember { mutableStateMapOf<String, String>() }
    val editedKeys = remember { mutableStateSetOf<String>() }

    fun keyOf(courseId: String, corte: Int) = "$courseId:$corte"

    fun syncDefaults(rows: List<CourseGradeRow>) {
        rows.forEach { row ->
            for (corte in 1..3) {
                val key = keyOf(row.course.id, corte)
                if (key !in editedKeys) {
                    cortesState[key] = row.realCortes[corte]?.let { formatGrade(it) } ?: ""
                }
            }
        }
    }

    LaunchedEffect(state.rows) { syncDefaults(state.rows) }

    // Nota final: solo cuando los 3 cortes tienen valor (real o simulado) -- pedido
    // explícito de Santiago Guerrero Parrado viendo la app en vivo: mientras falte
    // cualquier corte no hay "nota final" todavía, no un promedio parcial.
    fun notaFinal(courseId: String): Double? {
        val values = (1..3).map { corte -> cortesState[keyOf(courseId, corte)]?.let(::parseGrade) }
        return if (values.any { it == null }) null else values.filterNotNull().average()
    }

    // Promedio semestre: solo cuando TODAS las materias ya tienen su nota final
    // calculada (mismo criterio de arriba) -- si falta una sola materia, no se muestra
    // un promedio parcial. Ponderado por los créditos de cada materia sobre el total de
    // créditos de las materias que sí entran en la cuenta.
    val notasFinales = state.rows.map { row -> row.course to notaFinal(row.course.id) }
    val semesterAverage = if (notasFinales.isEmpty() || notasFinales.any { it.second == null }) {
        null
    } else {
        val totalCredits = notasFinales.sumOf { it.first.credits.toDouble() }
        if (totalCredits <= 0.0) {
            notasFinales.map { it.second!! }.average()
        } else {
            notasFinales.sumOf { (course, average) -> average!! * course.credits } / totalCredits
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.service_gpa_calculator)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                cortesState.clear()
                editedKeys.clear()
                syncDefaults(state.rows)
            }) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.gpa_reset_simulation))
            }
        }
    ) { padding ->
        if (state.rows.isEmpty() && !state.isRefreshing) {
            EmptyState(
                message = stringResource(R.string.gpa_empty_no_courses),
                icon = Icons.Filled.Assessment,
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            item {
                co.edu.eafit.appeafit.ui.components.GradientHeroBox(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (user.photoUrl.isNotBlank()) {
                            AsyncImage(
                                model = user.photoUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.White)
                            )
                        } else {
                            Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(64.dp)) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        user.fullName.take(1).ifBlank { "E" },
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                user.fullName.ifBlank { user.email },
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (user.program.isNotBlank()) {
                                Text(
                                    user.program,
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                stringResource(R.string.gpa_simulator_badge),
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.primary) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.gpa_semester_average), color = Color.White, style = MaterialTheme.typography.labelMedium)
                            Text(
                                semesterAverage?.let { formatGrade(it, decimals = 2) } ?: "—",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            items(state.rows, key = { it.course.id }) { row ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                        Column(
                            modifier = Modifier.weight(1f).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                row.course.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                for (corte in 1..3) {
                                    val key = keyOf(row.course.id, corte)
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            stringResource(R.string.grade_cut_short, corte),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        TextField(
                                            value = cortesState[key] ?: "",
                                            onValueChange = { cortesState[key] = it; editedKeys += key },
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            shape = MaterialTheme.shapes.small,
                                            colors = TextFieldDefaults.colors(
                                                unfocusedIndicatorColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                            ),
                                            modifier = Modifier.width(64.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .width(84.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    stringResource(R.string.gpa_final_grade),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    notaFinal(row.course.id)?.let { formatGrade(it) } ?: "—",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
