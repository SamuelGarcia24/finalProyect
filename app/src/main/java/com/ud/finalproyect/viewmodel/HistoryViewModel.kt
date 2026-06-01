package com.ud.finalproyect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ud.finalproyect.model.data.Medication
import com.ud.finalproyect.model.repository.MedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {
    private val repository = MedicationRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications.asStateFlow()

    init {
        fetchMedications()
    }

    private fun fetchMedications() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.getMedications(userId).collect {
                _medications.value = it
            }
        }
    }
}