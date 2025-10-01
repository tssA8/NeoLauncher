package com.pt.ifp.neolauncher.note

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson

const val SHARED_PREF_USER_PROFILE = "widgetPref"
const val SHARED_PREF_KEY_USER_ROLE_List = "userNoteList"
const val SHARED_PREF_NOTE_TEXT = "widget_note_text"
const val SHARED_PREF_DEFAULT_TEXT_SIZE = 30f

data class NoteData(
        var role: String,
        var content: String?,
        var timestamp: Long,
        var textSize: Float
)

fun getPreviousNoteData(context: Context): String?{
    return PreferenceManager.getDefaultSharedPreferences(context).run {
        getString(SHARED_PREF_NOTE_TEXT, null)
    }
}

fun getSharedPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREF_USER_PROFILE, Context.MODE_PRIVATE)

fun getPrefSting(context: Context, key: String, defaultValue: String? = null): String? {
    return getSharedPreferences(context).getString(key, defaultValue)
}

fun setPrefSting(context: Context, key: String, value: String) {
    getSharedPreferences(context).edit().apply { putString(key, value) }.apply()
}

fun getPrefUsersNote(context: Context, role: String): MutableList<NoteData> {
    val userNoteList = mutableListOf<NoteData>()
    val key = SHARED_PREF_KEY_USER_ROLE_List
    val usersData = getPrefSting(context, key) ?: run {
        //initial db
        Log.d("getPrefUserNoteData 1 ", "create first note data($role)")
        userNoteList.add(NoteData(role, getPreviousNoteData(context), System.currentTimeMillis(), SHARED_PREF_DEFAULT_TEXT_SIZE))
        savePrefUserNoteData(context, userNoteList)
        return userNoteList
    }

    val dataType = object : TypeToken<List<NoteData>>(){}.type
    userNoteList.addAll(Gson().fromJson<List<NoteData>>(usersData, dataType) as MutableList<NoteData>)
    userNoteList.find { it.role == role } ?: run {
        userNoteList.add(NoteData(role, getPreviousNoteData(context), System.currentTimeMillis(), SHARED_PREF_DEFAULT_TEXT_SIZE))
        Log.d("getPrefUser`s NoteData 2 ", "create note data($role)")
    }
    savePrefUserNoteData(context, userNoteList)
    return userNoteList
}

fun savePrefUserNoteData(context: Context, userNoteList: MutableList<NoteData>) {
    Log.d(" NoteEditViewMvc ","savePrefUserNoteData  ")
    val key = SHARED_PREF_KEY_USER_ROLE_List
    setPrefSting(context, key, Gson().toJson(userNoteList))
}