package com.mirage.reverie.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.mirage.reverie.data.model.AllDiaries
import com.mirage.reverie.data.model.TimeCapsule
import com.mirage.reverie.data.repository.TimeCapsuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject


sealed class TimeCapsuleUiState {
    data object Loading : TimeCapsuleUiState()
    data class Success(
        private val sentTimeCapsule: Map<String, TimeCapsule>, // capsule create dall'utente, scadute(inviate) e non scadute(programmate)
        private val receivedTimeCapsule: Map<String, TimeCapsule>, // capsule destinate all'utente (scadute e non)
    ) : TimeCapsuleUiState() {
        val timeCapsuleScheduled: Map<String, TimeCapsule> // capsule create dall'utente ma non ancora inviate in quanto la scadenza ancora non arriva
            get() = sentTimeCapsule.filter { it.value.deadline < Timestamp.now() }

        val timeCapsuleSent: Map<String, TimeCapsule>
            get() = sentTimeCapsule.filter { it.value.deadline >= Timestamp.now() }

        val timeCapsuleReceived: Map<String, TimeCapsule> // capsule ricevute dall'utente, che deve aprire
            get() = receivedTimeCapsule.filter { it.value.deadline >= Timestamp.now() }
    }
    data class Error(val exception: Throwable) : TimeCapsuleUiState()
}

@HiltViewModel
class TimeCapsuleViewModel @Inject constructor(
    private val repository: TimeCapsuleRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _uiState = MutableStateFlow<TimeCapsuleUiState>(TimeCapsuleUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        onStart()
    }

    // load diaries
    private fun onStart() {
        auth.uid?.let { userId ->
            viewModelScope.launch {

                val sentTimeCapsules = repository.getUserSentTimeCapsules(userId)
                val receivedTimeCapsules = repository.getUserReceivedTimeCapsules(userId)
                val sentTimeCapsulesMap = sentTimeCapsules.associateBy{sentTimeCapsule -> sentTimeCapsule.id}
                val receivedTimeCapsulesMap = sentTimeCapsules.associateBy{receivedTimeCapsule -> receivedTimeCapsule.id}
                _uiState.value = TimeCapsuleUiState.Success(sentTimeCapsulesMap, receivedTimeCapsulesMap)
            }
        }
    }
}