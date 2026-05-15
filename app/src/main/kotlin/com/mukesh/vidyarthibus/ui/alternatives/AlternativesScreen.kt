package com.mukesh.vidyarthibus.ui.alternatives

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukesh.vidyarthibus.domain.model.AlternativeContact
import com.mukesh.vidyarthibus.domain.usecase.GetAlternativesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AlternativesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getAlternativesUseCase: GetAlternativesUseCase
) : ViewModel() {
    val routeId: String = checkNotNull(savedStateHandle["routeId"])
    
    val alternatives: StateFlow<List<AlternativeContact>> = getAlternativesUseCase(routeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlternativesScreen(
    onBack: () -> Unit,
    viewModel: AlternativesViewModel = hiltViewModel()
) {
    val alternatives by viewModel.alternatives.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Shared Auto Contacts") })
        }
    ) { padding ->
        if (alternatives.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No alternative contacts found for this route.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(alternatives) { contact ->
                    AlternativeItem(contact)
                }
            }
        }
    }
}

@Composable
fun AlternativeItem(contact: AlternativeContact) {
    ListItem(
        headlineContent = { Text(contact.name) },
        supportingContent = { Text(contact.phone) },
        trailingContent = {
            IconButton(onClick = { /* Launch dialer */ }) {
                Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
            }
        }
    )
}
