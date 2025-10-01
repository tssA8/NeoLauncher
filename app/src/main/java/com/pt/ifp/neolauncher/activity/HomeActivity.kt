package com.pt.ifp.neolauncher.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.pt.ifp.neolauncher.DeviceProfile
import com.pt.ifp.neolauncher.R
import com.pt.ifp.neolauncher.SearchBarComponentView.GoogleSearchBar
import com.pt.ifp.neolauncher.SearchBarComponentView.GoogleSearchBarWithHistory
import com.pt.ifp.neolauncher.SearchBarComponentView.SearchBarComponent
import com.pt.ifp.neolauncher.SearchBarComponentView.SearchBarComponent.OnSearchBarClickListener
import com.pt.ifp.neolauncher.app.NeoLauncherApp
import com.pt.ifp.neolauncher.graphics.ToolbarBackground
import com.pt.ifp.neolauncher.menubar.CanvasMenuBarCompose
import com.pt.ifp.neolauncher.note.NoteEditActivity
import com.pt.ifp.neolauncher.note.NoteWidget
import com.pt.ifp.neolauncher.preference.Preferences
import com.pt.ifp.neolauncher.view.SoftKeyboard
import com.pt.ifp.neolauncher.view.SystemBars
import com.pt.ifp.neolauncher.widget.AppPieView
import com.pt.ifp.neolauncher.widget.AppPieView.ListListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.dimensionResource

class HomeActivity : ComponentActivity() {
    private lateinit var prefs: Preferences
    private lateinit var kb: SoftKeyboard
    private lateinit var gestureDetector: GestureDetector
    private lateinit var toolbarBackground: ToolbarBackground
    private lateinit var pieView: AppPieView

    private lateinit var searchBarComponent: SearchBarComponent
    private lateinit var searchInput: EditText
    private lateinit var prefsButton: ImageView
    private var updateAfterTextChange = true
    private var showAllAppsOnResume = false
    private var immersiveMode = Preferences.IMMERSIVE_MODE_DISABLED
    private var pausedAt = 0L
    private var recommendRow: RelativeLayout? = null

    private var mDeviceProfile: DeviceProfile? = null

    private lateinit var googleSearchView: ComposeView

    private lateinit var noteComposeView: ComposeView

    private val showHistoryState = mutableStateOf(false) // 👈 Activity 層級持有


    override fun onBackPressed() {
        super.onBackPressed()
        if (pieView.inEditMode()) {
            pieView.endEditMode()
            showAllApps()
        } else {
            hideAllApps()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev == null) return false
        if (pieView.inListMode() && gestureDetector.onTouchEvent(ev)) {
            return true
        }
        return try {
            super.dispatchTouchEvent(ev)
        } catch (e: IllegalStateException) {
            false
        }
    }

    val deviceProfile: DeviceProfile
        get() {
            if (mDeviceProfile == null) {
                mDeviceProfile = DeviceProfile(this)
            }
            return mDeviceProfile!!
        }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        prefs = NeoLauncherApp.getPrefs(this)
        kb = SoftKeyboard(this)
        gestureDetector = GestureDetector(
            this,
            FlingListener(ViewConfiguration.get(this).scaledMinimumFlingVelocity)
        )

        setContentView(R.layout.home_activity)

        // ✅ 這裡的型別參數改成非空
        recommendRow = findViewById<RelativeLayout>(R.id.llVariantContainer)

        val menuBar = findViewById<ComposeView>(R.id.menubarcompose)
        menuBar.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        menuBar.setContent {
            MaterialTheme {
                CanvasMenuBarCompose (
                    userName   = "admin",
                    onCasting  = {  },
                    onSettings = { showPreferences() },
                    onAllApps  = { showAllApps() },
                    onHelp     = { }
                )
            }
        }


        googleSearchView = findViewById<ComposeView>(R.id.googlesearchcompose)
        googleSearchView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        googleSearchView.setContent {
//            MaterialTheme {
//                GoogleSearchBar()  // 這就是我們剛剛寫的客製化 SearchBar
//            }

            MaterialTheme {
                GoogleSearchBarWithHistory(
                    showHistory = showHistoryState.value,
                    onDismissHistory = { showHistoryState.value = false },
                    onShowHistory = { showHistoryState.value = true }
                )
            }
        }

