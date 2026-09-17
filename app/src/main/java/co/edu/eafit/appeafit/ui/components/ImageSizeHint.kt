package co.edu.eafit.appeafit.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Aviso de tamaño recomendado para una imagen que el usuario está por subir (foto de
 * perfil, imagen de un anuncio, o cualquier otro lugar donde se suba una foto en el
 * futuro). Es solo una GUÍA -- no bloquea ni valida nada, el usuario puede subir
 * cualquier imagen igual; el objetivo es que sepa de antemano qué proporción se
 * adapta mejor a donde se va a mostrar, para minimizar recortes feos, sin exigirle
 * una medida exacta en píxeles.
 */
@Composable
fun ImageSizeHint(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = LocalContentColor.current.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 2.dp, end = 6.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = LocalContentColor.current.copy(alpha = 0.7f)
        )
    }
}
