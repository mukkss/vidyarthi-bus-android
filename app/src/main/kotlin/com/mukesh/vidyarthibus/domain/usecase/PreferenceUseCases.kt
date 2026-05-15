package com.mukesh.vidyarthibus.domain.usecase

import com.mukesh.vidyarthibus.domain.repository.PreferenceRepository
import javax.inject.Inject

class OnboardingUseCases @Inject constructor(
    private val repository: PreferenceRepository
) {
    fun isOnboardingCompleted() = repository.isOnboardingCompleted()
    suspend fun setOnboardingCompleted(completed: Boolean) = repository.setOnboardingCompleted(completed)
}

class PreferenceUseCases @Inject constructor(
    private val repository: PreferenceRepository
) {
    fun getLastSelectedRouteId() = repository.getLastSelectedRouteId()
    suspend fun saveLastSelectedRouteId(routeId: String) = repository.saveLastSelectedRouteId(routeId)
}
