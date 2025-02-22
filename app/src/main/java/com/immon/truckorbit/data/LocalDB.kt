package com.immon.truckorbit.data

import android.content.Context
import android.content.SharedPreferences
import com.immon.truckorbit.BuildConfig
import com.immon.truckorbit.TruckOrbit

@Suppress("unused")
object LocalDB {
    private var prefs: SharedPreferences = TruckOrbit
        .getAppContext()
        .createDeviceProtectedStorageContext()
        .getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

    private var editor: SharedPreferences.Editor = prefs.edit()

    fun putBoolean(key: String?, `val`: Boolean) {
        editor.putBoolean(key, `val`).apply()
    }

    fun putInt(key: String?, `val`: Int) {
        editor.putInt(key, `val`).apply()
    }

    fun putLong(key: String?, `val`: Long) {
        editor.putLong(key, `val`).apply()
    }

    fun putFloat(key: String?, `val`: Float) {
        editor.putFloat(key, `val`).apply()
    }

    fun putString(key: String?, `val`: String?) {
        editor.putString(key, `val`).apply()
    }

    fun getBoolean(key: String?): Boolean {
        return prefs.getBoolean(key, false)
    }

    fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return prefs.getBoolean(key, defValue)
    }

    fun getInt(key: String?): Int {
        return prefs.getInt(key, 0)
    }

    fun getInt(key: String?, defValue: Int): Int {
        return prefs.getInt(key, defValue)
    }

    fun getLong(key: String?): Long {
        return prefs.getLong(key, 0)
    }

    fun getLong(key: String?, defValue: Long): Long {
        return prefs.getLong(key, defValue)
    }

    fun getFloat(key: String?): Float {
        return prefs.getFloat(key, 0f)
    }

    fun getFloat(key: String?, defValue: Float): Float {
        return prefs.getFloat(key, defValue)
    }

    fun getString(key: String?): String? {
        return prefs.getString(key, null)
    }

    fun getString(key: String?, defValue: String?): String? {
        return prefs.getString(key, defValue)
    }

    fun clearPref(key: String?) {
        editor.remove(key).apply()
    }

    fun clearPrefs(vararg keys: String?) {
        for (key in keys) {
            editor.remove(key).apply()
        }
    }

    fun clearAllPrefs() {
        editor.clear().apply()
    }
}
