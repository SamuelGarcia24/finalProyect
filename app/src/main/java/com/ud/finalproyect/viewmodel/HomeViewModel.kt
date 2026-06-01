package com.ud.finalproyect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ud.finalproyect.model.data.Medication
import com.ud.finalproyect.model.repository.MedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val userId: String = "") : ViewModel() {

    private val repository = MedicationRepository()
    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications

    init {
        if (userId.isNotEmpty()) loadMedications()
    }

    fun loadForUser(id: String) {
        viewModelScope.launch {
            repository.getMedications(id).collect { all ->
                val today = java.time.LocalDate.now().toString()
                _medications.value = all.filter { med ->
                    med.isActive && med.startDate <= today && med.endDate >= today
                }
            }
        }
    }

    private fun loadMedications() {
        viewModelScope.launch {
            repository.getMedications(userId).collect { all ->
                val today = java.time.LocalDate.now().toString()
                _medications.value = all.filter { med ->
                    med.isActive && med.startDate <= today && med.endDate >= today
                }
            }
        }
    }

    fun deleteMedication(medicationId: String) {
        repository.deleteMedication(medicationId)
    }
}