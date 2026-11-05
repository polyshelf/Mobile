package com.command.polyshelf.presentation.screen.auth.registration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.command.polyshelf.presentation.components.button.PrimaryActionButton
import com.command.polyshelf.presentation.components.header.AuthHeader
import com.command.polyshelf.presentation.components.input.OutlinedPasswordField
import com.command.polyshelf.presentation.screen.auth.yandex.YandexAuthSection
import com.command.polyshelf.presentation.theme.PolyShelfTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegistrationScreen(
    modifier: Modifier = Modifier,
    viewModel: RegistrationViewModel = koinViewModel(),
    onLoginClick: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onRegisterSuccess()
        }
    }

    RegistrationContent(
        state = state,
        onIntent = viewModel::onIntent,
        onLoginClick = onLoginClick,
        onYandexSuccess = onRegisterSuccess,
        modifier = modifier
    )
}

@Composable
fun RegistrationContent(
    state: RegistrationState,
    onIntent: (RegistrationIntent) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    onYandexSuccess: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .widthIn(max = 440.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AuthHeader(
                            title = "Регистрация",
                            subtitle = "Создайте новый аккаунт PolyShelf"
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Email
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = state.email,
                            onValueChange = { onIntent(RegistrationIntent.EmailChanged(it)) },
                            label = { Text("Email") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email"
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Password
                        OutlinedPasswordField(
                            value = state.password,
                            onValueChange = { onIntent(RegistrationIntent.PasswordChanged(it)) },
                            label = "Пароль",
                            imeAction = ImeAction.Next
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Confirm Password
                        OutlinedPasswordField(
                            value = state.confirmPassword,
                            onValueChange = { onIntent(RegistrationIntent.ConfirmPasswordChanged(it)) },
                            label = "Повторите пароль",
                            imeAction = ImeAction.Done
                        )

                        if (state.errorMessage != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = state.errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Submit Button
                        PrimaryActionButton(
                            text = "Зарегистрироваться",
                            isLoading = state.isLoading,
                            onClick = { onIntent(RegistrationIntent.RegisterClicked) }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        YandexAuthSection(
                            buttonText = "Зарегистрироваться через Яндекс",
                            onSuccess = onYandexSuccess
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Уже есть аккаунт?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = onLoginClick) {
                                Text(
                                    text = "Войти",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Phone", device = Devices.PHONE, showBackground = true)
@Preview(name = "Tablet", device = Devices.TABLET, showBackground = true)
@Composable
private fun RegistrationContentPreview() {
    PolyShelfTheme {
        RegistrationContent(
            state = RegistrationState(email = "newuser@polyshelf.com"),
            onIntent = {},
            onLoginClick = {}
        )
    }
}
