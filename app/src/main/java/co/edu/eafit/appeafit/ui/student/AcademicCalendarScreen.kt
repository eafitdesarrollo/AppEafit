package co.edu.eafit.appeafit.ui.student

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.eafit.appeafit.R
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.core.di.GenericViewModelFactory
import co.edu.eafit.appeafit.domain.model.CalendarEvent
import co.edu.eafit.appeafit.ui.components.EafitCard
import co.edu.eafit.appeafit.ui.components.EmptyState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

class CalendarViewModel(private val container: AppContainer) : ViewModel() {
    val events: StateFlow<List<CalendarEvent>> = container.calendarRepository.observeCached()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { container.calendarRepository.refresh() }
    }
}

// No se cachea en un val de nivel de archivo: Locale.getDefault() debe leerse en
// cada uso para reflejar el idioma elegido en Configuración justo después de que
// MainActivity.attachBaseContext lo aplique con Locale.setDefault() en el
// arranque de la Activity (un val de nivel de archivo quedaría con el valor
// del primer idioma cargado en el proceso, aunque luego se recree la Activity).
private fun currentLocale(): Locale = Locale.getDefault()
private val WEEK_DAYS = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

private fun String.capitalizeLocalized(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(currentLocale()) else it.toString() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicCalendarScreen(container: AppContainer, onBack: () -> Unit) {
    val viewModel: CalendarViewModel = viewModel(factory = GenericViewModelFactory { CalendarViewModel(container) })
    val events by viewModel.events.collectAsStateWithLifecycle()

    val today = remember { LocalDate.now() }
    var visibleMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var selectedDay by remember { mutableStateOf(today) }

    val eventsByDay = remember(events) {
        events.groupBy { it.date.toLocalDate() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.service_academic_calendar), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // ---- Encabezado de mes con navegación ----
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { visibleMonth = visibleMonth.minusMonths(1) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.calendar_previous_month))
                }
                AnimatedContent(
                    targetState = visibleMonth,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                    label = "monthLabel"
                ) { month ->
                    Text(
                        text = "${month.month.getDisplayName(TextStyle.FULL, currentLocale()).capitalizeLocalized()} ${month.year}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = { visibleMonth = visibleMonth.plusMonths(1) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.calendar_next_month))
                }
            }

            // ---- Encabezados de día (L M M J V S D) ----
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                WEEK_DAYS.forEach { day ->
                    Text(
                        text = day.getDisplayName(TextStyle.NARROW, currentLocale()).uppercase(currentLocale()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ---- Grilla del mes ----
            AnimatedContent(
                targetState = visibleMonth,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(150)) },
                label = "monthGrid"
            ) { month ->
                val firstOfMonth = month.atDay(1)
                val leadingBlanks = (firstOfMonth.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
                val totalDays = month.lengthOfMonth()
                val cells = leadingBlanks + totalDays

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    userScrollEnabled = false
                ) {
                    items(cells) { index ->
                        val dayNumber = index - leadingBlanks + 1
                        if (dayNumber < 1) {
                            androidx.compose.foundation.layout.Box(modifier = Modifier.aspectRatio(1f))
                        } else {
                            val date = month.atDay(dayNumber)
                            val hasEvents = eventsByDay.containsKey(date)
                            val isSelected = date == selectedDay
                            val isToday = date == today
                            val interactionSource = remember { MutableInteractionSource() }
                            Column(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(3.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primaryContainer
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable(interactionSource = interactionSource, indication = null) {
                                        selectedDay = date
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = dayNumber.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                if (hasEvents) {
                                    androidx.compose.foundation.layout.Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary,
                                                CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 8.dp))
            androidx.compose.material3.HorizontalDivider()

            // ---- Eventos del día seleccionado ----
            val dayEvents = eventsByDay[selectedDay].orEmpty().sortedBy { it.date }
            AnimatedContent(
                targetState = selectedDay,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(150)) },
                label = "dayEvents",
                modifier = Modifier.fillMaxSize()
            ) { day ->
                if (dayEvents.isEmpty()) {
                    EmptyState(
                        message = if (day == today) stringResource(R.string.calendar_no_events_today) else stringResource(R.string.calendar_no_events_day),
                        icon = Icons.Filled.EventAvailable
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = stringResource(
                                    R.string.calendar_day_header,
                                    day.dayOfWeek.getDisplayName(TextStyle.FULL, currentLocale()).capitalizeLocalized(),
                                    day.dayOfMonth,
                                    day.month.getDisplayName(TextStyle.FULL, currentLocale())
                                ),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        items(dayEvents, key = { it.id }) { event ->
                            EafitCard {
                                Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.EventNote,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            event.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (event.description.isNotBlank()) {
                                            Text(
                                                event.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
