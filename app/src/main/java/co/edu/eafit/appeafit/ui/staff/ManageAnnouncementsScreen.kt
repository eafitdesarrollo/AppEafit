package co.edu.eafit.appeafit.ui.staff

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.edu.eafit.appeafit.R
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.domain.model.NewsItem
import co.edu.eafit.appeafit.ui.components.EafitCard
import co.edu.eafit.appeafit.ui.components.EmptyState
import co.edu.eafit.appeafit.ui.components.ImageSizeHint
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAnnouncementsScreen(container: AppContainer, authorId: String, onBack: () -> Unit) {
    var news by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun reload() {
        scope.launch { container.newsRepository.refresh() }
    }

    // Antes esta pantalla solo leía el caché local (Room) y nunca llamaba a refresh() al
    // abrirse — solo se refrescaba después de publicar/borrar. Si el caché estaba vacío
    // (instalación nueva, o cambios hechos desde otro dispositivo/la consola), la pantalla
    // se veía vacía aunque sí hubiera anuncios en Firestore.
    LaunchedEffect(Unit) {
        reload()
        container.newsRepository.observeCached().collect { news = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.service_manage_announcements)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.manage_announcements_new)) }
        }
    ) { padding ->
        if (news.isEmpty()) {
            EmptyState(message = stringResource(R.string.manage_announcements_empty), icon = Icons.Filled.Campaign, modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(news, key = { _, item -> item.id }) { index, item ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(250, delayMillis = (index % 12) * 30)) +
                            slideInVertically(tween(250, delayMillis = (index % 12) * 30)) { it / 5 }
                    ) {
                        EafitCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (item.imageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = item.imageUrl,
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                    androidx.compose.foundation.layout.Spacer(Modifier.size(12.dp))
                                }
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        item.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        item.category,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(onClick = {
                                    scope.launch {
                                        container.newsRepository.delete(item)
                                        reload()
                                    }
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("") }
        var body by remember { mutableStateOf("") }
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        var isSaving by remember { mutableStateOf(false) }

        val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) imageUri = uri
        }

        AlertDialog(
            onDismissRequest = { if (!isSaving) showDialog = false },
            title = { Text(stringResource(R.string.manage_announcements_new)) },
            text = {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(enabled = !isSaving) {
                                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                            )
                        } else {
                            Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    androidx.compose.foundation.layout.Spacer(Modifier.size(6.dp))
                    ImageSizeHint(text = stringResource(R.string.image_hint_news))
                    androidx.compose.foundation.layout.Spacer(Modifier.size(12.dp))
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.broadcast_title_label)) }, singleLine = true, enabled = !isSaving)
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text(stringResource(R.string.manage_announcements_category)) }, singleLine = true, enabled = !isSaving)
                    OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text(stringResource(R.string.manage_announcements_content)) }, enabled = !isSaving)
                }
            },
            confirmButton = {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    TextButton(onClick = {
                        isSaving = true
                        scope.launch {
                            var imageUrl = ""
                            var imageFileId = ""
                            val pickedUri = imageUri
                            if (pickedUri != null) {
                                container.imageKitClient.upload(
                                    uri = pickedUri,
                                    folder = "appeafit/news",
                                    fileName = "news_${System.currentTimeMillis()}.jpg"
                                ).onSuccess {
                                    imageUrl = it.url
                                    imageFileId = it.fileId
                                }
                            }
                            container.newsRepository.publish(
                                NewsItem(
                                    title = title,
                                    category = category,
                                    body = body,
                                    imageUrl = imageUrl,
                                    imageFileId = imageFileId,
                                    authorId = authorId,
                                    publishedAt = System.currentTimeMillis()
                                )
                            )
                            reload()
                            isSaving = false
                            showDialog = false
                        }
                    }) { Text(stringResource(R.string.common_save)) }
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!isSaving) showDialog = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }
}
