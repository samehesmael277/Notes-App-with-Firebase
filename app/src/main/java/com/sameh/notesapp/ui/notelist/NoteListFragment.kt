package com.sameh.notesapp.ui.notelist

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sameh.notesapp.R
import com.sameh.notesapp.databinding.AddNoteBinding
import com.sameh.notesapp.databinding.FragmentNoteListBinding
import com.sameh.notesapp.databinding.UpdateDeleteNoteBinding
import com.sameh.notesapp.model.Note
import com.sameh.notesapp.ui.notelist.adapter.NoteAdapter
import com.sameh.notesapp.ui.notelist.adapter.OnNoteClickListener
import com.sameh.notesapp.utils.getCurrentDate
import com.sameh.notesapp.viewmodel.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import javax.inject.Inject

@AndroidEntryPoint
class NoteListFragment : Fragment(), OnNoteClickListener {

    private lateinit var binding: FragmentNoteListBinding

    @Inject
    lateinit var noteAdapter: NoteAdapter

    private val noteViewModel: NoteViewModel by viewModels()

    private var currentList: ArrayList<Note>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoteListBinding.inflate(inflater, container, false)
        noteViewModel.getNotes()
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.note_list_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.menu_delete_all -> {
                        noteViewModel.deleteAllNotes()
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        setupRecyclerView()

        noteViewModel.notesLiveData.observe(viewLifecycleOwner) {
            noteViewModel.checkIfDataBaseIsEmpty(it)
            if (it != null) {
                if (it != currentList) {
                    currentList = it
                    noteAdapter.setData(it)
                    smoothScroll()
                }
            } else {
                currentList = null
            }
        }

        binding.floatingBtnAdd.setOnClickListener {
            showDialogAddNote()
        }

        noteViewModel.snapShotListener()

        noteViewModel.loading.observe(viewLifecycleOwner) {
            if (it) {
                showProgressBar()
            } else {
                hideProgressBar()
            }
        }

        noteViewModel.emptyDatabase.observe(viewLifecycleOwner) {
            if (it) {
                showNoDataViews()
            } else {
                hideNoDataViews()
            }
        }

    }

    private fun setupRecyclerView() {
        binding.myRec.adapter = noteAdapter
        binding.myRec.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        binding.myRec.itemAnimator = SlideInUpAnimator().apply {
            addDuration = 200
        }

        noteAdapter.onNoteClickListener = this
    }

    private fun showNoDataViews() {
        binding.tvNoData.visibility = View.VISIBLE
        binding.imgNoData.visibility = View.VISIBLE
    }

    private fun hideNoDataViews() {
        binding.tvNoData.visibility = View.GONE
        binding.imgNoData.visibility = View.GONE
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun smoothScroll() {
        binding.myRec.smoothScrollToPosition(0)
    }

    private fun showDialogAddNote() {
        val addAlertDialogBuilder = AlertDialog.Builder(requireContext())
        val bind: AddNoteBinding = AddNoteBinding.inflate(layoutInflater)
        addAlertDialogBuilder.setView(bind.root)
        val alertDialog = addAlertDialogBuilder.create()
        alertDialog.show()

        bind.btnAdd.setOnClickListener {
            val title = bind.edTitle.text.toString()
            val note = bind.edNote.text.toString()
            val validation = verifyDataFromUser(title, note)

            if (validation) {
                noteViewModel.insertNote(title, note)
                alertDialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun showDialogUpdateDelete(note: Note) {
        val updateAlertDialog = AlertDialog.Builder(requireContext())
        val bind: UpdateDeleteNoteBinding = UpdateDeleteNoteBinding.inflate(layoutInflater)
        updateAlertDialog.setView(bind.root)
        val alertDialog = updateAlertDialog.create()
        alertDialog.show()

        bind.edTitle.setText(note.title)
        bind.edNote.setText(note.note)

        bind.btnUpdate.setOnClickListener {
            val validation = verifyDataFromUser(bind.edTitle.text.toString(), bind.edNote.text.toString())

            if (validation) {
                val newNote = Note(
                    note.id,
                    bind.edTitle.text.toString(),
                    bind.edNote.text.toString(),
                    getCurrentDate()
                )
                noteViewModel.updateNote(note, newNote)
                alertDialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        bind.btnDelete.setOnClickListener {
            noteViewModel.deleteNote(note)
            alertDialog.dismiss()
        }
    }

    private fun verifyDataFromUser(title: String, note: String): Boolean {
        return !(title.isEmpty() || note.isEmpty())
    }

    override fun onNoteClickListener(note: Note) {
        goToNoteInfoFragment(note)
    }

    override fun onNoteLongClickListener(note: Note) {
        showDialogUpdateDelete(note)
    }

    private fun goToNoteInfoFragment(note: Note) {
        val action = NoteListFragmentDirections.actionNoteListFragmentToNoteInfoFragment(note)
        findNavController().navigate(action)
    }

}