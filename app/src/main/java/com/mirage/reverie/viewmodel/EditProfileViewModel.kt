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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileInputState(
    val name: String = "",
    val surname: String = ""
)

sealed class EditProfileUiState {
    data object Loading : EditProfileUiState()
    data class Success(
        val profile: User
    ) : EditProfileUiState()
    data class Complete(
        val profile: User
    ): EditProfileUiState()
    data class Error(val exception: Throwable) : EditProfileUiState()}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val profileRoute = savedStateHandle.toRoute<ProfileRoute>()

    private val _uiState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _inputState = MutableStateFlow(EditProfileInputState())
    val inputState = _inputState.asStateFlow()

    init {
        loadProfile(profileRoute.profileId)
    }

    private fun loadProfile(profileId: String) {
        //_uiState.value = EditProfileUiState.Loading
        viewModelScope.launch {
            runCatching {
                userRepository.getUser(profileId)
            }.onSuccess { user ->
                _inputState.value = EditProfileInputState(user.name, user.surname)
                _uiState.value = EditProfileUiState.Success(user)
            }.onFailure { e ->
                _uiState.value = EditProfileUiState.Error(e)
            }
        }
    }

//    TODO: Change email both in auth and db
//    fun changeEmail(newEmail: String) {}

    fun onSaveProfile() {
        val state = uiState.value
        if (state !is EditProfileUiState.Success) return

        viewModelScope.launch {
            //_uiState.value = EditProfileUiState.Loading
            val updatedUser = state.profile.copy(
                name = _inputState.value.name,
                surname = _inputState.value.surname
            )
            runCatching {
                userRepository.updateUser(updatedUser)
            }.onSuccess {
                _uiState.value = EditProfileUiState.Complete(updatedUser)
            }.onFailure { e ->
                _uiState.value = EditProfileUiState.Error(e)
            }
        }
    }

    fun onNameChange(newName: String) {
        _inputState.update { state ->
            state.copy(name = newName)
        }
    }

    fun onSurnameChange(newSurname: String) {
        _inputState.update { state ->
            state.copy(name = newSurname)
        }
    }
}