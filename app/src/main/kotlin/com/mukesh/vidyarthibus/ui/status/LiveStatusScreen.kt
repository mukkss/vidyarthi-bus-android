package com.mukesh.vidyarthibus.ui.status

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukesh.vidyarthibus.domain.model.BusRoute
import com.mukesh.vidyarthibus.domain.model.CrowdStatus
import com.mukesh.vidyarthibus.domain.usecase.GetCrowdStatusUseCase
import com.mukesh.vidyarthibus.domain.usecase.GetLastUpdatedUseCase
import com.mukesh.vidyarthibus.domain.usecase.GetRoutesUseCase
import com.mukesh.vidyarthibus.domain.usecase.SubmitReportUseCase
import com.mukesh.vidyarthibus.ui.theme.DangerRed
import com.mukesh.vidyarthibus.ui.theme.SuccessGreen
import com.mukesh.vidyarthibus.ui.theme.WarningYellow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class LiveStatusViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getCrowdStatusUseCase: GetCrowdStatusUseCase,
    getLastUpdatedUseCase: GetLastUpdatedUseCase,
    getRoutesUseCase: GetRoutesUseCase,
    private val submitReportUseCase: SubmitReportUseCase
) : ViewModel() {

    private val routeId: String = checkNotNull(savedStateHandle["routeId"])

    val route: StateFlow<BusRoute?> = getRoutesUseCase()
        .map { routes -> routes.find { it.id == routeId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val crowdStatus: StateFlow<CrowdStatus> = getCrowdStatusUseCase(routeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CrowdStatus.UNKNOWN)

    val lastUpdated: StateFlow<Long?> = getLastUpdatedUseCase(routeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _reportResult = MutableSharedFlow<Result<Unit>>()
    val reportResult = _reportResult.asSharedFlow()

    private val _isReporting = MutableStateFlow(false)
    val isReporting: StateFlow<Boolean> = _isReporting

    fun submitReport(status: CrowdStatus, deviceId: String) {
        val currentRoute = route.value ?: return
        viewModelScope.launch {
            _isReporting.value = true
            val result = submitReportUseCase(currentRoute, status, deviceId)
            _reportResult.emit(result)
            _isReporting.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveStatusScreen(
    onNavigateToAlternatives: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: LiveStatusViewModel = hiltViewModel()
) {
    val route by viewModel.route.collectAsState()
    val crowdStatus by viewModel.crowdStatus.collectAsState()
    val lastUpdated by viewModel.lastUpdated.collectAsState()
    val isReporting by viewModel.isReporting.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.reportResult.collectLatest { result ->
            result.onSuccess {
                snackbarHostState.showSnackbar("Report submitted successfully")
            }.onFailure { error ->
                snackbarHostState.showSnackbar(error.message ?: "Failed to submit report")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(route?.name ?: "Live Status") },
                actions = {
                    IconButton(onClick = { /* Refresh logic if needed, RTDB is live though */ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CrowdMeter(status = crowdStatus)
            
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = when (crowdStatus) {
                    CrowdStatus.EMPTY -> "Plenty of Seats"
                    CrowdStatus.SEATED -> "Filling Up"
                    CrowdStatus.FULL -> "Bus is Full"
                    CrowdStatus.UNKNOWN -> "Status Unknown"
                },
                style = MaterialTheme.typography.headlineMedium,
                color = getStatusColor(crowdStatus),
                fontWeight = FontWeight.Bold
            )

            lastUpdated?.let {
                Text(
                    text = "Last updated: ${formatTimestamp(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (crowdStatus == CrowdStatus.FULL) {
                Button(
                    onClick = { onNavigateToAlternatives(route?.id ?: "") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Alternatives (Shared Auto)")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            ReportStatusSection(
                onReport = { status -> viewModel.submitReport(status, "anon_user") }, // deviceId should be real in prod
                isReporting = isReporting
            )
        }
    }
}

@Composable
fun CrowdMeter(status: CrowdStatus) {
    val progress = when (status) {
        CrowdStatus.EMPTY -> 0.2f
        CrowdStatus.SEATED -> 0.6f
        CrowdStatus.FULL -> 1.0f
        CrowdStatus.UNKNOWN -> 0.0f
    }
    
    val color by animateColorAsState(targetValue = getStatusColor(status))

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Empty", style = MaterialTheme.typography.labelSmall)
            Text("Seated", style = MaterialTheme.typography.labelSmall)
            Text("Full", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun ReportStatusSection(onReport: (CrowdStatus) -> Unit, isReporting: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Are you on this bus?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Report current crowd status to help others.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusButton("Empty", SuccessGreen, Modifier.weight(1f)) { onReport(CrowdStatus.EMPTY) }
                StatusButton("Seated", WarningYellow, Modifier.weight(1f)) { onReport(CrowdStatus.SEATED) }
                StatusButton("Full", DangerRed, Modifier.weight(1f)) { onReport(CrowdStatus.FULL) }
            }
            if (isReporting) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }
        }
    }
}

@Composable
fun StatusButton(text: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        Text(text, fontSize = 12.sp, color = Color.White)
    }
}

fun getStatusColor(status: CrowdStatus): Color = when (status) {
    CrowdStatus.EMPTY -> SuccessGreen
    CrowdStatus.SEATED -> WarningYellow
    CrowdStatus.FULL -> DangerRed
    CrowdStatus.UNKNOWN -> Color.Gray
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
