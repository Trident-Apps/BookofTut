package com.sega.soni.activities


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.sega.soni.R
import com.sega.soni.databinding.WebActivityBinding
import com.sega.soni.viewmodel.BookViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WebActivity : AppCompatActivity() {

    lateinit var myViewModel: BookViewModel
    private lateinit var binding: WebActivityBinding
    private lateinit var webView: WebView
    private var isRedirected: Boolean = true
    private var messageAb: ValueCallback<Array<Uri?>>? = null
    private var isUrlSaved: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this, R.layout.web_activity
        )
        myViewModel = ViewModelProvider(this)[BookViewModel::class.java]
        webView = binding.webView
        webView.loadUrl(intent.getStringExtra(EXTRA_NAME) ?: "url not passed")
        webView.webViewClient = LocalClient()
        webView.settings.userAgentString = System.getProperty(USER_AGENT)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadWithOverviewMode = false
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri?>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                messageAb = filePathCallback
                selectImageIfNeed()
                return true
            }

            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                val newWeb = WebView(this@WebActivity)
                newWeb.webChromeClient = this
                newWeb.settings.javaScriptEnabled = true
                newWeb.settings.javaScriptCanOpenWindowsAutomatically = true
                newWeb.settings.domStorageEnabled = true
                newWeb.settings.setSupportMultipleWindows(true)
                val transport = resultMsg?.obj as WebView.WebViewTransport
                transport.webView = newWeb
                resultMsg.sendToTarget()
                newWeb.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        view?.loadUrl(url ?: "")
                        isRedirected = true
                        return true
                    }
                }
                return true
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        }
    }

    private fun selectImageIfNeed() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = INTENT_TYPE
        startActivityForResult(
            Intent.createChooser(intent, CHOOSER_TITLE), RESULT_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            messageAb?.onReceiveValue(null)
            return
        } else if (resultCode == Activity.RESULT_OK) {
            if (messageAb == null) return

            messageAb!!.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(
                    resultCode,
                    data
                )
            )
            messageAb = null
        }
    }

    private inner class LocalClient : WebViewClient() {
        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            isRedirected = false
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            CookieManager.getInstance().flush()

            if (!isRedirected) {
                url?.let { url ->
                    if (url == BASE_URL) {
                        with(Intent(this@WebActivity, StartGameActivity::class.java)) {
                            startActivity(this)
                            this@WebActivity.finish()
                        }
                    } else {
                        lifecycleScope.launch(Dispatchers.IO) {
                            isUrlSaved = myViewModel.checkIfSaved(IS_SAVED_KEY, this@WebActivity)
                            if (!isUrlSaved) {
                                myViewModel.saveUrlToDataStore(url, true, this@WebActivity)
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_NAME = "url"
        const val USER_AGENT = "http.agent"
        const val INTENT_TYPE = "image/*"
        const val CHOOSER_TITLE = "Image Chooser"
        const val BASE_URL = "https://bookoftut.live/"
        const val RESULT_CODE = 1
        const val IS_SAVED_KEY = "isSaved"
    }
}