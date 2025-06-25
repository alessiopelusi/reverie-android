package com.mirage.reverie.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mirage.reverie.R
import com.mirage.reverie.data.model.User
import com.mirage.reverie.data.repository.UserRepository
import com.mirage.reverie.navigation.ProfileRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileInputState(
    val username: String = "",
    val usernameError: String = "",
    val name: String = "",
    val nameError: String = "",
    val surname: String = "",
    val surnameError: String = "",
)

sealed class EditProfileUiState {
    data object Loading : EditProfileUiState()
    data class Idle(
        val profile: User
    ) : EditProfileUiState()
    data class Complete(
        val profile: User
    ): EditProfileUiState()
    data class LoadingError(val exception: Throwable) : EditProfileUiState()
    data class InputError(
        val profile: User,
        val errorMessage: String
    ) : EditProfileUiState()
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: UserRepository,
    private val context: Context
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
                repository.getUser(profileId)
            }.onSuccess { user ->
                _inputState.value = EditProfileInputState()
                onUsernameChange(user.username)
                onNameChange(user.name)
                onSurnameChange(user.surname)

                _uiState.value = EditProfileUiState.Idle(user)
            }.onFailure { e ->
                _uiState.value = EditProfileUiState.LoadingError(e)
            }
        }
    }

//    TODO: Change email both in auth and db
//    fun changeEmail(newEmail: String) {}


    private suspend fun validateUsername(username: String): String {
        // get profile
        val currentState = uiState.value
        if (currentState !is EditProfileUiState.Idle && currentState !is EditProfileUiState.InputError) return ""

        val profile = when (currentState) {
            is EditProfileUiState.Idle -> currentState.profile
            is EditProfileUiState.InputError -> currentState.profile
            else -> return ""// Unreachable but for safety
        }

        return when {
            username == profile.username -> ""
            username.isBlank() -> context.getString(R.string.username_mandatory)
            repository.isUsernameTaken(username) -> context.getString(R.string.username_already_taken)
            else -> ""
        }
    }

    private fun validateName(name: String): String {
        return if (name.isBlank()) {
            context.getString(R.string.name_mandatory)
        } else {
            ""
        }
    }

    private fun validateSurname(surname: String): String {
        return if (surname.isBlank()) {
            context.getString(R.string.surname_mandatory)
        } else {
            ""
        }
    }

    private var usernameCheckJob: Job? = null

    fun onUsernameChange(newUsername: String) {
        _inputState.update { state ->
            state.copy(
                username = newUsername,
            )
        }

        // Cancel any ongoing username check
        usernameCheckJob?.cancel()

        // Start a new coroutine for validation
        usernameCheckJob = viewModelScope.launch {
            val error = validateUsername(newUsername)

            _inputState.update { state ->
                state.copy(
                    usernameError = error
                )
            }
        }
    }

    fun onNameChange(newName: String) {
        val error = validateName(newName)

        _inputState.update { state ->
            state.copy(
                name = newName,
                nameError = error
            )
        }
    }

    fun onSurnameChange(newSurname: String) {
        val error = validateSurname(newSurname)

        _inputState.update { state ->
            state.copy(
                surname = newSurname,
                surnameError = error
            )
        }
    }

    fun onSaveProfile() {
        val currentState = uiState.value
        if (currentState !is EditProfileUiState.Idle && currentState !is EditProfileUiState.InputError) return

        val profile = when (currentState) {
            is EditProfileUiState.Idle -> currentState.profile
            is EditProfileUiState.InputError -> currentState.profile
            else -> return // Unreachable but for safety
        }

        _uiState.update { state ->
            EditProfileUiState.Idle(profile)
        }

        val inState = inputState.value

        viewModelScope.launch {
            // Validate all fields and update input state
            val updatedState = inState.copy(
                usernameError = validateUsername(inState.username),
                nameError = validateName(inState.name),
                surnameError = validateSurname(inState.surname),
            )
            _inputState.update { updatedState }

            // Check for any validation errors
            if (listOf(
                    updatedState.usernameError,
                    updatedState.nameError,
                    updatedState.surnameError,
                ).any { it.isNotBlank() } // Check for non-empty errors
            ) {
                _uiState.update { EditProfileUiState.InputError(profile, "") }
                return@launch
            }

            val updatedUser = profile.copy(
                username = _inputState.value.username,
                name = _inputState.value.name,
                surname = _inputState.value.surname
            )

            runCatching {
                repository.updateUser(updatedUser)
            }.onSuccess {
                _uiState.value = EditProfileUiState.Complete(updatedUser)
            }.onFailure { e ->
                _uiState.value = EditProfileUiState.InputError(profile, context.getString(R.string.update_error))
            }
        }
    }
}