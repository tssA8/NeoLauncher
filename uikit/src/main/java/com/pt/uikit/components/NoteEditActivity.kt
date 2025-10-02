package com.pt.uikit.components

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.pt.uikit.Constant
import com.pt.uikit.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob


class NoteEditActivity : AppCompatActivity(), NoteEditViewMvc.Listener {

    private lateinit var viewMvc: NoteEditViewMvc
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_WidgetSetting)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewMvc = NoteEditViewMvc(layoutInflater, null)
        setContentView(viewMvc.rootView)
        setupWindowDimAmount()

        // ★ 可選：把呼叫端帶來的內容預填到編輯框
        intent.getStringExtra(Constant.EXTRA_NOTE_CONTENT)?.let { preset ->
            viewMvc.getEditNote().setText(preset)
            viewMvc.getEditNote().setSelection(preset.length)
        }
    }

    private fun setupWindowDimAmount() {
        val params = window.attributes
        params.dimAmount = 0.5f
        window.attributes = params
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    override fun onStart() {
        super.onStart()
        viewMvc.registerListener(this)
    }

    override fun onStop() {
        super.onStop()
//        coroutineScope.coroutineContext.cancelChildren()
        viewMvc.unregisterListener(this)
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
            RESULT_OK,
            Intent()
                .putExtra(Constant.EXTRA_NOTE_CONTENT, content)
                .putExtra(Constant.EXTRA_NOTE_TEXT_SIZE, sizeSp) // ← 用這個 key 放字體大小
        )
        finish()
    }


    override fun noteCancel() {
        setResult(RESULT_CANCELED)
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
    }
}
