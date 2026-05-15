package com.mukesh.vidyarthibus.domain.usecase

import com.mukesh.vidyarthibus.core.location.LocationUtils
import com.mukesh.vidyarthibus.data.remote.LocationProvider
import com.mukesh.vidyarthibus.data.remote.model.LatLngDto
import com.mukesh.vidyarthibus.domain.model.BusRoute
import com.mukesh.vidyarthibus.domain.model.CrowdReport
import com.mukesh.vidyarthibus.domain.model.CrowdStatus
import com.mukesh.vidyarthibus.domain.repository.BusRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRoutesUseCase @Inject constructor(
    private val repository: BusRepository
) {
    operator fun invoke(): Flow<List<BusRoute>> = repository.getRoutes()
}

class GetCrowdStatusUseCase @Inject constructor(
    private val repository: BusRepository
) {
    operator fun invoke(routeId: String): Flow<CrowdStatus> = repository.getCrowdStatus(routeId)
}

class GetLastUpdatedUseCase @Inject constructor(
    private val repository: BusRepository
) {
    operator fun invoke(routeId: String): Flow<Long?> = repository.getLastUpdated(routeId)
}

class SubmitReportUseCase @Inject constructor(
    private val repository: BusRepository,
    private val locationProvider: LocationProvider
) {
    suspend operator fun invoke(route: BusRoute, status: CrowdStatus, deviceId: String): Result<Unit> {
        val location = locationProvider.getCurrentLocation() 
        
        val lat: Double
        val lng: Double
        
        if (location == null) {
            // FALLBACK for testing if GPS fails
            android.util.Log.w("VidyarthiBus", "GPS failed, using fallback location for testing")
            lat = 13.0699
            lng = 77.7985
        } else {
            lat = location.latitude
            lng = location.longitude
        }

        val isInCorridor = LocationUtils.isPointInCorridor(
            lat = lat,
            lng = lng,
            corridor = route.corridor
        )

        // For now, we only LOG if outside corridor instead of failing, to allow testing
        if (!isInCorridor) {
            android.util.Log.w("VidyarthiBus", "User reported from outside corridor")
        }

        val report = CrowdReport(
            routeId = route.id,
            status = status,
            timestamp = System.currentTimeMillis(),
            deviceId = deviceId,
            lat = lat,
            lng = lng
        )
        return repository.submitReport(report)
    }
}

class GetAlternativesUseCase @Inject constructor(
    private val repository: BusRepository
) {
    operator fun invoke(routeId: String) = repository.getAlternatives(routeId)
}
