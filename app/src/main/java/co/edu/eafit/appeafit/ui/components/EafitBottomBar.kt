package co.edu.eafit.appeafit.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import co.edu.eafit.appeafit.R

enum class MainTab(val route: String) {
    HOME("tab_home"),
    SERVICES("tab_services"),
    CARNET("tab_carnet"),
    PROFILE("tab_profile")
}

@Composable
fun EafitBottomBar(selected: MainTab, onSelect: (MainTab) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = selected == MainTab.HOME,
            onClick = { onSelect(MainTab.HOME) },
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text(androidx.compose.ui.res.stringResource(R.string.nav_inicio)) }
        )
        NavigationBarItem(
            selected = selected == MainTab.SERVICES,
            onClick = { onSelect(MainTab.SERVICES) },
            icon = { Icon(Icons.Filled.Apps, contentDescription = null) },
            label = { Text(androidx.compose.ui.res.stringResource(R.string.nav_servicios)) }
        )
        NavigationBarItem(
            selected = selected == MainTab.CARNET,
            onClick = { onSelect(MainTab.CARNET) },
            icon = { Icon(Icons.Filled.Badge, contentDescription = null) },
            label = { Text(androidx.compose.ui.res.stringResource(R.string.nav_carnet)) }
        )
        NavigationBarItem(
            selected = selected == MainTab.PROFILE,
            onClick = { onSelect(MainTab.PROFILE) },
            icon = { Icon(Icons.Filled.Person, contentDescription = null) },
            label = { Text(androidx.compose.ui.res.stringResource(R.string.nav_perfil)) }
        )
    }
}
