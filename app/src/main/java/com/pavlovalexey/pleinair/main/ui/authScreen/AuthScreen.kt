package com.pavlovalexey.pleinair.main.ui.authScreen

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.android.gms.common.SignInButton
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.utils.uiComponents.BackgroundImage
import com.pavlovalexey.pleinair.utils.uiComponents.CustomButtonOne

@Composable
fun AuthScreen(
    navController: NavHostController,
    onAuthSuccess: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current as Activity
    val authState by viewModel.authState.collectAsState()

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleSignInResult(result.data)
        }
    }

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        BackgroundImage(imageResId = R.drawable.back_lay)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AndroidView(
                factory = { context ->
                    SignInButton(context).apply {
                        setSize(SignInButton.SIZE_WIDE)
                        setOnClickListener {
                            viewModel.signInWithGoogle(googleSignInLauncher)
                        }
                    }
                },
                modifier = Modifier.wrapContentSize()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            CustomButtonOne(
                onClick = onCancel,
                text = stringResource(R.string.exit),
                iconResId = R.drawable.door_open_30dp,
                textColor = Color.Red,
                iconColor = Color.Red
            )
        }
    }
}
