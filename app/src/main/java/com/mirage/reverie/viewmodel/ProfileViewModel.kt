package com.mirage.reverie.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mirage.reverie.data.model.User
import com.mirage.reverie.data.repository.UserRepository
import com.mirage.reverie.navigation.ProfileRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Success(
        val profile: User
    ) : ProfileUiState()
    data class Error(val exception: Throwable) : ProfileUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val profileId = savedStateHandle.toRoute<ProfileRoute>().profileId

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        onStart()
    }

    // load profile
    private fun onStart() {
        viewModelScope.launch {
            val user = userRepository.getUser(profileId)
            _uiState.value = ProfileUiState.Success(user)
        }
    }

    fun overwriteProfile(profile: User?) {
        if (profile != null)
            _uiState.value = ProfileUiState.Success(profile)
    }

}
