package com.mukesh.vidyarthibus.data.remote.model

import com.mukesh.vidyarthibus.domain.model.AlternativeContact
import com.mukesh.vidyarthibus.domain.model.BusRoute
import com.mukesh.vidyarthibus.domain.model.CrowdReport
import com.mukesh.vidyarthibus.domain.model.CrowdStatus
import com.mukesh.vidyarthibus.domain.model.LatLng

data class BusRouteDto(
    val id: String = "",
    val name: String = "",
    val stops: List<String> = emptyList(),
    val corridor: List<LatLngDto> = emptyList()
) {
    fun toDomain() = BusRoute(
        id = id,
        name = name,
        stops = stops,
        corridor = corridor.map { it.toDomain() }
    )
}

data class LatLngDto(
    val lat: Double = 0.0,
    val lng: Double = 0.0
) {
    fun toDomain() = LatLng(lat, lng)
}

data class CrowdReportDto(
    val status: String = "UNKNOWN",
    val timestamp: Long = 0,
    val deviceId: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0
) {
    fun toDomain(routeId: String) = CrowdReport(
        routeId = routeId,
        status = try { CrowdStatus.valueOf(status.uppercase()) } catch (e: Exception) { CrowdStatus.UNKNOWN },
        timestamp = timestamp,
        deviceId = deviceId,
        lat = lat,
        lng = lng
    )

    companion object {
        fun fromDomain(report: CrowdReport) = CrowdReportDto(
            status = report.status.name,
            timestamp = report.timestamp,
            deviceId = report.deviceId,
            lat = report.lat,
            lng = report.lng
        )
    }
}

data class AlternativeContactDto(
    val name: String = "",
    val phone: String = ""
) {
    fun toDomain() = AlternativeContact(name, phone)
}
