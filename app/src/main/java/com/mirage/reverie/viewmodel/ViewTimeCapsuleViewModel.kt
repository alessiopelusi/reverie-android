package com.mirage.reverie.viewmodel

import com.mirage.reverie.data.model.TimeCapsule
import com.mirage.reverie.data.repository.TimeCapsuleRepository
import com.mirage.reverie.navigation.ViewTimeCapsuleRoute

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class ViewTimeCapsuleState {
    data object Loading : ViewTimeCapsuleState()
    data class Success(
        val timeCapsule: TimeCapsule
    ) : ViewTimeCapsuleState()
    data class Error(val exception: Throwable) : ViewTimeCapsuleState()
}

@HiltViewModel
class ViewTimeCapsuleViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: TimeCapsuleRepository
) : ViewModel() {
    private val timeCapsuleId = savedStateHandle.toRoute<ViewTimeCapsuleRoute>().timeCapsuleId

    private val _uiState = MutableStateFlow<ViewTimeCapsuleState>(ViewTimeCapsuleState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        onStart()
    }

    private fun onStart() {
        viewModelScope.launch {
            val timeCapsule = repository.getTimeCapsule(timeCapsuleId)
            _uiState.update { ViewTimeCapsuleState.Success(timeCapsule) }
        }
    }
}
