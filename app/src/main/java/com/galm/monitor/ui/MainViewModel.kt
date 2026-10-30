package com.galm.monitor.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.galm.monitor.GalmApplication
import com.galm.monitor.auth.GoogleAuthManager
import com.galm.monitor.data.EventRepository
import com.galm.monitor.data.SecurityEvent
import com.galm.monitor.util.NotificationHelper
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiState(
    val isSignedIn: Boolean = false,
    val accountEmail: String? = null,
    val accountName: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = ""
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as GalmApplication
    private val authManager = GoogleAuthManager(application)
    private val repository = EventRepository(app.database.securityEventDao(), authManager)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val events: StateFlow<List<SecurityEvent>> = repository.getAllEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        checkCurrentSignIn()
        viewModelScope.launch {
            repository.ensureLimitationNoteExists()
        }
    }

    private fun checkCurrentSignIn() {
        val account = authManager.getLastSignedInAccount()
        _uiState.value = _uiState.value.copy(
            isSignedIn = account != null,
            accountEmail = account?.email,
            accountName = account?.displayName
        )
    }

    fun getSignInIntent() = authManager.getSignInIntent()

    fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authManager.handleSignInResult(task)
            result.fold(
                onSuccess = { account ->
                    _uiState.value = _uiState.value.copy(
                        isSignedIn = true,
                        accountEmail = account.email,
                        accountName = account.displayName,
                        isLoading = false
                    )
                    repository.recordAppSignIn(account.email)
                    // Optional: notify user that sign-in was recorded locally
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Anmeldung fehlgeschlagen: ${e.message}"
                    )
                }
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authManager.signOut()
            _uiState.value = UiState()
        }
    }

    fun addManualEvent(
        description: String,
        deviceName: String? = null,
        ipAddress: String? = null,
        location: String? = null,
        isNewDevice: Boolean = false
    ) {
        viewModelScope.launch {
            repository.addManualEvent(description, deviceName, ipAddress, location, isNewDevice)
            // Show notification for newly added event
            val event = SecurityEvent(
                timestamp = System.currentTimeMillis(),
                eventType = "MANUAL",
                deviceName = deviceName,
                ipAddress = ipAddress,
                location = location,
                description = description,
                isNewDevice = isNewDevice,
                source = "MANUAL"
            )
            NotificationHelper.showNewEventNotification(getApplication(), event)
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
