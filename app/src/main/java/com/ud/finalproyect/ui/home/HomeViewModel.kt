package com.ud.finalproyect.ui.home

import androidx.lifecycle.ViewModel
import com.ud.finalproyect.data.Medication
import com.ud.finalproyect.data.getMockMedications
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {

    private val _medications = MutableStateFlow(getMockMedications())
    val medications: StateFlow<List<Medication>> = _medications
}