package com.pt.ifp.neolauncher.recommend

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.pt.ifp.neolauncher.R


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

        }

        rcMiddle.setOnClickListener {
            Log.d(TAG," rcMiddle ")

        }

        rcRight.setOnClickListener {
            Log.d(TAG," rcRight ")

        }
    }


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
        private const val PROP_HIDE_RECOMMENTROW_KEY = "persist.pt.recommend_off"
    }
}