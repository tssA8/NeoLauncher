package com.pt.ifp.neolauncher.clock.settingpage

import android.text.format.DateUtils
import java.util.TimeZone
import kotlin.math.abs

class TimeZoneRow(
    id: String,
    name: String,
    mTime: Long
) : Comparable<TimeZoneRow> {

    val mId: String
    val mLabel: String
    val mGmt: String
    val mLongName: String
    val mOffset: Int

    init {
        val tz = TimeZone.getTimeZone(id)
        mId = id
        mLabel = name
        mOffset = tz.getOffset(mTime)
        mGmt = buildGmtDisplay(mOffset)
        mLongName = tz.displayName
    }

    override fun compareTo(another: TimeZoneRow): Int = mOffset - another.mOffset

    private fun buildGmtDisplay(offsetMillis: Int): String {
        val p = abs(offsetMillis)
        val sign = if (offsetMillis < 0) '-' else '+'
        val hours = p / DateUtils.HOUR_IN_MILLIS
        val minutes = (p / 60000) % 60
        return "GMT$sign$hours:${if (minutes < 10) "0$minutes" else minutes}"
    }
}