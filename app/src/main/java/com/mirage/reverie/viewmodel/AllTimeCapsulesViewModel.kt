package com.mirage.reverie.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.mirage.reverie.data.model.TimeCapsule
import com.mirage.reverie.data.repository.TimeCapsuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


enum class TimeCapsuleType {
    SCHEDULED, SENT, RECEIVED // puoi aggiungere altre sezioni
}

sealed class AllTimeCapsulesUiState {
    data object Loading : AllTimeCapsulesUiState()
    data class Success(
        val sentTimeCapsule: Map<String, TimeCapsule>, // capsule create dall'utente, scadute(inviate) e non scadute(programmate)
        val receivedTimeCapsule: Map<String, TimeCapsule>, // capsule destinate all'utente (scadute e non)
        val buttonState: TimeCapsuleType = TimeCapsuleType.SCHEDULED,
    ) : AllTimeCapsulesUiState() {
        val timeCapsuleScheduledMap: Map<String, TimeCapsule> // capsule create dall'utente ma non ancora inviate in quanto la scadenza ancora non arriva
            get() = sentTimeCapsule.filter { it.value.deadline >= Timestamp.now() }

        val timeCapsuleScheduled: List<TimeCapsule> // capsule create dall'utente ma non ancora inviate in quanto la scadenza ancora non arriva
            get() = timeCapsuleScheduledMap.values.toList().sortedBy { timeCapsule -> timeCapsule.deadline }

        val timeCapsuleSentMap: Map<String, TimeCapsule>
            get() = sentTimeCapsule.filter { it.value.deadline < Timestamp.now() }

        val timeCapsuleSent: List<TimeCapsule>
            get() = timeCapsuleSentMap.values.toList().sortedBy { timeCapsule -> timeCapsule.deadline }

        val timeCapsuleReceivedMap: Map<String, TimeCapsule> // capsule ricevute dall'utente, che deve aprire
            get() = receivedTimeCapsule.filter { it.value.deadline < Timestamp.now() }

        val timeCapsuleReceived: List<TimeCapsule> // capsule ricevute dall'utente, che deve aprire
            get() = timeCapsuleReceivedMap.values.toList().sortedBy { timeCapsule -> timeCapsule.deadline }

        val buttonElements: List<TimeCapsuleType>
            get() = TimeCapsuleType.entries
    }
    data class Error(val exception: Throwable) : AllTimeCapsulesUiState()
}

@HiltViewModel
class AllTimeCapsulesViewModel @Inject constructor(
    private val repository: TimeCapsuleRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _uiState = MutableStateFlow<AllTimeCapsulesUiState>(AllTimeCapsulesUiState.Loading)
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
                val receivedTimeCapsulesMap = receivedTimeCapsules.associateBy{receivedTimeCapsule -> receivedTimeCapsule.id}
                _uiState.value = AllTimeCapsulesUiState.Success(sentTimeCapsulesMap, receivedTimeCapsulesMap)
            }
        }
    }

    fun onButtonStateUpdate(newButtonState: TimeCapsuleType) {
        val state = uiState.value
        if (state !is AllTimeCapsulesUiState.Success) return

        _uiState.update {
            AllTimeCapsulesUiState.Success(
                state.sentTimeCapsule,
                state.receivedTimeCapsule,
                newButtonState,
            )
        }
    }

    fun addNewTimeCapsule(newTimeCapsule: TimeCapsule?) {
        val state = uiState.value
        if (state !is AllTimeCapsulesUiState.Success) return
        if (newTimeCapsule == null) return

        val sentTimeCapsule = state.sentTimeCapsule.toMutableMap()
        val receivedTimeCapsule = state.receivedTimeCapsule.toMutableMap()

        sentTimeCapsule[newTimeCapsule.id] = newTimeCapsule
        if (newTimeCapsule.receivers.contains(auth.uid)) {
            receivedTimeCapsule[newTimeCapsule.id] = newTimeCapsule
        }

        _uiState.update {
            AllTimeCapsulesUiState.Success(
                sentTimeCapsule,
                receivedTimeCapsule,
                state.buttonState
            )
        }
    }
}