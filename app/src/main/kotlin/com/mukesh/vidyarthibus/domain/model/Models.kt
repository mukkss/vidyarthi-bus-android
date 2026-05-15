package com.mukesh.vidyarthibus.domain.model

data class BusRoute(
    val id: String,
    val name: String,
    val stops: List<String>,
    val corridor: List<LatLng>
)

data class LatLng(
    val lat: Double,
    val lng: Double
)

enum class CrowdStatus {
    UNKNOWN,
    EMPTY,
    SEATED,
    FULL
}

data class CrowdReport(
    val routeId: String,
    val status: CrowdStatus,
    val timestamp: Long,
    val deviceId: String,
    val lat: Double,
    val lng: Double
)

data class AlternativeContact(
    val name: String,
    val phone: String
)
