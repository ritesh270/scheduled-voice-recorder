package com.example.voicerecorder

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri

object StorageHelper {
    private const val PREF = "vr_prefs"
    private const val KEY_URI = "dir_uri"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_H = "hour"
    private const val KEY_M = "minute"

    fun saveDirectoryUri(ctx: Context, uri: String) {
        val p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        p.edit().putString(KEY_URI, uri).apply()
    }
    fun getDirectoryUri(ctx: Context): String? {
        val p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return p.getString(KEY_URI, null)
    }
    fun saveEnabled(ctx: Context, e: Boolean) {
        val p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        p.edit().putBoolean(KEY_ENABLED, e).apply()
    }
    fun isEnabled(ctx: Context): Boolean {
        val p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return p.getBoolean(KEY_ENABLED, false)
    }
    fun saveScheduleTime(ctx: Context, h: Int, m: Int) {
        val p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        p.edit().putInt(KEY_H, h).putInt(KEY_M, m).apply()
    }
    fun getScheduleTime(ctx: Context): Pair<Int,Int> {
        val p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val h = p.getInt(KEY_H, -1)
        val m = p.getInt(KEY_M, -1)
        return Pair(h,m)
    }
}
