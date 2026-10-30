package com.galm.monitor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.galm.monitor.ui.MainScreen
import com.galm.monitor.ui.MainViewModel
import com.galm.monitor.ui.theme.GalmTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            viewModel.handleSignInResult(task)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GalmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val uiState by viewModel.uiState.collectAsState()
                    val events by viewModel.events.collectAsState()

                    MainScreen(
                        uiState = uiState,
                        events = events,
                        onSignInClick = {
                            signInLauncher.launch(viewModel.getSignInIntent())
                        },
                        onSignOutClick = { viewModel.signOut() },
                        onAddManualEvent = { desc, device, ip, loc, isNew ->
                            viewModel.addManualEvent(desc, device, ip, loc, isNew)
                        },
                        onOpenGoogleSecurity = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://myaccount.google.com/security")
                            )
                            startActivity(intent)
                        },
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                        onClearError = { viewModel.clearError() }
                    )
                }
            }
        }
    }
}
