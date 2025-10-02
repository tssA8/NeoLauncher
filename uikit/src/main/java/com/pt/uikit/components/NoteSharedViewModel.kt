// NoteSharedViewModel.kt
package com.pt.uikit.components

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class NoteSharedViewModel : ViewModel() {
    var text by mutableStateOf("點我開啟編輯")
        private set

    var sizeSp by mutableStateOf(30f)
        private set

    fun update(text: String? = null, sizeSp: Float? = null) {
        text?.let { this.text = it }
        sizeSp?.let { this.sizeSp = it }
    }
}
