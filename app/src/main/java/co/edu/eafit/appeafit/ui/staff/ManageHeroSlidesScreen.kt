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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ViewCarousel
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.edu.eafit.appeafit.R
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.domain.model.HeroSlide
import co.edu.eafit.appeafit.ui.components.EafitCard
import co.edu.eafit.appeafit.ui.components.EmptyState
import co.edu.eafit.appeafit.ui.components.ImageSizeHint
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

/**
 * Pantalla de Admin/Staff para administrar los slides del hero del sitio web
 * (`appeafit-web-frontend`) y, a futuro, de la app de escritorio -- misma colección
 * Firestore `hero_slides` que ya lee el sitio (lectura pública, escritura
 * Staff/Admin, ver firestore.rules). Mismo patrón de UI que
 * [ManageAnnouncementsScreen], con dos diferencias explícitas del hero:
 * soporta imagen O video, y permite editar un slide existente (no solo crear/borrar).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageHeroSlidesScreen(container: AppContainer, onBack: () -> Unit) {
    var slides by remember { mutableStateOf<List<HeroSlide>>(emptyList()) }
    var editingSlide by remember { mutableStateOf<HeroSlide?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun reload() {
        scope.launch {
            container.heroSlideRepository.fetchAll().onSuccess { slides = it }
        }
    }

    LaunchedEffect(Unit) { reload() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.service_manage_hero)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingSlide = null
                showDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.manage_hero_new))
            }
        }
    ) { padding ->
        if (slides.isEmpty()) {
            EmptyState(
                message = stringResource(R.string.manage_hero_empty),
                icon = Icons.Filled.ViewCarousel,
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(slides, key = { _, item -> item.id }) { index, item ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(250, delayMillis = (index % 12) * 30)) +
                            slideInVertically(tween(250, delayMillis = (index % 12) * 30)) { it / 5 }
                    ) {
                        EafitCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editingSlide = item
                                    showDialog = true
                                }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when {
                                        item.type == "video" -> Icon(
                                            Icons.Filled.Videocam,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        item.url.isNotBlank() -> AsyncImage(
                                            model = item.url,
                                            contentDescription = item.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        else -> Icon(
                                            Icons.Filled.Image,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(Modifier.size(12.dp))
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        item.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "${stringResource(R.string.manage_hero_order_label)}: ${item.order}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(onClick = {
                                    editingSlide = item
                                    showDialog = true
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = null)
                                }
                                IconButton(onClick = {
                                    scope.launch {
                                        container.heroSlideRepository.delete(item)
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
        val context = LocalContext.current
        val editing = editingSlide
        var title by remember { mutableStateOf(editing?.title.orEmpty()) }
        var subtitle by remember { mutableStateOf(editing?.subtitle.orEmpty()) }
        var ctaLabel by remember { mutableStateOf(editing?.ctaLabel.orEmpty()) }
        var ctaHref by remember { mutableStateOf(editing?.ctaHref.orEmpty()) }
        var order by remember { mutableStateOf((editing?.order ?: (slides.size + 1)).toString()) }
        var mediaUri by remember { mutableStateOf<Uri?>(null) }
        var mediaIsVideo by remember { mutableStateOf(editing?.type == "video") }
        var isSaving by remember { mutableStateOf(false) }

        val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                mediaUri = uri
                mediaIsVideo = context.contentResolver.getType(uri).orEmpty().startsWith("video/")
            }
        }

        AlertDialog(
            onDismissRequest = { if (!isSaving) showDialog = false },
            title = {
                Text(stringResource(if (editing == null) R.string.manage_hero_new else R.string.manage_hero_edit))
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(enabled = !isSaving) {
                                pickMedia.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            mediaUri != null && mediaIsVideo -> Icon(
                                Icons.Filled.Videocam,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            mediaUri != null -> AsyncImage(
                                model = mediaUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                            )
                            editing != null && editing.type == "video" -> Icon(
                                Icons.Filled.Videocam,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            editing != null && editing.url.isNotBlank() -> AsyncImage(
                                model = editing.url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                            )
                            else -> Icon(
                                Icons.Filled.AddPhotoAlternate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.size(6.dp))
                    ImageSizeHint(text = stringResource(R.string.manage_hero_media_hint))
                    Spacer(Modifier.size(12.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(stringResource(R.string.broadcast_title_label)) },
                        singleLine = true,
                        enabled = !isSaving
                    )
                    OutlinedTextField(
                        value = subtitle,
                        onValueChange = { subtitle = it },
                        label = { Text(stringResource(R.string.manage_hero_subtitle)) },
                        enabled = !isSaving
                    )
                    OutlinedTextField(
                        value = ctaLabel,
                        onValueChange = { ctaLabel = it },
                        label = { Text(stringResource(R.string.manage_hero_cta_label)) },
                        singleLine = true,
                        enabled = !isSaving
                    )
                    OutlinedTextField(
                        value = ctaHref,
                        onValueChange = { ctaHref = it },
                        label = { Text(stringResource(R.string.manage_hero_cta_href)) },
                        singleLine = true,
                        enabled = !isSaving
                    )
                    OutlinedTextField(
                        value = order,
                        onValueChange = { input -> order = input.filter { it.isDigit() } },
                        label = { Text(stringResource(R.string.manage_hero_order_label)) },
                        singleLine = true,
                        enabled = !isSaving,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    TextButton(onClick = {
                        isSaving = true
                        scope.launch {
                            var url = editing?.url.orEmpty()
                            var mediaFileId = editing?.mediaFileId.orEmpty()
                            var type = if (mediaIsVideo) "video" else (editing?.type ?: "image")
                            val pickedUri = mediaUri
                            if (pickedUri != null) {
                                val extension = if (mediaIsVideo) "mp4" else "jpg"
                                container.imageKitClient.upload(
                                    uri = pickedUri,
                                    folder = "appeafit/hero_slides",
                                    fileName = "hero_${System.currentTimeMillis()}.$extension"
                                ).onSuccess { result ->
                                    // Se borra el archivo anterior DESPUÉS de que el nuevo ya
                                    // subió con éxito -- si la subida nueva fallara, el slide
                                    // se queda con el archivo viejo en vez de sin ninguno.
                                    val previousFileId = editing?.mediaFileId.orEmpty()
                                    if (previousFileId.isNotBlank() && previousFileId != result.fileId) {
                                        container.imageKitClient.delete(previousFileId)
                                    }
                                    url = result.url
                                    mediaFileId = result.fileId
                                    type = if (mediaIsVideo) "video" else "image"
                                }
                            }
                            val slide = HeroSlide(
                                id = editing?.id.orEmpty(),
                                type = type,
                                url = url,
                                mediaFileId = mediaFileId,
                                title = title,
                                subtitle = subtitle,
                                ctaLabel = ctaLabel,
                                ctaHref = ctaHref,
                                order = order.toIntOrNull() ?: 0
                            )
                            if (editing == null) {
                                container.heroSlideRepository.publish(slide)
                            } else {
                                container.heroSlideRepository.update(slide)
                            }
                            reload()
                            isSaving = false
                            showDialog = false
                        }
                    }) { Text(stringResource(R.string.common_save)) }
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!isSaving) showDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}
