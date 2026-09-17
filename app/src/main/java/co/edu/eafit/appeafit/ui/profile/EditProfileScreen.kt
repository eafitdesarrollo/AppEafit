package co.edu.eafit.appeafit.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.eafit.appeafit.R
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.core.di.GenericViewModelFactory
import co.edu.eafit.appeafit.domain.model.User
import co.edu.eafit.appeafit.ui.components.EafitButton
import co.edu.eafit.appeafit.ui.theme.pressScale
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(container: AppContainer, user: User, onBack: () -> Unit) {
    val viewModel: ProfileViewModel = viewModel(factory = GenericViewModelFactory { ProfileViewModel(container) })
    val state by viewModel.editState.collectAsStateWithLifecycle()

    var fullName by remember { mutableStateOf(user.fullName) }
    var program by remember { mutableStateOf(user.program) }
    var photoUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) photoUri = uri
    }

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_edit_data)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
            val avatarInteractionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.CenterHorizontally)
                    .pressScale(avatarInteractionSource)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))
                    )
                    .clickable(interactionSource = avatarInteractionSource, indication = null) {
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    AsyncImage(model = photoUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else if (user.photoUrl.isNotBlank()) {
                    AsyncImage(model = user.photoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
            androidx.compose.foundation.layout.Spacer(Modifier.height(10.dp))
            co.edu.eafit.appeafit.ui.components.ImageSizeHint(
                text = stringResource(R.string.image_hint_profile),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(horizontal = 24.dp)
            )

            androidx.compose.foundation.layout.Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text(stringResource(R.string.auth_full_name_label)) },
                shape = MaterialTheme.shapes.small,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = program,
                onValueChange = { program = it },
                label = { Text(stringResource(R.string.auth_program_label)) },
                shape = MaterialTheme.shapes.small,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (state.errorMessage != null) {
                androidx.compose.foundation.layout.Spacer(Modifier.height(10.dp))
                Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            androidx.compose.foundation.layout.Spacer(Modifier.height(24.dp))
            EafitButton(
                text = stringResource(R.string.common_save),
                onClick = { viewModel.updateProfile(user.uid, fullName, program, photoUri) },
                enabled = !state.isSaving,
                isLoading = state.isSaving,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
