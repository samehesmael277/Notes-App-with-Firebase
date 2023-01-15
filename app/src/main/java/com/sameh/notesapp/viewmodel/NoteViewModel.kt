package com.sameh.notesapp.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.sameh.notesapp.constants.Tag
import com.sameh.notesapp.model.Note
import com.sameh.notesapp.utils.getCurrentDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    application: Application,
    private val myRef: CollectionReference
) : AndroidViewModel(application) {

    private var _notesMutableLiveData = MutableLiveData<ArrayList<Note>>()
    val notesLiveData: LiveData<ArrayList<Note>> get() = _notesMutableLiveData

    private var _emptyDatabase: MutableLiveData<Boolean> = MutableLiveData(false)
    val emptyDatabase: LiveData<Boolean> get() = _emptyDatabase

    private var _loading: MutableLiveData<Boolean> = MutableLiveData(true)
    val loading: LiveData<Boolean> get() = _loading

    private val context = application

    fun checkIfDataBaseIsEmpty(notesList: ArrayList<Note>) {
        _emptyDatabase.value = notesList.isEmpty()
    }

    fun insertNote(title: String, note: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loading.postValue(true)
                val newNote2 = Note(null, title, note, getCurrentDate())
                myRef.add(newNote2)
                    .addOnSuccessListener {
                        myRef.document(it.id).update("id", it.id)
                        showContext("added")
                        _loading.postValue(false)
                    }
                    .addOnFailureListener {
                        showContext("unable to add")
                        _loading.postValue(false)
                    }
            } catch (e: Exception) {
                Log.d(Tag.TAG, "insertNote: ${e.message}")
            }
        }
    }

    fun getNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loading.postValue(true)
                myRef.orderBy("date", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener {
                        if (!it.isEmpty) {
                            val noteList = it.toObjects(Note::class.java)
                            _notesMutableLiveData.postValue(noteList as ArrayList<Note>)
                            _loading.postValue(false)
                        }
                    }
                    .addOnFailureListener {
                        showContext("unable to get notes: ${it.message}")
                        _loading.postValue(false)
                    }
            } catch (e: Exception) {
                Log.d(Tag.TAG, "getNotes: ${e.message}")
                _loading.postValue(false)
            }
        }
    }

    fun snapShotListener() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loading.postValue(true)
                myRef.orderBy("date", Query.Direction.DESCENDING)
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            showContext("unable to update notes: ${error.message}")
                            _loading.postValue(false)
                            return@addSnapshotListener
                        }
                        if (value != null) {
                            val noteList = value.toObjects(Note::class.java)
                            _notesMutableLiveData.postValue(noteList as ArrayList<Note>)
                            _loading.postValue(false)
                        } else {
                            Log.d(Tag.TAG, "snapShotListener: current data null")
                            _loading.postValue(false)
                        }
                    }
            } catch (e: Exception) {
                Log.d(Tag.TAG, "snapShotListener: ${e.message}")
                _loading.postValue(false)
            }
        }
    }

    fun updateNote(oldNote: Note, newNote: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loading.postValue(true)
                myRef.document(oldNote.id!!).update(
                    "title", newNote.title,
                    "note", newNote.note,
                    "date", getCurrentDate()
                )
                showContext("updated")
                _loading.postValue(false)
            } catch (e: Exception) {
                showContext("unable to update: ${e.message}")
                _loading.postValue(false)
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loading.postValue(true)
                myRef.document(note.id!!).delete()
                showContext("deleted")
                _loading.postValue(false)
            } catch (e: Exception) {
                showContext("unable to delete: ${e.message}")
                _loading.postValue(false)
            }
        }
    }

    fun deleteAllNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loading.postValue(true)
                myRef.get()
                    .addOnSuccessListener {
                        for (doc in it) {
                            myRef.document(doc.id).delete()
                        }
                        showContext("all note deleted")
                        _loading.postValue(false)
                    }
                    .addOnFailureListener {
                        showContext("unable to delete all notes")
                        _loading.postValue(false)
                    }
            } catch (e: Exception) {
                Log.d(Tag.TAG, "deleteAllNotes: ${e.message}")
                _loading.postValue(false)
            }
        }
    }

    private fun showContext(words: String) {
        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(context, words, Toast.LENGTH_SHORT).show()
        }
    }

}

/*

*********************************** real time database ***********************************

private var noteList = ArrayList<Note>()

fun getNotesFromFirebase() {
    viewModelScope.launch(Dispatchers.IO) {
        try {
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {
                        noteList.clear()
                        for (data in snapshot.children) {
                            val note = data.getValue(Note::class.java)
                            noteList.add(0, note!!)
                        }
                        _notesMutableLiveData.postValue(noteList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("myTAG", "onCancelled: ${error.toException()}")
                }
            })
        } catch (e: Exception) {
            Log.d("myTAG", "getNotesFromFirebase: ${e.message}")
        }
    }
}

fun insertData(title: String, note: String) {
    viewModelScope.launch(Dispatchers.IO) {
        try {
            val id = myRef.push().key
            val myNote = Note(id!!, title, note, getCurrentDate())
            myRef.child(id).setValue(myNote)
        } catch (e: Exception) {
            Log.d("myTAG", "insertData: ${e.message}")
        }
    }
}

fun updateNote(note: Note) {
    viewModelScope.launch(Dispatchers.IO) {
        try {
            val childRef = myRef.child(note.id!!)
            childRef.setValue(note)
        } catch (e: Exception) {
            Log.d("myTAG", "insertData: ${e.message}")
        }
    }
}

fun deleteNote(noteId: String) {
    viewModelScope.launch(Dispatchers.IO) {
        try {
            myRef.child(noteId).removeValue()
        } catch (e: Exception) {
            Log.d("myTAG", "insertData: ${e.message}")
        }
    }
}

            // Create a new user with a first, middle, and last name
//            val user2 = hashMapOf(
//                "first" to "Alan",
//                "middle" to "",
//                "last" to "Turing",
//                "born" to 1912
//            )
//            // Add a new document with a generated ID
//            db.collection("users")
//                .add(user2)
//                .addOnSuccessListener { documentReference ->
//                    Log.d("myTAG", "DocumentSnapshot added with ID: ${documentReference.id}")
//                }
//                .addOnFailureListener { e ->
//                    Log.d("myTAG", "Error adding document $e")
//                }
//
//

val id = FirebaseAuth.getInstance().uid
        val newNote = Note(id, title, note, getCurrentDate())
        noteRef.set(newNote)
            .addOnSuccessListener {
                Log.d("myTAG", "insertNote: insertSuccessful")
            }
            .addOnFailureListener {
                Log.d("myTAG", "insertNote: insertFailed")
            }

 */