        noteComposeView = findViewById<ComposeView>(R.id.notecompose)
        noteComposeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        noteComposeView.setContent {
            // 這裡是 Composable 環境
            val defaultSizeSp = dimensionResource(id = R.dimen.note_text_size).value

            var noteText by rememberSaveable { mutableStateOf("點我開啟編輯") }
            var noteSizeSp by rememberSaveable { mutableStateOf(defaultSizeSp) }

            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { res ->
                if (res.resultCode == Activity.RESULT_OK) {
                    noteText = res.data?.getStringExtra(NoteEditActivity.EXTRA_NOTE_CONTENT).orEmpty()
                    res.data?.getFloatExtra(NoteEditActivity.EXTRA_NOTE_TEXT_SIZE, Float.NaN)
                        ?.takeIf { !it.isNaN() }
                        ?.let { noteSizeSp = it }
                }
            }

            NoteWidget(
                text = noteText,
                fontSizeSp = noteSizeSp,
                onClick = {
                    val intent = Intent(this, NoteEditActivity::class.java).apply {
                        putExtra(NoteEditActivity.EXTRA_NOTE_CONTENT, noteText) // 可選：預填
                    }
                    launcher.launch(intent)
                }
            )
        }


        if (!PreferencesActivity.isReady(this)) {
            PreferencesActivity.startWelcome(this)
        }

        toolbarBackground = ToolbarBackground(resources)
        pieView = findViewById(R.id.pie)
        pieView.setWindow(window) // ✅ 留這裡那一個就好
        pieView.setActivity(this)
        pieView.isVerticalScrollBarEnabled = false
        pieView.isHorizontalScrollBarEnabled = false
        pieView.setOnClickListener {
            showHistoryState.value = false   // ✅ 只改狀態，不要重建 setContent
        }

        // 啟用 immersive，隱藏底部導航列
        AppPieView.enableImmersive(window)

