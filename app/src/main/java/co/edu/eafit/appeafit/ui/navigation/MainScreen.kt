package co.edu.eafit.appeafit.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.domain.model.User
import co.edu.eafit.appeafit.ui.carnet.CarnetScreen
import co.edu.eafit.appeafit.ui.components.EafitBottomBar
import co.edu.eafit.appeafit.ui.components.MainTab
import co.edu.eafit.appeafit.ui.components.OfflineBanner
import co.edu.eafit.appeafit.ui.home.HomeScreen
import co.edu.eafit.appeafit.ui.profile.ProfileScreen
import co.edu.eafit.appeafit.ui.services.ServicesScreen

@Composable
fun MainScreen(
    container: AppContainer,
    user: User,
    isOnline: Boolean,
    navController: NavHostController,
    onSignOut: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }

    Scaffold(
        bottomBar = { EafitBottomBar(selected = selectedTab, onSelect = { selectedTab = it }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (!isOnline) OfflineBanner()
            when (selectedTab) {
                MainTab.HOME -> HomeScreen(container = container, user = user, navController = navController)
                MainTab.SERVICES -> ServicesScreen(user = user, navController = navController)
                MainTab.CARNET -> CarnetScreen(user = user)
                MainTab.PROFILE -> ProfileScreen(
                    user = user,
                    navController = navController,
                    onSignOut = onSignOut
                )
            }
        }
    }
}
