package com.jucelio.rotacerta.ui.delivery

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.jucelio.rotacerta.domain.model.delivery.Delivery

/**
 * Abre o endereço da entrega no aplicativo de mapas do dispositivo,
 * com fallback para o Google Maps na web.
 */
internal fun openInMaps(
    context: Context,
    delivery: Delivery
) {
    val query = Uri.encode(delivery.fullAddress)

    val geoIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("geo:0,0?q=$query")
    )

    val webIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://www.google.com/maps/search/?api=1&query=$query")
    )

    runCatching {
        context.startActivity(geoIntent)
    }.onFailure {
        runCatching { context.startActivity(webIntent) }
    }
}
