package co.edu.eafit.appeafit.ui.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import co.edu.eafit.appeafit.domain.model.User
import co.edu.eafit.appeafit.ui.components.ServiceCard

@Composable
fun ServicesScreen(user: User, navController: NavHostController) {
    val services = remember(user.role) { ServiceCatalog.forRole(user.role) }
    val grouped = remember(services) { services.groupBy { it.group } }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        grouped.forEach { (group, entries) ->
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Text(
                    text = group,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                )
            }
            items(entries) { entry ->
                ServiceCard(
                    icon = entry.icon,
                    label = stringResource(entry.labelResId),
                    onClick = { navController.navigate(entry.route) }
                )
            }
        }
    }
}