        searchBarComponent = findViewById(R.id.ll_search)
        searchBarComponent.setOnSearchBarClickListener(object : OnSearchBarClickListener {
            override fun onSearchBarClicked(text: String?) {}
            override fun onSearchBarEmpty() { Log.d(TAG, "onSearchBarEmpty") }
            override fun onSearchBarChangeListener(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        searchInput = searchBarComponent.searchEditText
        prefsButton = findViewById(R.id.preferences)

        initPieView()
        initSearchInput()

        SystemBars.listenForWindowInsets(pieView) { left, _, right, bottom ->
            // list 顯示在 status bar 下方 → top 一律 0
            pieView.setPadding(left, 0, right, bottom)
        }

        immersiveMode = prefs.immersiveMode
        SystemBars.setTransparentSystemBars(window, immersiveMode)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean { // ✅ 改非空
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.preferences) {
            showPreferences()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent != null &&
            (intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                isGestureNavigationEnabled
            ) {
                finish()
                return
            }
            if (pieView.inEditMode()) {
                pieView.endEditMode()
            } else if (!isSearchVisible &&
                !isGestureNavigationEnabled &&
                System.currentTimeMillis() - pausedAt < 100
            ) {
                showAllAppsOnResume = true
            }
        }
    }

    public override fun onRestart() {
        super.onRestart()
        if (prefs.forceRelaunch()) finish()
    }

    override fun onStart() {
        super.onStart()
        requestedOrientation = prefs.orientation
    }

    override fun onResume() {
        super.onResume()
        updatePrefsButton()
        updateSystemBars()
        if (showAllAppsOnResume) {
            showAllApps()
            showAllAppsOnResume = false
        } else {
            hideAllApps()
        }
    }

    override fun onPause() {
        super.onPause()
        pausedAt = System.currentTimeMillis()
    }

    private fun initPieView() {
        pieView.setListListener(object : ListListener {
            override fun onOpenList(resume: Boolean) {
                showAllAppsOnResume = resume
                Log.d("onOpenList ", "AAA_onOpenList")
                showAllApps()
            }
            override fun onHideList() { hideAllApps() }
            override fun onScrollList(y: Int, isScrolling: Boolean) {
                if (isScrolling && y != 0) hideKeyboadAndPrefsButton()
            }
            override fun onDragDown(alpha: Float) {
                hideKeyboadAndPrefsButton()
                searchInput.setBackgroundColor(0)
                setAlpha(searchInput, alpha)
            }
        })

        NeoLauncherApp.appMenu.setUpdateListener {
            searchInput.text.clear()
            updateAppList()
        }
    }

    override fun onStop() {
        pieView.setHotseatDropTarget(null, null)
        super.onStop()
    }

    private fun initSearchInput() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(e: Editable) {
                if (!updateAfterTextChange) return
                else if (e.isNotEmpty()) hidePrefsButton()

                val s = e.toString()
                when (s) {
                    ".." -> {
                        e.clear(); showPreferences(); return
                    }
                    ",," -> {
                        e.clear(); showEditor(); return
                    }
                }
                if (endsWithDoubleSpace(e)) {
                    pieView.launchSelectedAppFromList()
                }
                updateAppList()
                if (prefs.autoLaunchMatching() && pieView.iconCount == 1) {
                    pieView.launchSelectedAppFromList()
                }
            }
        })
        searchInput.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            when (actionId) {
                EditorInfo.IME_ACTION_GO,
                EditorInfo.IME_ACTION_SEND,
                EditorInfo.IME_ACTION_DONE,
                EditorInfo.IME_ACTION_NEXT,
                EditorInfo.IME_ACTION_SEARCH,
                EditorInfo.IME_NULL -> {
                    if (searchInput.text.isNotEmpty()) {
                        pieView.launchSelectedAppFromList()
                    }
                    hideAllApps()
                    true
                }
                else -> false
            }
        }
    }

    private fun endsWithDoubleSpace(e: Editable): Boolean {
        val doubleSpaceLaunch = prefs.doubleSpaceLaunch()
        val s = e.toString()
        if ((doubleSpaceLaunch && s.endsWith("  ")) || s.endsWith(". ")) {
            updateAfterTextChange = false
            e.clear()
            e.append(s.substring(0, s.length - 2))
            if (!doubleSpaceLaunch) e.append("  ")
            updateAfterTextChange = true
            return doubleSpaceLaunch
        }
        return false
    }

    private fun updatePrefsButton() {
        if (prefs.iconPress == Preferences.ICON_PRESS_MENU) {
            prefsButton.setImageResource(R.drawable.ic_edit)
            prefsButton.setOnClickListener { showEditor() }
            prefsButton.setOnLongClickListener {
                showPreferences(); true
            }
        } else {
            prefsButton.setImageResource(R.drawable.ic_preferences)
            prefsButton.setOnClickListener { showPreferences() }
            prefsButton.setOnLongClickListener {
                showEditor(); true
            }
        }
    }

    private fun updateSystemBars() {
        val newImmersiveMode = prefs.immersiveMode
        if (immersiveMode != newImmersiveMode) {
            immersiveMode = newImmersiveMode
            SystemBars.setSystemUIVisibility(window, immersiveMode)
        }
    }

    private fun showPreferences() {
        hideAllApps()
        PreferencesActivity.start(this)
        showAllAppsOnResume = true
    }

    private fun showEditor() {
        hideAllApps()
        pieView.showEditor()
    }

    private fun hidePrefsButton() {
        if (prefsButton.visibility == View.VISIBLE) {
            prefsButton.visibility = View.GONE
        }
    }

    fun showAllApps() {
        recommendRow?.visibility = View.GONE
        googleSearchView?.visibility = View.GONE
        if (isSearchVisible) return

        searchBarComponent.visibility = View.VISIBLE
        searchInput.visibility = View.VISIBLE
        prefsButton.visibility = View.VISIBLE
        setAlpha(searchInput, 1f)
        if (prefs.displayKeyboard()) kb.showFor(searchInput)

        val editable = searchInput.text
        val searchWasEmpty = editable.toString().isEmpty()
        updateAfterTextChange = false
        editable.clear()
        updateAfterTextChange = true

        if (!searchWasEmpty || pieView.isEmpty()) {
            updateAppList()
        }
        pieView.showList()
    }

    private fun setAlpha(view: View, alpha: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            view.alpha = alpha
        }
    }

    private fun hideAllApps() {
        recommendRow?.visibility = View.VISIBLE
        googleSearchView?.visibility = View.VISIBLE
        if (isSearchVisible) {
            searchBarComponent.visibility = View.GONE
            searchInput.visibility = View.GONE
            hideKeyboadAndPrefsButton()
        }
        pieView.hideList()
    }

    private fun hideKeyboadAndPrefsButton() {
        kb.hideFrom(searchInput)
        hidePrefsButton()
    }

    private val isSearchVisible: Boolean
        get() = searchInput.visibility == View.VISIBLE

    private val isGestureNavigationEnabled: Boolean
        get() = Settings.Secure.getInt(contentResolver, "navigation_mode", 0) == 2

    private fun updateAppList() {
        pieView.filterAppList(searchInput.text.toString())
    }

    private inner class FlingListener(private val minimumVelocity: Int) :
        GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (pieView.appListNotScrolled() && kotlin.math.abs(distanceY) > kotlin.math.abs(distanceX)) {
                pieView.dragDownListBy(distanceY)
            }
            return false
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (pieView.appListNotScrolled()
                && velocityY > velocityX
                && velocityY >= minimumVelocity
                && e1 != null && e2 != null
                && e2.y - e1.y > 0
            ) {
                pieView.resetScroll()
                hideAllApps()
                return true
            }
            return false
        }
    }

    companion object {
        private const val TAG = "HomeActivity"
    }
}
