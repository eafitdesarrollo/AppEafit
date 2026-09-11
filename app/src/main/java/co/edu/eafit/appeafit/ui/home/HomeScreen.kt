package co.edu.eafit.appeafit.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import co.edu.eafit.appeafit.R
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.core.di.GenericViewModelFactory
import co.edu.eafit.appeafit.domain.model.User
import co.edu.eafit.appeafit.ui.components.NewsCard
import co.edu.eafit.appeafit.ui.components.SectionHeader
import co.edu.eafit.appeafit.ui.components.ServiceCard
import co.edu.eafit.appeafit.ui.navigation.Routes
import co.edu.eafit.appeafit.ui.services.ServiceCatalog

@Composable
fun HomeScreen(container: AppContainer, user: User, navController: NavHostController) {
    val viewModel: HomeViewModel = viewModel(factory = GenericViewModelFactory { HomeViewModel(container) })
    val news by viewModel.news.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }

    val favorites = remember(user.role) { ServiceCatalog.forRole(user.role).take(4) }

    Column(modifier = Modifier.fillMaxSize().padding(bottom = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "¡Qué gusto verte!",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    user.fullName.substringBefore(" ").ifBlank { "Eafitense" },
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            IconButton(onClick = { navController.navigate(Routes.NOTIFICATIONS) }) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)) {
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = stringResource(R.string.notifications_title),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text(stringResource(R.string.common_search_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)
            )
            SectionHeader(title = "Favoritos", modifier = Modifier.padding(horizontal = 20.dp))
            androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favorites) { entry ->
                    ServiceCard(
                        icon = entry.icon,
                        label = stringResource(entry.labelResId),
                        modifier = Modifier.size(96.dp),
                        onClick = { navController.navigate(entry.route) }
                    )
                }
            }

            if (news.isNotEmpty()) {
                androidx.compose.foundation.layout.Spacer(Modifier.height(24.dp))
                SectionHeader(title = "Actualidad EAFIT", modifier = Modifier.padding(horizontal = 20.dp))
                androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(news, key = { it.id }) { item -> NewsCard(item = item) }
                }
                androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
            }
        }
    }
}
