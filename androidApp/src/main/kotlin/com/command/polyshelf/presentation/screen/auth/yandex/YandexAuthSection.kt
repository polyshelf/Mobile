package com.command.polyshelf.presentation.screen.auth.yandex

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthResult
import com.yandex.authsdk.YandexAuthSdk
import org.koin.androidx.compose.koinViewModel

@Composable
fun YandexAuthSection(
    buttonText: String,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (LocalInspectionMode.current) {
        OutlinedButton(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp),
            onClick = {}
        ) { Text(buttonText) }
    } else {
        YandexAuthSectionContent(buttonText, onSuccess, modifier)
    }
}

@Composable
private fun YandexAuthSectionContent(
    buttonText: String,
    onSuccess: () -> Unit,
    modifier: Modifier,
    viewModel: YandexAuthViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val sdk = remember(context) { YandexAuthSdk.create(YandexAuthOptions(context)) }
    val launcher = rememberLauncherForActivityResult(sdk.contract) { result ->
        when (result) {
            is YandexAuthResult.Success -> viewModel.onIntent(YandexAuthIntent.TokenReceived(result.token.value))
            is YandexAuthResult.Failure -> viewModel.onIntent(YandexAuthIntent.SdkFailed)
            YandexAuthResult.Cancelled -> Unit
        }
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onSuccess()
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = "или",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = !state.isLoading,
        onClick = { launcher.launch(YandexAuthLoginOptions()) }
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            Text(
                text = "Я",
                color = Color(0xFFFC3F1D),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = buttonText, style = MaterialTheme.typography.titleMedium)
        }
    }

    if (state.errorMessage != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = state.errorMessage.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }

    state.link?.let { link ->
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(YandexAuthIntent.LinkDismissed) },
            title = { Text("Привязать Яндекс?") },
            text = {
                Column {
                    Text(link.message)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = link.password,
                        onValueChange = { viewModel.onIntent(YandexAuthIntent.LinkPasswordChanged(it)) },
                        label = { Text("Пароль от аккаунта") },
                        singleLine = true,
                        isError = link.error != null,
                        supportingText = link.error?.let { { Text(it) } },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = link.password.isNotEmpty() && !state.isLoading,
                    onClick = { viewModel.onIntent(YandexAuthIntent.LinkConfirmed) }
                ) { Text("Привязать") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(YandexAuthIntent.LinkDismissed) }) {
                    Text("Отмена")
                }
            }
        )
    }
}
