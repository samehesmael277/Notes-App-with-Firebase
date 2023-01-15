package com.sameh.notesapp.ui.notelist.adapter

import android.app.Application
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sameh.notesapp.databinding.NotesOnRecBinding
import com.sameh.notesapp.model.Note
import javax.inject.Inject

class NoteAdapter @Inject constructor(
    private val context: Application,
) : ListAdapter<Note, NoteAdapter.ViewHolder>(NoteDiffCallBack()) {

    var onNoteClickListener: OnNoteClickListener? = null

    fun setData(notes: ArrayList<Note>) {
        this.submitList(notes)
    }

    inner class ViewHolder(private val binding: NotesOnRecBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.tvTitle.text = note.title
            binding.tvDate.text = note.date

            binding.cardView.setOnClickListener {
                onNoteClickListener?.onNoteClickListener(note)
            }

            binding.cardView.setOnLongClickListener {
                onNoteClickListener?.onNoteLongClickListener(note)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            NotesOnRecBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

}


class NoteDiffCallBack : DiffUtil.ItemCallback<Note>() {

    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem == newItem
    }

}