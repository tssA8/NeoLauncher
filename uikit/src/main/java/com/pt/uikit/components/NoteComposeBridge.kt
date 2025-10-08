package com.pt.uikit.components

import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pt.uikit.R

fun interface Save2Listener { fun onSave(text: String, sizeSp: Float) }

object NoteComposeBridge {

    @JvmStatic
    fun setupNoteWidget(noteView: ComposeView, editorView: ComposeView?) {
        noteView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        noteView.setContent {
            val vm: NoteSharedViewModel = viewModel()
            val defaultSizeSp = dimensionResource(id = R.dimen.uikit_note_text_size).value
            LaunchedEffect(Unit) { if (vm.sizeSp <= 0f) vm.update(sizeSp = defaultSizeSp) }
            NoteWidget(
                text = vm.text,
                fontSizeSp = vm.sizeSp,
                onClick = { editorView?.visibility = View.VISIBLE }
            )
        }
    }

    @JvmStatic
    fun setupNoteEditor(editorView: ComposeView, listener: Save2Listener?) {
        editorView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        editorView.setContent {
            val vm: NoteSharedViewModel = viewModel()
            val defaultSizeSp = dimensionResource(id = R.dimen.uikit_note_text_size).value
            val presetText = vm.text
            val presetSize = if (vm.sizeSp > 0f) vm.sizeSp else defaultSizeSp
            NoteEditorDialog(
                initialText = presetText,
                initialSizeSp = presetSize,
                onSave = { text, sizeSp ->
                    vm.update(text = text, sizeSp = sizeSp)
                    editorView.visibility = View.GONE
                    listener?.onSave(text, sizeSp)
                },
                onCancel = { editorView.visibility = View.GONE },
                size = DpSize(426.dp, 526.dp),
                useDialogWindow = false,
                scale = 1.4f
            )
        }
    }
}
