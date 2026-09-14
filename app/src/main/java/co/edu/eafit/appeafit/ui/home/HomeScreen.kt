package co.edu.eafit.appeafit.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import co.edu.eafit.appeafit.R
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.core.di.GenericViewModelFactory
import co.edu.eafit.appeafit.domain.model.User
import co.edu.eafit.appeafit.ui.components.GradientHeroBox
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
        GradientHeroBox {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.home_greeting),
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        user.fullName.substringBefore(" ").ifBlank { "Eafitense" },
                        color = androidx.compose.ui.graphics.Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = { navController.navigate(Routes.NOTIFICATIONS) }) {
                    Surface(shape = CircleShape, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.15f)) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = stringResource(R.string.notifications_title),
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
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
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)
            )

            val favoritesVisible = remember { MutableTransitionState(false).apply { targetState = true } }
            AnimatedVisibility(
                visibleState = favoritesVisible,
                enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 5 }
            ) {
                Column {
                    SectionHeader(title = stringResource(R.string.home_favorites), modifier = Modifier.padding(horizontal = 20.dp))
                    androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(favorites) { entry ->
                            ServiceCard(
                                icon = entry.icon,
                                label = stringResource(entry.labelResId),
                                modifier = Modifier.width(108.dp),
                                onClick = { navController.navigate(entry.route) }
                            )
                        }
                    }
                }
            }

            val newsVisible = remember { MutableTransitionState(false).apply { targetState = true } }
            AnimatedVisibility(
                visibleState = newsVisible,
                enter = fadeIn(tween(400, delayMillis = 80)) + slideInVertically(tween(400, delayMillis = 80)) { it / 5 }
            ) {
                Column {
                    androidx.compose.foundation.layout.Spacer(Modifier.height(24.dp))
                    SectionHeader(title = stringResource(R.string.home_news_section), modifier = Modifier.padding(horizontal = 20.dp))
                    androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
                    if (news.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(news, key = { it.id }) { item -> NewsCard(item = item) }
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Filled.Campaign,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                                androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                                Text(
                                    stringResource(R.string.home_news_empty),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                    androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
