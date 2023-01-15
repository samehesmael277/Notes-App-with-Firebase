package com.sameh.notesapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note(
    var id: String? = null,
    var title: String? = null,
    var note: String? = null,
    var date: String? = null
): Parcelable
