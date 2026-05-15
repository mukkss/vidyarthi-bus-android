package com.mukesh.vidyarthibus.data.repository

import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mukesh.vidyarthibus.data.remote.model.AlternativeContactDto
import com.mukesh.vidyarthibus.data.remote.model.BusRouteDto
import com.mukesh.vidyarthibus.data.remote.model.CrowdReportDto
import com.mukesh.vidyarthibus.domain.model.AlternativeContact
import com.mukesh.vidyarthibus.domain.model.BusRoute
import com.mukesh.vidyarthibus.domain.model.CrowdReport
import com.mukesh.vidyarthibus.domain.model.CrowdStatus
import com.mukesh.vidyarthibus.domain.repository.BusRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BusRepositoryImpl @Inject constructor(
    private val db: FirebaseDatabase
) : BusRepository {

    override fun getRoutes(): Flow<List<BusRoute>> = callbackFlow {
        val ref = db.getReference("routes")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    trySend(emptyList())
                    return
                }
                val routes = snapshot.children.mapNotNull { it.getValue(BusRouteDto::class.java)?.toDomain() }
                trySend(routes)
            }
            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException(), "Firebase onCancelled")
                // Using a Handler to show Toast from background thread
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    Toast.makeText(db.app.applicationContext, "Firebase Error: ${error.message}", Toast.LENGTH_LONG).show()
                }
                trySend(emptyList())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override fun getCrowdStatus(routeId: String): Flow<CrowdStatus> = callbackFlow {
        val ref = db.getReference("crowd_reports").child(routeId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    trySend(CrowdStatus.UNKNOWN)
                    return
                }
                // Get the most recent report
                val reports = snapshot.children.mapNotNull { it.getValue(CrowdReportDto::class.java) }
                val latestStatus = reports.maxByOrNull { it.timestamp }?.status?.let {
                    try { CrowdStatus.valueOf(it.uppercase()) } catch (e: Exception) { CrowdStatus.UNKNOWN }
                } ?: CrowdStatus.UNKNOWN
                trySend(latestStatus)
            }
            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException(), "Firebase onCancelled")
                trySend(CrowdStatus.UNKNOWN)
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override fun getLastUpdated(routeId: String): Flow<Long?> = callbackFlow {
        val ref = db.getReference("crowd_reports").child(routeId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reports = snapshot.children.mapNotNull { it.getValue(CrowdReportDto::class.java) }
                trySend(reports.maxByOrNull { it.timestamp }?.timestamp)
            }
            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun submitReport(report: CrowdReport): Result<Unit> = try {
        val ref = db.getReference("crowd_reports").child(report.routeId).push()
        ref.setValue(CrowdReportDto.fromDomain(report)).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error submitting report")
        Result.failure(e)
    }

    override fun getAlternatives(routeId: String): Flow<List<AlternativeContact>> = callbackFlow {
        val ref = db.getReference("alternatives").child(routeId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val contacts = snapshot.children.mapNotNull { it.getValue(AlternativeContactDto::class.java)?.toDomain() }
                trySend(contacts)
            }
            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
