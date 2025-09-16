package com.benq.ifp.neolauncher.menubar

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.benq.ifp.neolauncher.R
import com.benq.ifp.neolauncher.activity.HomeActivity
import com.benq.ifp.neolauncher.hotseat.Hotseat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MenuBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    // 不要在這裡硬轉型，避免預覽/主題 Context 崩潰
    private var mLauncher: HomeActivity? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)

    //Logout Mode
    lateinit var mShowLoginView: ConstraintLayout
    private lateinit var mUserPicture: CircleImageView
    private lateinit var mName: TextView

    //Login Mode
    lateinit var mBtnLogin: Button
    private lateinit var mMenubarRoot: View
    private lateinit var mShowAccountSettingLoginView: ConstraintLayout
    private lateinit var mUserPictureLogIn: CircleImageView

    @SuppressLint("StaticFieldLeak")
    private lateinit var mLogInName: TextView
    private lateinit var mUserMenubar: View
    private lateinit var mCasting: ImageView
    private lateinit var mEzWrite: ImageView
    private lateinit var mAllApp: ImageView
    private lateinit var mSetting: ImageView
    lateinit var mFlCastingContainer: FrameLayout
    private lateinit var mFlEzWriteContainer: FrameLayout
    lateinit var mFlAllAppContainer: FrameLayout
    private lateinit var mFlSettingContainer: FrameLayout
    private var mVlastFocusView: View? = null
    lateinit var mMenubarConstraintSet: ConstraintLayout

    // 熱座改成可為 null，並在附著/由外部注入時取得
    var hotseat: Hotseat? = null
        private set

    private val TAG = "MenuBar"

    // 讓 Activity 來注入 Host 與 Hotseat（推薦）
    fun bindHost(host: HomeActivity, hotseatView: Hotseat? = null) {
        mLauncher = host
        hotseat = hotseatView ?: rootView.findViewById(R.id.hotseat)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // 嘗試安全取得 Host 與 Hotseat（若尚未用 bindHost）
        if (mLauncher == null) {
            mLauncher = context as? HomeActivity
        }
        if (hotseat == null) {
            hotseat = rootView.findViewById(R.id.hotseat)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 如有註冊 Receiver，記得在這裡反註冊
    }

    private fun initView() {
        mMenubarConstraintSet = findViewById(R.id.menubar_constraint_set_container)
        mUserMenubar = findViewById(R.id.menubar_user)
        mUserPicture = findViewById<View>(R.id.picture) as CircleImageView
        //logout
        mShowLoginView = findViewById<View>(R.id.cl_local_guest_container) as ConstraintLayout
        mName = findViewById<View>(R.id.name) as TextView
        mCasting = findViewById<View>(R.id.casting) as ImageView
        mEzWrite = findViewById<View>(R.id.ezwrite) as ImageView
        mAllApp = findViewById<View>(R.id.allApps) as ImageView
        mSetting = findViewById<View>(R.id.setting) as ImageView
        mFlCastingContainer = findViewById<View>(R.id.fl_casting_container) as FrameLayout
        mFlEzWriteContainer = findViewById<View>(R.id.fl_ezwrite_container) as FrameLayout
        mFlAllAppContainer = findViewById<View>(R.id.fl_allapps_container) as FrameLayout
        mFlSettingContainer = findViewById<View>(R.id.fl_setting_container) as FrameLayout

        // Hotseat 可能不在 MenuBar 的子樹，改用 rootView 尋找（或靠 bindHost 注入）
        hotseat = hotseat ?: rootView.findViewById(R.id.hotseat)

        //login
        mShowAccountSettingLoginView = findViewById<View>(R.id.cl_logIn_container) as ConstraintLayout
        mUserPictureLogIn = findViewById<View>(R.id.picture_local_public) as CircleImageView
        mLogInName = findViewById<View>(R.id.name_local_public) as TextView
        mMenubarRoot = findViewById(R.id.menubar_root)
        val params = mMenubarRoot.layoutParams
        val height = resources.getDimensionPixelSize(R.dimen.menubar_height)
        params.height = height
        mMenubarRoot.layoutParams = params
    }

    private fun setListener() {
        mCasting.setOnClickListener {
            if (DEBUG) Log.i(TAG, "Click casting button")
        }

        mEzWrite.setOnClickListener {
            if (DEBUG) Log.i(TAG, "Click mEzWrite button")
        }

        mSetting.setOnClickListener {
            if (DEBUG) Log.i(TAG, "Click setting button")
        }

        mMenubarRoot.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && ::mBtnLogin.isInitialized) {
                mBtnLogin.requestFocus()
            }
        }

        mAllApp.setOnClickListener {
            if (DEBUG) Log.i(TAG, "Click mAllApp button")
            // 避免空指標
            mLauncher?.showAllApps() ?: (context as? HomeActivity)?.showAllApps()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        initView()
        mShowLoginView.visibility = GONE
        setListener()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        Log.i(TAG, "onInterceptTouchEvent:$ev")
        return false
    }

    fun setUserName(name: String?) {
        if (name != null) {
            mName.text = name
            mLogInName.text = name
        }
    }

    override fun addFocusables(views: ArrayList<View>?, direction: Int, focusableMode: Int) {
        val lastView = mVlastFocusView ?: mShowAccountSettingLoginView
        if (lastView.isFocusable && direction == FOCUS_DOWN) {
            views?.add(lastView)
            return
        }
        super.addFocusables(views, direction, focusableMode)
    }

    override fun focusSearch(focused: View?, direction: Int): View? {
        val focusedView = focused ?: return null
        if (direction == FOCUS_UP) {
            mVlastFocusView = focusedView
        }
        return super.focusSearch(focused, direction)
    }

    companion object {
        private const val DEBUG = true
        private const val TAG = "MenuBar"
    }
}
