package com.pt.uikit.components

import android.util.Log
import android.util.TypedValue
import android.view.ActionMode
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.pt.uikit.R

class NoteEditViewMvc(
    private val layoutInflater: LayoutInflater,
    private val parent: ViewGroup?
) : BaseViewMvc<NoteEditViewMvc.Listener>(
    layoutInflater,
    parent,
    R.layout.uikit_note_edit_activity_edla
) {

    interface Listener {
        fun noteSave()
        fun noteCancel()
    }

    private lateinit var noteEdit: EditText
    private lateinit var noteEditorView: TextView
    private lateinit var noteRoot: View
    private lateinit var noteTextSizeLarge: Button
    private lateinit var noteTextSizeMedium: Button
    private lateinit var noteTextSizeSmall: Button
    private lateinit var noteSave: Button
    private lateinit var noteCancel: Button
    private lateinit var editNoteContainer: ConstraintLayout

    init {
        noteRoot = findViewById(R.id.note_edit)
        noteEdit = findViewById(R.id.note_editor_view)
        noteSave = findViewById(R.id.note_edit_save)
        noteCancel = findViewById(R.id.note_edit_cancel)
        noteTextSizeLarge = findViewById(R.id.note_text_size_large)
        noteTextSizeMedium = findViewById(R.id.note_text_size_medium)
        noteTextSizeSmall = findViewById(R.id.note_text_size_small)
        noteEditorView = findViewById<TextView>(R.id.note_editor_view)
        editNoteContainer = findViewById<ConstraintLayout>(R.id.note_edit)

        val role = Util.getSystemProperty(
            Companion.SYS_PROPERTY_ROLE,
            Companion.DEFAULT_ROLE
        )
        val noteData = getPrefUsersNote(context, role).find { it.role == role }
        requireNotNull(noteData) { "note data must be non-null" }

        setUpNoteData(role)
        noteSave.setOnClickListener {
            for (listener in listeners) {
                listener.noteSave()
            }
        }

        noteCancel.setOnClickListener {
            for (listener in listeners) {
                listener.noteCancel()
            }
        }

//        if (BuildConfig.FEATURE_WIDGET_NOTE_TEXT_SIZE) {
            textSizeLayout(noteData)
//        }

        setDpadListener()
    }

    private fun setDpadListener() {
        noteTextSizeSmall.setOnKeyListener { _, keyCode, event ->
            Log.d("setDpadListener ","bbb_ Key code: "+keyCode)
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                noteEdit.requestFocus()
                true
            } else {
                false
            }
        }
        noteTextSizeMedium.setOnKeyListener { _, keyCode, event ->
            Log.d("setDpadListener ","bbb_ Key code: "+keyCode)
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                noteEdit.requestFocus()
                true
            } else {
                false
            }
        }
        noteTextSizeLarge.setOnKeyListener { _, keyCode, event ->
            Log.d("setDpadListener ","bbb_ Key code: "+keyCode)
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                noteEdit.requestFocus()
                true
            } else {
                false
            }
        }
    }

    private fun setUpNoteData(role: String) {
        val noteData = getPrefUsersNote(context, role).find { it.role == role }
        requireNotNull(noteData) { "note data must be non-null" }
        noteEdit.setText(noteData.content)
        noteEdit.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {}
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                return false
            }
        }
        noteEdit.isLongClickable = false
        noteEdit.setTextIsSelectable(false)
    }


    private fun textSizeLayout(noteData: NoteData) {
        noteEdit.textSize = noteData.textSize

        noteTextSizeLarge = findViewById(R.id.note_text_size_large)
        noteTextSizeSmall = findViewById(R.id.note_text_size_small)
        noteTextSizeMedium = findViewById(R.id.note_text_size_medium)

        noteTextSizeLarge.setOnClickListener {
            noteEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_LARGE)
            setNoteTextBackground(noteTextSizeLarge)
        }
        noteTextSizeMedium.setOnClickListener {
            noteEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_MEDIUM)
            setNoteTextBackground(noteTextSizeMedium)
        }
        noteTextSizeSmall.setOnClickListener {
            noteEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SMALL)
            setNoteTextBackground(noteTextSizeSmall)
        }

        when (noteData.textSize) {
            TEXT_SIZE_LARGE -> {
                setNoteTextBackground(noteTextSizeLarge)
            }

            TEXT_SIZE_MEDIUM -> {
                setNoteTextBackground(noteTextSizeMedium)
            }

            TEXT_SIZE_SMALL -> {
                setNoteTextBackground(noteTextSizeSmall)
            }
        }
    }

    private fun setNoteTextBackground(selectView: View) {
        val drawablePressed = ContextCompat.getDrawable(context, R.drawable.uikit_widget_note_text_press)
        val drawableNormal = ContextCompat.getDrawable(context, R.drawable.uikit_widget_note_text_unpress)
        val buttonList = mutableListOf(noteTextSizeLarge, noteTextSizeMedium, noteTextSizeSmall)

        for (button in buttonList) {
            if (button == selectView) button.background = drawablePressed else button.background = drawableNormal
        }
    }

    fun getEditNote(): EditText {
        return noteEdit
    }

    fun getNoteEditorView(): TextView {
        return noteEditorView
    }

    fun getMediumTextView(): TextView {
        return noteTextSizeMedium
    }

    fun getNoteSave():Button {
        return noteSave
    }

    fun getNoteCancel():Button {
        return noteCancel
    }


    companion object {
        private const val TEXT_SIZE_LARGE = 40f
        private const val TEXT_SIZE_MEDIUM = 30f
        private const val TEXT_SIZE_SMALL = 20f

        const val SYS_PROPERTY_ROLE = "persist.sys.benq.role"

        const val DEFAULT_ROLE = "default"
    }

}