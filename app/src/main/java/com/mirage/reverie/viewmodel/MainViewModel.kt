package com.mirage.reverie.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {
    fun isUserAuthenticated(): Boolean = auth.currentUser != null
    fun logout() = auth.signOut()
    fun getUserId(): String = auth.uid.orEmpty()
}
