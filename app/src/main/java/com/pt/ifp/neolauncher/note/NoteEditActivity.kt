package com.pt.ifp.neolauncher.note

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class NoteEditActivity : AppCompatActivity(), NoteEditViewMvc.Listener {

    private lateinit var viewMvc: NoteEditViewMvc

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewMvc = NoteEditViewMvc(layoutInflater, null)
        viewMvc.registerListener(this)
        setContentView(viewMvc.rootView)

        // ★ 可選：把呼叫端帶來的內容預填到編輯框
        intent.getStringExtra(EXTRA_NOTE_CONTENT)?.let { preset ->
            viewMvc.getEditNote().setText(preset)
            viewMvc.getEditNote().setSelection(preset.length)
        }
    }

    override fun onDestroy() {
        viewMvc.unregisterListener(this)
        super.onDestroy()
    }

    // ====== NoteEditViewMvc.Listener ======
    override fun noteSave() {
        val editor = viewMvc.getEditNote()
        val content = editor.text?.toString().orEmpty()
        val sizeSp = pxToSp(editor.textSize) // px -> sp

        setResult(
            Activity.RESULT_OK,
            Intent()
                .putExtra(EXTRA_NOTE_CONTENT, content)
                .putExtra(EXTRA_NOTE_TEXT_SIZE, sizeSp) // ← 用這個 key 放字體大小
        )
        finish()
    }


    override fun noteCancel() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    // ====== Helpers ======
    private fun pxToSp(px: Float): Float {
        val scaledDensity = resources.displayMetrics.scaledDensity
        return px / scaledDensity
    }

    companion object {
        const val SYS_PROPERTY_ROLE = "persist.sys.benq.role"
        const val DEFAULT_ROLE = "default"
        const val EXTRA_NOTE_CONTENT = "extra_note_content"

        const val EXTRA_NOTE_TEXT_SIZE = "extra_note_text_size"  // 字體大小（Float, 單位 sp）
    }
}
