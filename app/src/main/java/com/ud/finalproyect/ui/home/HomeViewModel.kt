package com.ud.finalproyect.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ud.finalproyect.data.Medication
import com.ud.finalproyect.data.getMockMedications
import com.ud.finalproyect.data.getMoreMockMedications
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _medications = MutableStateFlow(getMockMedications())
    val medications: StateFlow<List<Medication>> = _medications

    private var showMoreClicked = false

    fun onShowMore() {
        if (!showMoreClicked) {
            showMoreClicked = true
            viewModelScope.launch {
                val currentList = _medications.value.toMutableList()
                currentList.addAll(getMoreMockMedications())
                _medications.value = currentList
            }
        }
    }
}