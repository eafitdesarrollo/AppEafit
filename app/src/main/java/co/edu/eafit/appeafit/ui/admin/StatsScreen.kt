package co.edu.eafit.appeafit.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.edu.eafit.appeafit.R
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.domain.model.LostItemStatus
import co.edu.eafit.appeafit.domain.model.ReservationStatus
import co.edu.eafit.appeafit.domain.model.Role

private data class Stat(val label: String, val value: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(container: AppContainer, onBack: () -> Unit) {
    var stats by remember { mutableStateOf<List<Stat>>(emptyList()) }

    LaunchedEffect(Unit) {
        val users = container.userRepository.listUsers().getOrDefault(emptyList())
        val lostItems = container.lostItemRepository.list().getOrDefault(emptyList())
        val reservations = container.reservationRepository.listAll().getOrDefault(emptyList())

        stats = listOf(
            Stat("Estudiantes", users.count { it.role == Role.STUDENT }),
            Stat("Profesores", users.count { it.role == Role.PROFESSOR }),
            Stat("Administrativos", users.count { it.role == Role.STAFF }),
            Stat("Objetos perdidos activos", lostItems.count { it.status != LostItemStatus.CLAIMED.id }),
            Stat("Reservas pendientes", reservations.count { it.status == ReservationStatus.PENDING.id }),
            Stat("Total usuarios", users.size)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.service_stats)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(stats) { stat ->
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                        Text(stat.value.toString(), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                        Text(stat.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
