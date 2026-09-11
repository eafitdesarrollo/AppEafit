package co.edu.eafit.appeafit.ui.carnet

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.edu.eafit.appeafit.R
import co.edu.eafit.appeafit.domain.model.User
import co.edu.eafit.appeafit.ui.components.RoleBadge
import co.edu.eafit.appeafit.ui.theme.EafitNavy
import coil.compose.AsyncImage

@Composable
fun CarnetScreen(user: User) {
    val context = LocalContext.current
    val qrBitmap = remember(user.uid) {
        QrCodeGenerator.generate("EAFIT-ID:${user.uid}:${user.institutionalId}")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EafitNavy)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.foundation.layout.Spacer(Modifier.size(24.dp))

        if (user.photoUrl.isNotBlank()) {
            AsyncImage(
                model = user.photoUrl,
                contentDescription = null,
                modifier = Modifier.size(96.dp).background(Color.White, CircleShape)
            )
        } else {
            Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.15f), modifier = Modifier.size(96.dp)) {
                androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        user.fullName.take(1).ifBlank { "E" },
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }
        }

        androidx.compose.foundation.layout.Spacer(Modifier.size(16.dp))
        Text(user.fullName.ifBlank { user.email }, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        androidx.compose.foundation.layout.Spacer(Modifier.size(6.dp))
        Text(user.institutionalId, color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodyMedium)
        androidx.compose.foundation.layout.Spacer(Modifier.size(10.dp))
        RoleBadge(role = user.role)

        androidx.compose.foundation.layout.Spacer(Modifier.size(28.dp))
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "QR carnet",
                    modifier = Modifier.size(200.dp)
                )
            }
        }

        androidx.compose.foundation.layout.Spacer(Modifier.size(24.dp))
        Button(
            onClick = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "${user.fullName} · ${user.role.label} · ${user.institutionalId} · EAFIT")
                }
                context.startActivity(Intent.createChooser(shareIntent, null))
            },
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(stringResource(R.string.carnet_share))
        }
    }
}
