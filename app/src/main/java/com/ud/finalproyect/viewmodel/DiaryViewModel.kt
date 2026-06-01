package com.ud.finalproyect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ud.finalproyect.data.Medication
import com.ud.finalproyect.data.MedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DiaryViewModel(private val userId: String = "") : ViewModel() {

    private val repository = MedicationRepository()

    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications

    init {
        if (userId.isNotEmpty()) loadMedications(userId)
    }

    fun loadForUser(id: String) {
        loadMedications(id)
    }

    private fun loadMedications(id: String) {
        viewModelScope.launch {
            repository.getMedications(id).collect { all ->
                _medications.value = all
            }
        }
    }

    fun getMedicationsForDate(date: String): List<Medication> {
        return _medications.value.filter { med ->
            med.startDate <= date && med.endDate >= date
        }
    }

    fun toggleTaken(medicationId: String, date: String) {
        val medication = _medications.value.find { it.id == medicationId } ?: return
        repository.toggleTaken(medicationId, date, medication.takenDates)
    }
}