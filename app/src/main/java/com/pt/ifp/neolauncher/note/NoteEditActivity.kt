package com.pt.ifp.neolauncher.note

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
class NoteEditActivity : AppCompatActivity(), NoteEditViewMvc.Listener {

    private lateinit var viewMvc: NoteEditViewMvc

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 建立 ViewMvc 並設為內容
        viewMvc = NoteEditViewMvc(layoutInflater, null)
        viewMvc.registerListener(this)
        setContentView(viewMvc.rootView)
    }

    override fun onDestroy() {
        viewMvc.unregisterListener(this)
        super.onDestroy()
    }

    // ====== NoteEditViewMvc.Listener ======
    override fun noteSave() {
        val content = viewMvc.getEditNote().text?.toString().orEmpty()
        val textSizeSp = pxToSp(viewMvc.getEditNote().textSize) // EditText.textSize 是 px
        val role = Util.getSystemProperty(SYS_PROPERTY_ROLE, DEFAULT_ROLE)

        // 將結果回傳給呼叫端（由呼叫端決定如何寫入偏好/資料庫）
        val data = Intent().apply {
            putExtra(EXTRA_NOTE_CONTENT, content)
            putExtra(EXTRA_NOTE_TEXT_SIZE, textSizeSp)
            putExtra(EXTRA_ROLE, role)
        }
        setResult(Activity.RESULT_OK, data)

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
        // 你現有程式碼用到的系統屬性 Key/預設值
        const val SYS_PROPERTY_ROLE = "persist.sys.benq.role"
        const val DEFAULT_ROLE = "default"

        // 給呼叫端使用的 result extras
        const val EXTRA_NOTE_CONTENT = "extra_note_content"
        const val EXTRA_NOTE_TEXT_SIZE = "extra_note_text_size"
        const val EXTRA_ROLE = "extra_role"

        // 啟動 Activity for result 的便捷方法（可選）
        fun createIntent(context: Context): Intent =
            Intent(context, NoteEditActivity::class.java)
    }
}