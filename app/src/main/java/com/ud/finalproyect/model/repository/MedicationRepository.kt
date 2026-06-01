package com.ud.finalproyect.model.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ud.finalproyect.model.data.Medication
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MedicationRepository {

    private val db = FirebaseDatabase.getInstance()
    private val medicationsRef = db.getReference("medications")

    fun getMedications(userId: String): Flow<List<Medication>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    child.getValue(Medication::class.java)?.copy(id = child.key ?: "")
                }.filter { it.userId == userId }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        medicationsRef.addValueEventListener(listener)
        awaitClose { medicationsRef.removeEventListener(listener) }
    }

    fun saveMedication(medication: Medication) {
        val key = medicationsRef.push().key ?: return
        medicationsRef.child(key).setValue(medication.copy(id = key))
    }

    fun toggleTaken(medicationId: String, date: String, currentTakenDates: List<String>) {
        val updated = if (currentTakenDates.contains(date)) {
            currentTakenDates - date
        } else {
            currentTakenDates + date
        }
        medicationsRef.child(medicationId).child("takenDates").setValue(updated)
    }

    fun deleteMedication(medicationId: String) {
        medicationsRef.child(medicationId).removeValue()
    }
}