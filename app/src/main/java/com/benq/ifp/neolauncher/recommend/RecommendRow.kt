package com.benq.ifp.neolauncher.recommend

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.benq.ifp.neolauncher.R
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class RecommendRow(context: Context, attributeSet: AttributeSet?) :
    LinearLayout(context, attributeSet) {

    lateinit var rcRight: RecommendRowItemView
    lateinit var rcLeft: RecommendRowItemView
    lateinit var rcMiddle: RecommendRowItemView
    lateinit var root: View

    private var mLastFocusView: View? = null
    private var isHideView = false

//    init {
//        orientation = HORIZONTAL
//        // ★ 把自己的 layout 塞進來（很關鍵）
//        LayoutInflater.from(context).inflate(R.layout.recommend_row, this, true)
//    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        rcLeft = findViewById(R.id.rrWirelessProjection)
        rcRight = findViewById(R.id.rrWhiteboard)
        rcMiddle = findViewById(R.id.rrFileManager)
        root = findViewById(R.id.llrecommendRow)

        // 綁定點擊事件
        rcLeft.setOnClickListener {
            Log.d(TAG," rcLeft ")
            // 左邊 → 無線投影
            launchApp(INSTANT_SHARE_TWO_EDLA)   // 先嘗試 EDLA
                ?: launchApp(INSTANT_SHARE_TWO) // 再 fallback 到一般版
        }

        rcMiddle.setOnClickListener {
            Log.d(TAG," rcMiddle ")
            // 中間 → 檔案管理
            launchApp(PKG_NAME_AMS)
        }

        rcRight.setOnClickListener {
            Log.d(TAG," rcRight ")
            // 右邊 → 白板 / EZWrite
            launchApp(EZ_WRITE_PKG_NAME6)
        }
    }

    private fun launchApp(mPkg: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(mPkg)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            } ?: return

            if (intent.resolveActivity(context.packageManager) != null) {
                MainScope().launch { context.startActivity(intent) }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "activity not found: pkg = $mPkg", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startActivity(Page: String, action: String = PROP_HIDE_RECOMMENTROW_KEY) {
        val it = Intent(action).apply {
            putExtra(BUNDLE_DATA_KEY_INTENT_PAGE_START, Page)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        MainScope().launch { context.startActivity(it) }
    }

    private fun launchEShare() {
        try {
            val intent =
                context.packageManager.getLaunchIntentForPackage(INSTANT_SHARE_TWO)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                } ?: return
            if (intent.resolveActivity(context.packageManager) != null) {
                MainScope().launch { context.startActivity(intent) }
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "activity not found: pkg = $INSTANT_SHARE_TWO",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

//    fun setFocus(isFocusable: Boolean) {
//        rcRight.isFocusable = isFocusable
//        rcLeft.isFocusable = isFocusable
//        rcMiddle.isFocusable = isFocusable
//    }

    override fun focusSearch(focused: View?, direction: Int): View? {
        //當RecommendRow偵測到往下, 記住目前最後的位置
        if (direction == FOCUS_DOWN && focused != null) {
            mLastFocusView = focused
        }

        return super.focusSearch(focused, direction)
    }

    override fun addFocusables(views: ArrayList<View>?, direction: Int, focusableMode: Int) {

        val lastView = mLastFocusView ?: run {

            super.addFocusables(views, direction, focusableMode)
            return
        }

        if (lastView.isFocusable && direction == FOCUS_UP) {

            //當回到RecommendRow時, 如果有最後離開的view, 指定到最後的view
            views?.add(lastView)
            return
        }

        super.addFocusables(views, direction, focusableMode)
    }


    companion object {
        private const val TAG = "RecommendRow"
        private const val CASTING_PKG_NAME = "com.ecloud.eshare.server"
        const val BUNDLE_DATA_KEY_INTENT_PAGE_START = "com.ifp.unilauncher.intent.bundle.key"
        private const val PROP_HIDE_RECOMMENTROW_KEY = "persist.benq.recommend_off"
        private const val PKG_NAME_AMS = "com.benq.ifp.ams"
        private const val PKG_NAME_BENQ_SUGGEST = "com.benq.store"
        private const val EZ_WRITE_PKG_NAME6 = "com.benq.app.ezwrite" //EZ6
        private const val INSTANT_SHARE_TWO = "com.benq.qshare.host"
        private const val INSTANT_SHARE_TWO_EDLA = "com.benq.qshare.edla"
    }
}