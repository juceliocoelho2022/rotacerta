package com.jucelio.rotacerta.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.jucelio.rotacerta.ui.delivery.DeliveryHistoryScreen
import com.jucelio.rotacerta.ui.delivery.DeliveryHomeScreen
import com.jucelio.rotacerta.ui.delivery.DeliveryRouteScreen
import com.jucelio.rotacerta.ui.delivery.DeliveryScannerScreen
import com.jucelio.rotacerta.ui.delivery.DeliveryViewModel

private object Routes {
    const val GRAPH = "delivery_graph"
    const val HOME = "delivery_home"
    const val ROUTE = "delivery_route"
    const val HISTORY = "delivery_history"
    const val SCANNER = "delivery_scanner"
}

/**
 * App do entregador (RotaCerta).
 *
 * Todo o app compartilha um único [DeliveryViewModel] com escopo do
 * grafo, de modo que Início, Rota, Histórico e Scanner enxergam a
 * mesma rota mantida em memória.
 */
@Composable
fun RotaCertaApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.GRAPH
    ) {
        navigation(
            route = Routes.GRAPH,
            startDestination = Routes.HOME
        ) {

            composable(Routes.HOME) { entry ->
                val viewModel = sharedViewModel(navController, entry)

                DeliveryHomeScreen(
                    state = viewModel.state,
                    onToggleOnline = viewModel::toggleOnline,
                    onScanClick = {
                        navController.navigate(Routes.SCANNER) { launchSingleTop = true }
                    },
                    onRouteClick = {
                        navController.navigate(Routes.ROUTE) { launchSingleTop = true }
                    },
                    onHistoryClick = {
                        navController.navigate(Routes.HISTORY) { launchSingleTop = true }
                    },
                    onProfileClick = { /* Perfil do entregador (em breve). */ }
                )
            }

            composable(Routes.ROUTE) { entry ->
                val viewModel = sharedViewModel(navController, entry)

                DeliveryRouteScreen(
                    state = viewModel.state,
                    onBack = { navController.popBackStack() },
                    onScanClick = {
                        navController.navigate(Routes.SCANNER) { launchSingleTop = true }
                    },
                    onReorder = viewModel::reorderRoute,
                    onClear = viewModel::clearRoute,
                    onMarkDelivered = viewModel::markAsDelivered,
                    onMarkFailed = viewModel::markAsFailed,
                    onReopen = viewModel::reopen,
                    onRemove = viewModel::removeStop,
                    onMessageShown = viewModel::consumeMessage
                )
            }

            composable(Routes.HISTORY) { entry ->
                val viewModel = sharedViewModel(navController, entry)

                DeliveryHistoryScreen(
                    state = viewModel.state,
                    onScanClick = {
                        navController.navigate(Routes.SCANNER) { launchSingleTop = true }
                    },
                    onHomeClick = { navController.popBackStack(Routes.HOME, false) },
                    onRouteClick = {
                        navController.navigate(Routes.ROUTE) { launchSingleTop = true }
                    },
                    onProfileClick = { /* Perfil do entregador (em breve). */ }
                )
            }

            composable(Routes.SCANNER) { entry ->
                val viewModel = sharedViewModel(navController, entry)

                DeliveryScannerScreen(
                    onBack = { navController.popBackStack() },
                    onCodeRead = { code ->
                        viewModel.addFromScannedCode(code)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
private fun sharedViewModel(
    navController: NavHostController,
    entry: NavBackStackEntry
): DeliveryViewModel {
    val graphEntry = remember(entry) {
        navController.getBackStackEntry(Routes.GRAPH)
    }
    return hiltViewModel(graphEntry)
}
