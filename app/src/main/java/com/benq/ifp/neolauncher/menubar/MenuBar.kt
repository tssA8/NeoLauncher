/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.benq.ifp.neolauncher.menubar

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


class MenuBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)

    //Logout Mode
    lateinit var mShowLoginView: ConstraintLayout
    private lateinit var mUserPicture: CircleImageView
    private lateinit var mName: TextView
    //Login Mode
    //These two are using by Launcher
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

    private val TAG = "MenuBar"

    // Jacky {
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val filter = IntentFilter()
        filter.apply {
            addAction(ACTION_LAUNCHER_SHOW_FORGET_PASSWORD_VIEW)
            addAction(ACTION_LAUNCHER_SHOW_LOGIN_VIEW)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }


    // Jacky }
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

        //login
        mShowAccountSettingLoginView = findViewById<View>(R.id.cl_logIn_container) as ConstraintLayout
        mUserPictureLogIn = findViewById<View>(R.id.picture_local_public) as CircleImageView
        mLogInName = findViewById<View>(R.id.name_local_public) as TextView
        mMenubarRoot = findViewById(R.id.menubar_root)
        val params = mMenubarRoot.layoutParams
        val height = resources.getDimensionPixelSize(R.dimen.menubar_height)
        params.height = height

        mMenubarRoot.setLayoutParams(params)

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
            if (hasFocus) mBtnLogin.requestFocus()
        }

        mAllApp.setOnClickListener {
            if (DEBUG) Log.i(TAG, "Click mAllApp button")

        }

    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        initView()
        mShowLoginView.visibility = GONE
        setListener()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // We don't want any clicks to go through to the hotseat unless the workspace is in
        // the normal state.
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
        private const val ACTION_LAUNCH_UPDATE = "com.benq.qota.action.LAUNCH_UPDATE" //BroadCast
        private const val EZ_WRITE_PKG_NAME6 = "com.benq.app.ezwrite" //EZ6
        private const val AMS_FILES = "com.benq.ifp.ams"
        private const val ACTION_AMS_SAFE_MODE = "com.benq.action.safetymode"
        private const val ACTION_SHOW_HIDE_QUICK_USE = "com.benq.action.refresh_guest_login"
        private const val CONNECTION_KTC_PKG_NAME = "com.benqsetting.connection"
        private const val CONNECTION_SB_PKG_NAME = "com.ifpdos.windowsettings"
        private const val DEVICE_VERSION_NAME_RM03 = "RM03"
        private const val DEMO_VIDEO_ACTION = "com.benq.activity.DemoVideoActivity"
        private const val OPEN_ACCOUNT_SETTING_VIEW = "com.benq.accountsetting.ui"
        private const val INSTANT_SHARE_TWO = "com.benq.qshare.host"
        private const val INSTANT_SHARE_TWO_EDLA = "com.benq.qshare.edla"
        private const val ACTION_LAUNCHER_SHOW_LOGIN_VIEW =
            "com.benq.action.ACTION_LAUNCHER_SHOW_LOGIN_VIEW"
        private const val ACTION_LAUNCHER_SHOW_FORGET_PASSWORD_VIEW =
            "com.benq.launchercs.action.PWDFORGET"
        private const val ACTION_BENQ_SHOW_FLOATING_ALL_APPS = "com.benq.show.floating.all.apps"
        private const val ACTION_BENQ_SHOW_FLOATING_ALL_APPS_LEFT =
            "com.benq.show.floating.all.apps.is.left"
        private const val TIME_INTERVAL = 100
        var IS_DEBUG_SHOW_EDLA_STYLE_GUEST_MENUBAR = true
        private const val SYS_PROP_DMS_ENABLE_GUEST = "persist.sys.benq.enable_guest"
    }

}