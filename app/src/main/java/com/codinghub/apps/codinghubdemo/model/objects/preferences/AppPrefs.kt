package com.codinghub.apps.codinghubdemo.model.objects.preferences

import android.preference.PreferenceManager
import com.codinghub.apps.codinghubdemo.app.CodingHubDemoApplication

object AppPrefs {

    private const val KEY_SERVICE_URL = "KEY_SERVICE_URL"
    private const val KEY_HEADER_USERNAME = "KEY_HEADER_USERNAME"
    private const val KEY_HEADER_PASSWORD = "KEY_HEADER_PASSWORD"
    private const val KEY_ALPR_SERVICE_URL = "KEY_ALPR_SERVICE_URL"
    private const val KEY_FACE_SASS_SERVICE_URL = "KEY_FACE_SASS_SERVICE_URL"
    private const val KEY_API_KEY = "KEY_API_KEY"

    private fun sharedPrefs() = PreferenceManager.getDefaultSharedPreferences(CodingHubDemoApplication.getAppContext())

    fun getServiceURL(): String? = sharedPrefs().getString(KEY_SERVICE_URL, "https://api.iottechgroup.com/demo/v2/") // http://103.208.27.9:8041  //http://27.254.41.62:8050/
    fun getHeaderUserName(): String? = sharedPrefs().getString(KEY_HEADER_USERNAME, "DEMO")
    fun getHeaderPassword(): String? = sharedPrefs().getString(KEY_HEADER_PASSWORD, "Demon2499")
    fun getApiKey(): String? = sharedPrefs().getString(KEY_API_KEY, "ZH7mDMvggNGGaZ1q3ERhQqPSuYrafq5v")

    fun getOpenALPRServiceURL(): String? = sharedPrefs().getString(KEY_ALPR_SERVICE_URL, "https://api.openalpr.com/v2/")

    fun getFaceSassServiceURL(): String? = sharedPrefs().getString(KEY_FACE_SASS_SERVICE_URL, "http://192.168.1.87:9500/")

}