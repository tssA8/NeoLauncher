package com.pt.ifp.neolauncher.searchbarcomponentView

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.FocusFinder
import android.view.KeyEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import com.pt.ifp.neolauncher.R
import kotlinx.coroutines.*
import kotlin.properties.Delegates


@SuppressLint("AppCompatCustomView")
open class EditTextFocusShift(context: Context, attrs: AttributeSet?) : EditText(context, attrs),
        OnFocusChangeListener, OnEditorActionListener {
    private val className = this.javaClass.simpleName
    private var isFocusShiftDown by Delegates.notNull<Boolean>()
    private var isFocusShiftUp by Delegates.notNull<Boolean>()
    private var isFocusShiftRight by Delegates.notNull<Boolean>()
    private var isFocusShiftLeft by Delegates.notNull<Boolean>()

    private var scope: CoroutineScope? = null

    init {
        if (attrs != null) {
            val attributes = context.theme.obtainStyledAttributes(
                    attrs,
                    R.styleable.EditTextFocusShift,
                    0, 0
            )

            //取得預設值
            isFocusShiftDown =
                    attributes.getBoolean(R.styleable.EditTextFocusShift_focusShiftDown, false)
            isFocusShiftUp =
                    attributes.getBoolean(R.styleable.EditTextFocusShift_focusShiftUp, false)
            isFocusShiftRight =
                    attributes.getBoolean(R.styleable.EditTextFocusShift_focusShiftRight, false)
            isFocusShiftLeft =
                    attributes.getBoolean(R.styleable.EditTextFocusShift_focusShiftLeft, false)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        onFocusChangeListener = this
        setOnEditorActionListener(this)
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scope?.cancel()
        scope = null
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (isDirectKeyCode(keyCode)) {

            var direction = FOCUS_DOWN
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> direction = FOCUS_UP
                KeyEvent.KEYCODE_DPAD_DOWN -> direction = FOCUS_DOWN
//                KeyEvent.KEYCODE_DPAD_DOWN_LEFT -> direction = View.FOCUS_LEFT
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    Log.d(
                            className,
                            "getSelectionEnd():" + selectionEnd + "   getText().length():" + text.length
                    )
                    if (selectionEnd >= text.length) {
                        direction = FOCUS_RIGHT
                    } else {
                        Log.e(className, "setSelection:" + (selectionEnd + 1))
                        setSelection(selectionEnd + 1)
                        return true
                    }
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    Log.d("KEYCODE_DPAD_LEFT", "selectionStart :" + selectionStart)
                    if (selectionStart == 0) {
                        direction = FOCUS_LEFT
                    } else {
                        Log.e(className, "setSelection:" + (selectionEnd - 1))
                        setSelection(selectionEnd - 1)
                        return true
                    }
                }
            }
            Log.d(className, "isDirectKeyCode direction:" + FOCUS_RIGHT)
            val nextFocus =
                    FocusFinder.getInstance().findNextFocus(rootView as ViewGroup, this, direction)
            if (nextFocus != null) {
                nextFocus.requestFocus()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun isDirectKeyCode(keyCode: Int): Boolean {
        return (keyCode == KeyEvent.KEYCODE_DPAD_UP && isFocusShiftUp) ||
                (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && isFocusShiftDown) ||
                (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && isFocusShiftRight) ||
                (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && isFocusShiftLeft)
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (hasFocus) {
            popupInputMethodWindow(v)
        } else {
            hideKeyboard(v)
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_NEXT ||
                actionId == EditorInfo.IME_ACTION_SEARCH ||
                event?.keyCode == KeyEvent.KEYCODE_ENTER
        ) {
            v?.let { hideKeyboard(it) }
            return true
        }
        return false
    }

    private fun popupInputMethodWindow(v: View) {
        scope?.launch {

            delay(200)
            Log.d(className, "popupInputMethodWindow")
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun hideKeyboard(v: View) {
        Log.d(className, "hideKeyboard")
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(v.windowToken, 0)
    }
}