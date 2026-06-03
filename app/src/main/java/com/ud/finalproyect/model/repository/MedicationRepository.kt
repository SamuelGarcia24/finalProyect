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

    fun saveMedication(medication: Medication): Medication {
        val key = medicationsRef.push().key ?: return medication
        val savedMedication = medication.copy(id = key)
        medicationsRef.child(key).setValue(savedMedication)
        return savedMedication
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

    // Actualiza la hora real en que se tomó el medicamento.
    // Si scheduledTime es provisto, la llave usada será "{date}|{scheduledTime}" permitiendo múltiples tomas en un mismo día.
    fun updateActualTakenTime(medicationId: String, date: String, actualTime: String, scheduledTime: String? = null) {
        val key = if (!scheduledTime.isNullOrEmpty()) "${date}|${scheduledTime}" else date
        medicationsRef.child(medicationId).child("actualTakenTimes").child(key).setValue(actualTime)
    }

    // Elimina la entrada actualTakenTimes específica (por fecha o por fecha|hora)
    fun removeActualTakenTime(medicationId: String, date: String, scheduledTime: String? = null) {
        val key = if (!scheduledTime.isNullOrEmpty()) "${date}|${scheduledTime}" else date
        medicationsRef.child(medicationId).child("actualTakenTimes").child(key).removeValue()
    }

    // Obtiene la hora real de toma para una fecha específica
    fun getActualTakenTime(medicationId: String, date: String): Flow<String?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val time = snapshot.getValue(String::class.java)
                trySend(time)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        medicationsRef.child(medicationId).child("actualTakenTimes").child(date).addValueEventListener(listener)
        awaitClose { medicationsRef.child(medicationId).child("actualTakenTimes").child(date).removeEventListener(listener) }
    }
}
