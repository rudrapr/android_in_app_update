package dev.rudrax.android_in_app_update

import android.app.Activity
import android.content.Context

class PreferenceManager {

    companion object {

        private var REQUEST_CODE_LAST_UPDATE = "last_triggered_update"
        private var PREF_NAME = "AndroidInAppUpdatePlugin_pref"

        fun saveLastUpdateType(activity: Activity, appUpdateType: Int) {
            val prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putInt(REQUEST_CODE_LAST_UPDATE, appUpdateType).apply()
        }

        fun readLastUpdateType(activity: Activity): Int {
            val prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return prefs.getInt(REQUEST_CODE_LAST_UPDATE, -1)
        }

        fun clearUpdateType(activity: Activity) {
            val prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
        }
    }
}