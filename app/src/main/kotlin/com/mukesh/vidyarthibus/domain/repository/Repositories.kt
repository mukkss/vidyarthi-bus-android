package com.mukesh.vidyarthibus.domain.repository

import com.mukesh.vidyarthibus.domain.model.AlternativeContact
import com.mukesh.vidyarthibus.domain.model.BusRoute
import com.mukesh.vidyarthibus.domain.model.CrowdReport
import com.mukesh.vidyarthibus.domain.model.CrowdStatus
import kotlinx.coroutines.flow.Flow

interface BusRepository {
    fun getRoutes(): Flow<List<BusRoute>>
    fun getCrowdStatus(routeId: String): Flow<CrowdStatus>
    fun getLastUpdated(routeId: String): Flow<Long?>
    suspend fun submitReport(report: CrowdReport): Result<Unit>
    fun getAlternatives(routeId: String): Flow<List<AlternativeContact>>
}

interface PreferenceRepository {
    fun getLastSelectedRouteId(): Flow<String?>
    suspend fun saveLastSelectedRouteId(routeId: String)
    fun isOnboardingCompleted(): Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)
}
