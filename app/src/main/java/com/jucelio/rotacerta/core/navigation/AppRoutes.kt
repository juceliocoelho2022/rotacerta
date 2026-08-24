package com.jucelio.rotacerta.core.navigation

/**
 * Rotas centralizadas da aplicação.
 *
 * Mantê-las fora das telas evita strings duplicadas e facilita a evolução
 * para navegação tipada nas próximas versões do Navigation Compose.
 */
object AppRoutes {
    const val SPLASH = "splash"
    const val DELIVERY_GRAPH = "delivery_graph"
    const val HOME = "delivery_home"
    const val ROUTE = "delivery_route"
    const val HISTORY = "delivery_history"
    const val SCANNER = "delivery_scanner"
}
