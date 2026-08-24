package com.jucelio.rotacerta.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.jucelio.rotacerta.core.navigation.AppRoutes
import com.jucelio.rotacerta.presentation.splash.SplashRoute
import com.jucelio.rotacerta.ui.delivery.DeliveryHistoryScreen
import com.jucelio.rotacerta.ui.delivery.DeliveryHomeScreen
import com.jucelio.rotacerta.ui.delivery.DeliveryRouteScreen
import com.jucelio.rotacerta.ui.delivery.DeliveryScannerScreen
import com.jucelio.rotacerta.ui.delivery.DeliveryViewModel

/**
 * Grafo raiz de navegação do RotaCerta.
 *
 * A Splash é a entrada do app. O fluxo de entregas continua usando um único
 * [DeliveryViewModel] compartilhado no escopo do grafo de entregas, preservando
 * a rota enquanto o entregador navega entre Home, Scanner, Rota e Histórico.
 */
@Composable
fun RotaCertaApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.SPLASH
    ) {
        composable(AppRoutes.SPLASH) {
            SplashRoute(
                onFinished = {
                    navController.navigate(AppRoutes.DELIVERY_GRAPH) {
                        popUpTo(AppRoutes.SPLASH) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        navigation(
            route = AppRoutes.DELIVERY_GRAPH,
            startDestination = AppRoutes.HOME
        ) {
            composable(AppRoutes.HOME) { entry ->
                val viewModel = sharedViewModel(navController, entry)

                DeliveryHomeScreen(
                    state = viewModel.state,
                    onToggleOnline = viewModel::toggleOnline,
                    onScanClick = {
                        navController.navigate(AppRoutes.SCANNER) { launchSingleTop = true }
                    },
                    onRouteClick = {
                        navController.navigate(AppRoutes.ROUTE) { launchSingleTop = true }
                    },
                    onHistoryClick = {
                        navController.navigate(AppRoutes.HISTORY) { launchSingleTop = true }
                    },
                    onProfileClick = { /* Perfil do entregador: próxima sprint. */ }
                )
            }

            composable(AppRoutes.ROUTE) { entry ->
                val viewModel = sharedViewModel(navController, entry)

                DeliveryRouteScreen(
                    state = viewModel.state,
                    onBack = { navController.popBackStack() },
                    onScanClick = {
                        navController.navigate(AppRoutes.SCANNER) { launchSingleTop = true }
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

            composable(AppRoutes.HISTORY) { entry ->
                val viewModel = sharedViewModel(navController, entry)

                DeliveryHistoryScreen(
                    state = viewModel.state,
                    onScanClick = {
                        navController.navigate(AppRoutes.SCANNER) { launchSingleTop = true }
                    },
                    onHomeClick = { navController.popBackStack(AppRoutes.HOME, false) },
                    onRouteClick = {
                        navController.navigate(AppRoutes.ROUTE) { launchSingleTop = true }
                    },
                    onProfileClick = { /* Perfil do entregador: próxima sprint. */ }
                )
            }

            composable(AppRoutes.SCANNER) { entry ->
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
        navController.getBackStackEntry(AppRoutes.DELIVERY_GRAPH)
    }
    return hiltViewModel(graphEntry)
}
