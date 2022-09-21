package com.sega.soni.viewmodel

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.facebook.applinks.AppLinkData
import com.sega.soni.R
import com.sega.soni.Util.BuilderUri
import com.sega.soni.Util.TagSender
import com.sega.soni.activities.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BookViewModel(application: Application) : AndroidViewModel(application) {
    private val sender = TagSender()
    private val builder = BuilderUri()
    private var isAppsStarted: Boolean = false
    private var url = MutableLiveData<String>()

    var urlLiveData: LiveData<String> = url

    fun getDeepLink(activity: Context?) {
        AppLinkData.fetchDeferredAppLinkData(activity) {
            when (it?.targetUri.toString()) {
                "null" -> {
                    viewModelScope.launch {
                        isAppsStarted = checkApps(APPS_STATUS_KEY, activity!!)
                    }
                    if (!isAppsStarted) {
                        startApps(activity!!)
                    }
                }
                else -> {
                    url.postValue(builder.createUrl(it?.targetUri.toString(), null, activity))
                    sender.sendTag(it?.targetUri.toString(), null)
                }
            }
        }
    }

    private fun startApps(activity: Context) {
        AppsFlyerLib.getInstance().init(
            activity.getString(R.string.apps_dev_key),
            object : AppsFlyerConversionListener {
                override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
                    if (!isAppsStarted){
                        url.postValue(builder.createUrl("null", data, activity))
                        sender.sendTag("null", data)
                        viewModelScope.launch {
                            saveAppsStatus(true, activity)
                        }
                    }
                }

                override fun onConversionDataFail(data: String?) {
                    url.postValue(builder.createUrl("null", null, activity))
                    sender.sendTag("null", null)
                    viewModelScope.launch {
                        saveAppsStatus(true, activity)
                    }
                }

                override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {}

                override fun onAttributionFailure(p0: String?) {}
            }, activity
        )
        AppsFlyerLib.getInstance().start(activity)
    }

    suspend fun saveUrlToDataStore(url: String, isSaved: Boolean, context: Context) {
        val strKey = stringPreferencesKey(URL_KEY)
        val boolKey = booleanPreferencesKey(IS_SAVED_KEY)
        context.dataStore.edit {
            it[strKey] = url
            it[boolKey] = isSaved
        }
    }

    suspend fun checkUrl(key: String, context: Context): String? {
        val dataStoreKey = stringPreferencesKey(key)
        return context.dataStore.data.first()[dataStoreKey]
    }

    suspend fun checkIfSaved(key: String, context: Context): Boolean {
        val dataStoreKey = booleanPreferencesKey(key)
        return context.dataStore.data.first()[dataStoreKey] ?: false
    }

    suspend fun saveAppsStatus(isStarted: Boolean, context: Context) {
        val boolKey = booleanPreferencesKey(APPS_STATUS_KEY)
        context.dataStore.edit {
            it[boolKey] = isStarted
        }
    }

    suspend fun checkApps(key: String, context: Context): Boolean {
        val dataStoreKey = booleanPreferencesKey(key)
        return context.dataStore.data.first()[dataStoreKey] ?: false
    }

    companion object {
        const val URL_KEY = "finalUrl"
        const val IS_SAVED_KEY = "isSaved"
        const val APPS_STATUS_KEY = "isStarted"
    }
}