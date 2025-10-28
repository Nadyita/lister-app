package xyz.travitia.lister.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import xyz.travitia.lister.R
import xyz.travitia.lister.data.model.PrimaryColor
import xyz.travitia.lister.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCategories: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var baseUrl by remember { mutableStateOf(uiState.baseUrl) }
    var bearerToken by remember { mutableStateOf(uiState.bearerToken) }
    var suggestionCount by remember { mutableStateOf(uiState.suggestionCount.toString()) }
    var selectedColor by remember { mutableStateOf(uiState.primaryColor) }
    var useMaterialYou by remember { mutableStateOf(uiState.useMaterialYou) }

    val isMaterialYouAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    LaunchedEffect(uiState.baseUrl, uiState.bearerToken, uiState.suggestionCount, uiState.primaryColor,
        uiState.useMaterialYou) {
        baseUrl = uiState.baseUrl
        bearerToken = uiState.bearerToken
        suggestionCount = uiState.suggestionCount.toString()
        selectedColor = uiState.primaryColor
        useMaterialYou = uiState.useMaterialYou
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_api_section_title),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.settings_base_url_label),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.settings_base_url_placeholder)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.settings_bearer_token_label),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = bearerToken,
                onValueChange = { bearerToken = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.settings_bearer_token_placeholder)) },
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.settings_app_section_title),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.settings_suggestion_count_label),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = suggestionCount,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.toIntOrNull()?.let { it >= 0 && it <= 100 } == true) {
                        suggestionCount = newValue
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.settings_suggestion_count_placeholder)) },
                supportingText = { Text(stringResource(R.string.settings_suggestion_count_hint)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onNavigateToCategories,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_manage_categories))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.settings_appearance_section_title),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isMaterialYouAvailable) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_use_material_you),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(R.string.settings_use_material_you_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = useMaterialYou,
                        onCheckedChange = { useMaterialYou = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = stringResource(R.string.settings_primary_color_label),
                style = MaterialTheme.typography.bodyLarge,
                color = if (isMaterialYouAvailable && useMaterialYou) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ColorSelectionGrid(
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it },
                enabled = !isMaterialYouAvailable || !useMaterialYou
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val count = suggestionCount.toIntOrNull() ?: 3
                    viewModel.saveSettings(baseUrl, bearerToken, count, selectedColor, useMaterialYou) {
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.button_save))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.settings_base_url_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.settings_base_url_example),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.settings_bearer_token_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ColorSelectionGrid(
    selectedColor: PrimaryColor,
    onColorSelected: (PrimaryColor) -> Unit,
    enabled: Boolean = true
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val colors = PrimaryColor.entries
        val chunkedColors = colors.chunked(4)

        chunkedColors.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { color ->
                    ColorOption(
                        color = color,
                        isSelected = color == selectedColor,
                        onSelect = { if (enabled) onColorSelected(color) },
                        modifier = Modifier.weight(1f),
                        enabled = enabled
                    )
                }
                repeat(4 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ColorOption(
    color: PrimaryColor,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier
            .clickable(enabled = enabled, onClick = onSelect)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.toColor().copy(alpha = if (enabled) 1f else 0.38f))
                .then(
                    if (isSelected) {
                        Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    } else {
                        Modifier.border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
                    }
                )
        )

        Text(
            text = stringResource(color.nameResId),
            style = MaterialTheme.typography.bodySmall,
            color = if (enabled) {
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            }
        )
    }
}
