package com.pt.ifp.neolauncher.clock.settingpage;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;

import com.google.gson.Gson;
import com.pt.ifp.neolauncher.R;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SettingsManager {
    public static final int CLOCK_TYPE_TIMEZONE3 = 2;
    public static final int DAY_DISPLAY_SHORT = 1;
    public static final String SHARED_PREF_CLOCK_PREFIX = "widget_clock_";
    public static final String SHARED_PREF_NOTE_TEXT = "widget_note_text";
    public static final String SHARED_PREF_NOTE_SIZE = "widget_note_size";
    private static final String SHARED_PREF_MEETING_ROOM_NAME = "widget_meeting_room_name";
    private static final String SHARED_PREF_MEETING_ROOM_EXTENSION = "widget_meeting_room_extension";
    private static final String SHARED_PREF_MEETING_ROOM_SHOW_EXTENSION = "widget_meeting_room_show_extension";
    private static final String SHARED_PREF_MEETING_ROOM_WIFI = "widget_meeting_room_wifi";
    private static final String SHARED_PREF_MEETING_ROOM_SHOW_WIFI = "widget_meeting_room_show_wifi";
    private static final String SHARED_PREF_MEETING_ROOM_PASSWORD = "widget_meeting_room_password";
    private static final String SHARED_PREF_MEETING_ROOM_SHOW_PASSWORD = "widget_meeting_room_show_password";
    public static final String USER_AMS_NO_LOGIN = "ams_guest";
    private static final String TAG = SettingsManager.class.getSimpleName();


    private static SettingsManager sInstance;
    private static Context mContext;
    private static ClockSettings mWidgetSettings;
    private SharedPreferences mPrefs;
    private static String mAccount = "";

    private SettingsManager() {
        if (mContext == null) {
            throw new IllegalStateException("LauncherAppState inited before app context set");
        }
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public static SettingsManager getInstance(Context ctx) {
        if (mContext == null) {
            mContext = ctx;
        }

        if (sInstance == null) {
            synchronized (SettingsManager.class) {
                if (sInstance == null) {
                    sInstance = new SettingsManager();
                }
            }
        }
        return sInstance;
    }

    public void setAccount(String account) {
        mAccount = account == null ? "" : account;
        Log.i(TAG, "setAccount, account:" + account);
        mWidgetSettings = getClockSharedPref();
        Log.i(TAG, "setAccount, mWidgetSettings:" + mWidgetSettings);
    }

    public boolean isAmsNoLogin() {
        Log.i(TAG, "isAmsNoLogin, account:" + mAccount);
        return mAccount.equals(USER_AMS_NO_LOGIN);
    }


    public String getTimeZoneLabel(String tzId) {
        Resources resources = mContext.getResources();
        String[] ids = resources.getStringArray(R.array.timezone_values);
        String[] labels = resources.getStringArray(R.array.timezone_labels);

        String label = "";
        for (int i = 0; i < ids.length; i++) {
            if (ids[i].equals(tzId)) {
                label = labels[i];
                Log.i(TAG, "tzId: " + tzId + ", label:" + label);
            }
        }
        return label;
    }

    public String getTimeZoneDisplay(String tzId) {
        String tzDisplay;
        TimeZone tz;
        try {
            tz = TimeZone.getTimeZone(tzId);
        } catch (Exception e) {
            e.printStackTrace();
            tz = TimeZone.getDefault();
        }
//        TimeZone tz = TimeZone.getTimeZone(tzId);
        tzDisplay = tz.getDisplayName();
        return tzDisplay == null ? "" : tzDisplay;
    }

    public String getCurrentTimeZone() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
                Locale.getDefault());
        Date currentLocalTime = calendar.getTime();
        java.text.DateFormat date = new SimpleDateFormat("Z", Locale.getDefault());
        String localTime = date.format(currentLocalTime);
        StringBuilder  sb = new StringBuilder (localTime);
        sb.insert(3, ":");

        return sb.toString();
    }

    public String getDayDisplay(String tzId, int style) {
        TimeZone tz1;
        try {
            tz1 = TimeZone.getTimeZone(tzId);
        } catch (Exception e) {
            e.printStackTrace();
            tz1 = TimeZone.getDefault();
        }
//        TimeZone tz1 = TimeZone.getTimeZone(tzId);
        TimeZone tzCur = TimeZone.getDefault();
        Long mTime = System.currentTimeMillis();
        int mTz1Offset = tz1.getOffset(mTime);
        int mTzCurOffset = tzCur.getOffset(mTime);
        int mOffset = mTz1Offset - mTzCurOffset;
        Long timeInMillis = mTime + mTz1Offset - mTzCurOffset;

        Calendar compareTime = Calendar.getInstance();
        compareTime.setTimeInMillis(timeInMillis);

        Calendar now = Calendar.getInstance();

        String day;
        if (now.get(Calendar.DATE) == compareTime.get(Calendar.DATE)) {
            day = mContext.getString(R.string.alarm_today);
        } else if (now.get(Calendar.DATE) - compareTime.get(Calendar.DATE) == 1) {
            day = mContext.getString(R.string.yesterday);
        } else {
            day = mContext.getString(R.string.alarm_tomorrow);
        }

        if (style == DAY_DISPLAY_SHORT)
            return day;

        DateUtils.getRelativeTimeSpanString(mOffset, mTime, DateUtils.DAY_IN_MILLIS);
        int p = Math.abs(mOffset);
        StringBuilder name = new StringBuilder(day);
        name.append(", ");
        name.append(mOffset < 0 ? '-' : '+');
        long iHour = p / DateUtils.HOUR_IN_MILLIS;
        double dHour = (double) p / DateUtils.HOUR_IN_MILLIS;
        dHour = (Math.round(dHour * 10) / 10.0);
        Log.i(TAG, "getDayDisplay, ihour:" + iHour);
        Log.i(TAG, "getDayDisplay, dHour:" + dHour);

        String languageCurrent = Locale.getDefault().getDisplayLanguage();
        if (languageCurrent.equals("en")) {
            String hour = getNumberFormattedQuantityString(mContext, R.plurals.hours,
                    iHour == dHour ? (int) iHour : (int) dHour);
            name.append(hour);
        } else {
            int hours = iHour == dHour ? (int) iHour : (int) dHour;
            String mHourLabel = hours + " " + mContext.getString(R.string.hours_label);
            name.append(mHourLabel);
        }
        return name.toString();
    }

    public String getNumberFormattedQuantityString(Context context, int id, int quantity) {
        final String localizedQuantity = NumberFormat.getInstance().format(quantity);
        return context.getResources().getQuantityString(id, quantity, localizedQuantity);
    }

    /***
     * @param context - context used to get time format string resource
     * @param amPmFontSize - size of am/pm label (label removed is size is 0).
     * @return format string for 12 hours mode time
     */
    public CharSequence get12ModeFormat(Context context, int amPmFontSize) {
        String pattern = isJBMR2OrLater()
                ? DateFormat.getBestDateTimePattern(Locale.getDefault(), "hma")
                : context.getString(R.string.time_format_12_mode);

        // Remove the am/pm
        if (amPmFontSize <= 0) {
            pattern.replaceAll("a", "").trim();
        }
        // Replace spaces with "Hair Space"
        pattern = pattern.replaceAll(" ", "\u200A");
        // Build a spannable so that the am/pm will be formatted
        int amPmPos = pattern.indexOf('a');
        if (amPmPos == -1) {
            return pattern;
        }
        Spannable sp = new SpannableString(pattern);
        sp.setSpan(new StyleSpan(Typeface.NORMAL), amPmPos, amPmPos + 1,
                Spannable.SPAN_POINT_MARK);
        sp.setSpan(new AbsoluteSizeSpan(amPmFontSize), amPmPos, amPmPos + 1,
                Spannable.SPAN_POINT_MARK);
        sp.setSpan(new TypefaceSpan("sans-serif"), amPmPos, amPmPos + 1,
                Spannable.SPAN_POINT_MARK);
        return sp;
    }

    public CharSequence get24ModeFormat() {
        return isJBMR2OrLater()
                ? DateFormat.getBestDateTimePattern(Locale.getDefault(), "Hm")
                : (new SimpleDateFormat("k:mm", Locale.getDefault())).toLocalizedPattern();
    }

    /**
     * @return {@code true} if the device is {@link Build.VERSION_CODES#JELLY_BEAN_MR2} or later
     */
    private boolean isJBMR2OrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    public void setClockSettings(ClockSettings cs) {
        Log.i(TAG, "setClockSettings");
        mWidgetSettings = cs;
        Log.i(TAG, "setClockSettings, putClockSharedPref");
        putClockSharedPref();
    }

    public ClockSettings getClockSettings() {
        Log.i(TAG, "getClockSettings");
        // TODO: 2018/10/19 若系統有切語言的話，必須要有CallBack，才能重新load到正確的ClockInfo顯示正確的語言
        if (mWidgetSettings == null) {
            Log.i(TAG, "getClockSettings, getClockSharedPref");
            mWidgetSettings = getClockSharedPref();
        }

        if (mWidgetSettings == null) {
            Log.i(TAG, "getClockSettings, getDefaultClockSettings");
            mWidgetSettings = getDefaultClockSettings();
        }

        ClockSettings mClock = mWidgetSettings;
//        String timezoneOneId = mClock.mCity1TimezoneID;
//        String timezoneTwoId = mClock.mCity2TimezoneID;
//        List<TimeZoneRow> timeZoneRowList = getAllTimezoneList();
//        for (TimeZoneRow tmz : timeZoneRowList) {
//            if (tmz.mId.equals(timezoneOneId)) {
////                Log.d(TAG,"ccc_ find time 1 label :  "+tmz.mLabel);
//                mClock.mCity1TimezoneID = tmz.mId;
//                mClock.mCity1Gmt = tmz.mGmt;
//                mClock.mCity1Label = tmz.mLabel;
//                mClock.mCity1LongName = tmz.mLongName;
//                mClock.mCity1Name = tmz.mLabel;
//            }
//            if (tmz.mId.equals(timezoneTwoId)) {
////                Log.d(TAG,"ccc_ find time 2 label :  "+tmz.mLabel);
//                mClock.mCity2TimezoneID = tmz.mId;
//                mClock.mCity2Gmt = tmz.mGmt;
//                mClock.mCity2Label = tmz.mLabel;
//                mClock.mCity2LongName = tmz.mLongName;
//                mClock.mCity2Name = tmz.mLabel;
//            }
//        }
        return mClock;
    }

    private void putClockSharedPref() {
        Log.i(TAG, "putClockSharedPref, account:" + mAccount);
        if (TextUtils.isEmpty(mAccount)) {
            Log.e(TAG, "save share preference, but account is empty");
            mAccount = "ams_guest";
        }
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mWidgetSettings);
        prefsEditor.putString(SHARED_PREF_CLOCK_PREFIX + mAccount, json);
        prefsEditor.apply();
    }

    private ClockSettings getClockSharedPref() {
        Log.i(TAG, "getClockSharedPref, account:" + mAccount);
        if (TextUtils.isEmpty(mAccount)) {
            mAccount = "ams_guest";
        }
        Gson gson = new Gson();
        String json = mPrefs.getString(SHARED_PREF_CLOCK_PREFIX + mAccount, "");
        ClockSettings mClockSettings = gson.fromJson(json, ClockSettings.class);
        return mClockSettings;
    }

    private ClockSettings getDefaultClockSettings() {
        return convertDefaultClockSettingData();
    }

    private ClockSettings convertDefaultClockSettingData() {
        List<TimeZoneRow> timeZoneRowList = getAllTimezoneList();
        ClockSettings mClock = new ClockSettings();
        if (timeZoneRowList.size() > 0) {
            //city1 get Default 0
            TimeZoneRow tmrC1 = timeZoneRowList.get(0);
            mClock.mCity1TimezoneID = tmrC1.getMId();
            mClock.mCity1Gmt = tmrC1.getMGmt();
            mClock.mCity1Label = tmrC1.getMLabel();
            mClock.mCity1LongName = tmrC1.getMLongName();
            mClock.mCity1Name = tmrC1.getMLongName();
            Log.d(TAG,"AAA_mCity1Name : "+mClock.mCity1Name+" mClock.mCity1Label : "+mClock.mCity1Label+"tmrC1.mLongName : "+tmrC1.getMLongName());
            //city2 get Default 1
            TimeZoneRow tmrC2 = timeZoneRowList.get(1);
            mClock.mCity2TimezoneID = tmrC2.getMId();
            mClock.mCity2Gmt = tmrC2.getMGmt();
            mClock.mCity2Label = tmrC2.getMLabel();
            mClock.mCity2LongName = tmrC2.getMLongName();
            mClock.mCity2Name = tmrC2.getMLongName();
            mClock.mClockType = -1;
            mClock.mLockClockType = CLOCK_TYPE_TIMEZONE3;
            Log.d(TAG,"AAA_mCity2Name : "+mClock.mCity2Name+" mClock.mCity2Label : "+mClock.mCity2Label+" tmrC2.mLongName : "+tmrC2.getMLongName());
        } else {
            Log.d(TAG, "TimeZone List is null , using default data");
            //city1
            mClock.mCity1TimezoneID = "Asia/Taipei";
            mClock.mCity1Gmt = "GMT+8:00";
            mClock.mCity1Label = "Taipei";
            mClock.mCity1LongName = "";
            mClock.mCity1Name = mClock.mCity1Label;
            //city2
            mClock.mCity2TimezoneID = "Asia/Taipei";
            mClock.mCity2Gmt = "GMT+8:00";
            mClock.mCity2Label = "Taipei";
            mClock.mCity2LongName = "";
            mClock.mCity2Name = mClock.mCity2Label;
            mClock.mClockType = -1;
            mClock.mLockClockType = CLOCK_TYPE_TIMEZONE3;
        }
        return mClock;
    }


    public static class ClockSettings {
        public String mCity1TimezoneID;
        public String mCity1Label;
        public String mCity1LongName;
        public String mCity1Gmt;
        public String mCity1Name;

        public String mCity2TimezoneID;
        public String mCity2Label;
        public String mCity2LongName;
        public String mCity2Gmt;
        public String mCity2Name;

        public int mClockType;
        public int mLockClockType;
    }

    public String getNoteText() {
        return mPrefs.getString(SHARED_PREF_NOTE_TEXT, "");
    }

    public int getNoteSize() {
        return mPrefs.getInt(SHARED_PREF_NOTE_SIZE, 20);
    }

    public void setNote(String nt) {
        if (nt != null) {
            SharedPreferences.Editor prefsEditor = mPrefs.edit();
            prefsEditor.putString(SHARED_PREF_NOTE_TEXT, nt);
            prefsEditor.putInt(SHARED_PREF_NOTE_SIZE, nt.length());
            prefsEditor.apply();
        }
    }

    private List<TimeZoneRow> getAllTimezoneList() {
        Resources resources = mContext.getResources();
        String[] ids = resources.getStringArray(R.array.timezone_values);
        String[] labels = resources.getStringArray(R.array.timezone_labels);
        int minLength = ids.length;
        if (ids.length != labels.length) {
            minLength = Math.min(minLength, labels.length);
            Log.e(TAG, "Timezone ids and labels have different length!");
        }
        List<TimeZoneRow> timezones = new ArrayList<>();
        for (int i = 0; i < minLength; i++) {
            timezones.add(new TimeZoneRow(ids[i], labels[i], System.currentTimeMillis()));
        }
        Collections.sort(timezones);
        return timezones;
    }

    public void setMeetingRoomInfo(String name, String extension, boolean showExtension,
                                   String wifi, boolean showWifi, String password, boolean showPassword) {
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        prefsEditor.putString(SHARED_PREF_MEETING_ROOM_NAME, name);
        prefsEditor.putString(SHARED_PREF_MEETING_ROOM_EXTENSION, extension);
        prefsEditor.putBoolean(SHARED_PREF_MEETING_ROOM_SHOW_EXTENSION, showExtension);
        prefsEditor.putString(SHARED_PREF_MEETING_ROOM_WIFI, wifi);
        prefsEditor.putBoolean(SHARED_PREF_MEETING_ROOM_SHOW_WIFI, showWifi);
        prefsEditor.putString(SHARED_PREF_MEETING_ROOM_PASSWORD, password);
        prefsEditor.putBoolean(SHARED_PREF_MEETING_ROOM_SHOW_PASSWORD, showPassword);
        prefsEditor.apply();
    }

    public void setMeetingRoomInfoName(String name) {
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        prefsEditor.putString(SHARED_PREF_MEETING_ROOM_NAME, name);
        prefsEditor.apply();
    }

    public String getMeetingRoomName() {
        String res = mPrefs.getString(SHARED_PREF_MEETING_ROOM_NAME, "Meeting Room");
        if(res.isEmpty()){
            res = "Meeting Room";
        }
        return res;
    }

    public String getMeetingRoomExtension() {
        return mPrefs.getString(SHARED_PREF_MEETING_ROOM_EXTENSION, "");
    }

    public Boolean getMeetingRoomShowExtension() {
        return mPrefs.getBoolean(SHARED_PREF_MEETING_ROOM_SHOW_EXTENSION, false);
    }

    public String getMeetingRoomWifi() {
        return mPrefs.getString(SHARED_PREF_MEETING_ROOM_WIFI, "");
    }

    public Boolean getMeetingRoomShowWifi() {
        return mPrefs.getBoolean(SHARED_PREF_MEETING_ROOM_SHOW_WIFI, false);
    }

    public String getMeetingRoomPassword() {
        return mPrefs.getString(SHARED_PREF_MEETING_ROOM_PASSWORD, "");
    }

    public Boolean getMeetingRoomShowPassword() {
        return mPrefs.getBoolean(SHARED_PREF_MEETING_ROOM_SHOW_PASSWORD, false);
    }
}