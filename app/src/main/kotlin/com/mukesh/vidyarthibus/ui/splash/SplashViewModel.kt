package com.mukesh.vidyarthibus.ui.splash

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mukesh.vidyarthibus.domain.usecase.OnboardingUseCases
import com.mukesh.vidyarthibus.domain.usecase.PreferenceUseCases
import com.mukesh.vidyarthibus.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val onboardingUseCases: OnboardingUseCases,
    private val preferenceUseCases: PreferenceUseCases,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        checkNavigation()
    }

    private fun checkNavigation() {
        viewModelScope.launch {
            try {
                Timber.d("Checking onboarding status...")
                val isOnboardingCompleted = withTimeoutOrNull(2000) {
                    onboardingUseCases.isOnboardingCompleted().first()
                } ?: false
                
                Timber.d("Onboarding completed: $isOnboardingCompleted")

                if (!isOnboardingCompleted) {
                    _navigationEvent.emit(Screen.Onboarding.route)
                    return@launch
                }

                // Authenticate anonymously
                try {
                    if (auth.currentUser == null) {
                        Timber.d("Authenticating anonymously...")
                        withTimeoutOrNull(5000) {
                            auth.signInAnonymously().await()
                        }
                        Timber.d("Anonymous auth status: ${auth.currentUser?.uid}")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Auth failed or timed out")
                }

                val lastRouteId = withTimeoutOrNull(2000) {
                    preferenceUseCases.getLastSelectedRouteId().first()
                }
                
                if (lastRouteId != null) {
                    _navigationEvent.emit(Screen.LiveStatus.createRoute(lastRouteId))
                } else {
                    _navigationEvent.emit(Screen.RouteSelection.route)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error during splash navigation")
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                _navigationEvent.emit(Screen.RouteSelection.route)
            }
        }
    }
}
