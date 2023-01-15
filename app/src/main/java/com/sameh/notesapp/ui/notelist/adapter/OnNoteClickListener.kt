package com.sameh.notesapp.ui.notelist.adapter

import com.sameh.notesapp.model.Note

interface OnNoteClickListener {

    fun onNoteClickListener(note: Note)

    fun onNoteLongClickListener(note: Note)

}
