package com.sameh.notesapp.ui.noteinfo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sameh.notesapp.databinding.FragmentNoteInfoBinding
import com.sameh.notesapp.viewmodel.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoteInfoFragment : Fragment() {

    private lateinit var binding: FragmentNoteInfoBinding

    private val noteViewModel: NoteViewModel by viewModels()

    private val args by navArgs<NoteInfoFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoteInfoBinding.inflate(inflater, container, false)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDataInUi()

        binding.floatingBtnDelete.setOnClickListener {
            noteViewModel.deleteNote(args.note)
            goToNoteListFragment()
        }
    }

    private fun setDataInUi() {
        binding.tvTitle.text = args.note.title
        binding.tvDate.text = args.note.date
        binding.tvNote.text = args.note.note
    }

    private fun goToNoteListFragment() {
        val action = NoteInfoFragmentDirections.actionNoteInfoFragmentToNoteListFragment()
        findNavController().navigate(action)
    }

}