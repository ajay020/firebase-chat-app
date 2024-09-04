package com.example.chatapp.ui.screens

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.chatapp.R
import com.example.chatapp.viewModel.AuthState
import com.example.chatapp.viewModel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlin.math.sign

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: () -> Unit,
    onAuthFailure: (exception: Exception) -> Unit = {}
) {
    val authState by authViewModel.authState.collectAsState()
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onAuthSuccess()
        }
    }

    val context = LocalContext.current
    // Configure Google Sign-In
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    // Launcher to handle the result of Google Sign-In Intent
    val googleSignInLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                authViewModel.firebaseAuthWithGoogle(
                    account.idToken!!,
                    onAuthSuccess,
                    onAuthFailure
                )
            } catch (e: ApiException) {
                onAuthFailure(e)
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
        signInWithGoogle = {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        },
        onAuthSuccess = onAuthSuccess
    )
}

@Composable
fun AuthScreenContent(
    authState: AuthState,
    onSignInClick: (String, String) -> Unit,
    onSignUpClick: (String, String) -> Unit,
    signInWithGoogle: () -> Unit,
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
        GoogleSignInButton(onClick = signInWithGoogle)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Or",
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
        )
        Spacer(modifier = Modifier.height(16.dp))
        EmailField(email = email, onEmailChange = { email = it })
        Spacer(modifier = Modifier.height(8.dp))
        PasswordField(password = password, onPasswordChange = { password = it })
        Spacer(modifier = Modifier.height(16.dp))
        AuthButton(
            isEnabled = email.isNotEmpty() && password.isNotEmpty(),
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
fun AuthButton(isEnabled: Boolean, isLogin: Boolean, onClick: () -> Unit) {
    Button(
        enabled = isEnabled,
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

@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(Color.White),
        modifier = Modifier
            .fillMaxWidth()

            .height(50.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_google),
            contentDescription = "Google Logo",
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Sign in with Google", color = Color.Black)
    }
}


@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    AuthScreenContent(
        authState = AuthState.Loading,
        onSignInClick = { _, _ -> },
        onSignUpClick = { _, _ -> },
        onAuthSuccess = {},
        signInWithGoogle = {}
    )
}