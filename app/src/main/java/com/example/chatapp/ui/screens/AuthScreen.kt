package com.example.chatapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.chatapp.viewModel.AuthState
import com.example.chatapp.viewModel.AuthViewModel

@Composable
fun AuthScreen(authViewModel: AuthViewModel = hiltViewModel(), onAuthSuccess: () -> Unit) {
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onAuthSuccess()
        }
    }

    AuthScreenContent(
        authState = authState,
        onSignInClick = { email, password ->
            authViewModel.signInWithEmailPassword(email, password)
        },
        onSignUpClick = { email, password ->
            authViewModel.signUpWithEmailPassword(email, password)
        },
        onAuthSuccess = onAuthSuccess
    )
}

@Composable
fun AuthScreenContent(
    authState: AuthState,
    onSignInClick: (String, String) -> Unit,
    onSignUpClick: (String, String) -> Unit,
    onAuthSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }

    when (authState) {
        is AuthState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier)
            }
        }

        is AuthState.Success -> {
            authState.user?.let {
                onAuthSuccess()
            }
        }

        is AuthState.Error -> {
            message = authState.message
        }

        else -> {}
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        EmailField(email = email, onEmailChange = { email = it })
        Spacer(modifier = Modifier.height(8.dp))
        PasswordField(password = password, onPasswordChange = { password = it })
        Spacer(modifier = Modifier.height(16.dp))
        AuthButton(
            isLogin = isLogin,
            onClick = {
                if (isLogin) {
                    onSignInClick(email, password)
                } else {
                    onSignUpClick(email, password)
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        ToggleAuthModeButton(isLogin = isLogin, onClick = { isLogin = !isLogin })
        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun EmailField(email: String, onEmailChange: (String) -> Unit) {
    TextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun PasswordField(password: String, onPasswordChange: (String) -> Unit) {
    TextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Password") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun AuthButton(isLogin: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(if (isLogin) "Sign In" else "Sign Up")
    }
}

@Composable
fun ToggleAuthModeButton(isLogin: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(if (isLogin) "Don't have an account? Sign Up" else "Already have an account? Sign In")
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    AuthScreenContent(
        authState = AuthState.Loading,
        onSignInClick = { _, _ -> },
        onSignUpClick = { _, _ -> },
        onAuthSuccess = {}
    )
}