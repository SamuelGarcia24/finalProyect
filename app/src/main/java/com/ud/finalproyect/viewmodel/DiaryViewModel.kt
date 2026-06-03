package com.ud.finalproyect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ud.finalproyect.model.data.Medication
import com.ud.finalproyect.model.repository.MedicationRepository
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

    // Marca una toma real guardando la hora en el mapa actualTakenTimes.
    // scheduledTime debe ser la hora en formato HH:mm (24h) que identifica la dosis.
    fun markTaken(medicationId: String, date: String, scheduledTime: String, actualTime: String) {
        repository.updateActualTakenTime(medicationId, date, actualTime, scheduledTime)
    }

    // Desmarca (elimina) una toma guardada para una dosis específica
    fun unmarkTaken(medicationId: String, date: String, scheduledTime: String) {
        repository.removeActualTakenTime(medicationId, date, scheduledTime)
    }

    fun toggleTaken(medicationId: String, date: String) {
        val medication = _medications.value.find { it.id == medicationId } ?: return
        repository.toggleTaken(medicationId, date, medication.takenDates)
    }
}