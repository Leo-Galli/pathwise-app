package it.leogalli.pathwise.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import it.leogalli.pathwise.R
import it.leogalli.pathwise.ui.evaluation.EvaluationScreen
import it.leogalli.pathwise.ui.history.TracksScreen
import it.leogalli.pathwise.ui.map.MapScreen

/** Destinazioni della bottom bar. */
private data class Destination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
) {
    companion object {
        val MAP = Destination("map", R.string.nav_mappa, Icons.Filled.Map)
        val EVALUATION = Destination("evaluation", R.string.nav_valutazione, Icons.Filled.BarChart)
        val HISTORY = Destination("history", R.string.nav_storico, Icons.Filled.History)

        val ALL = listOf(MAP, EVALUATION, HISTORY)
    }
}

/**
 * NavHost principale con bottom bar a tre destinazioni.
 * Scaffold + Navigation Compose: ogni tab mantiene il proprio stato
 * (inclusa la mappa aperta) quando si naviga tra le schede.
 */
@Composable
fun PathWiseNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface) {
                Destination.ALL.forEach { destination ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == destination.route
                    } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                // Pop-up behavior standard: mantieni lo start destination
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = null) },
                        label = { Text(stringResource(destination.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.MAP.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Destination.MAP.route) { MapScreen() }
            composable(Destination.EVALUATION.route) { EvaluationScreen() }
            composable(Destination.HISTORY.route) { TracksScreen() }
        }
    }
}
