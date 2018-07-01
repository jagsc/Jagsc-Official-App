package org.jagsc.jagsc_official_app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import com.hardikgoswami.oauthLibGithub.R

import org.json.JSONException
import org.json.JSONObject

import java.io.IOException
import java.util.ArrayList

import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class OauthActivity : AppCompatActivity() {

    var scopeAppendToUrl = ""
    var scopeList = arrayListOf<String>()

    private var webview: WebView? = null

    private var clearDataBeforeLaunch = false
    private var isScopeDefined = false
    private var debug = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oauth)

        scopeList = ArrayList()
        scopeAppendToUrl = ""

        val intent = intent

        if (intent.extras != null) {
            CLIENT_ID = intent.getStringExtra("id")
            PACKAGE = intent.getStringExtra("package")
            CLIENT_SECRET = intent.getStringExtra("secret")
            ACTIVITY_NAME = intent.getStringExtra("activity")
            debug = intent.getBooleanExtra("debug", false)
            isScopeDefined = intent.getBooleanExtra("isScopeDefined", false)
            clearDataBeforeLaunch = intent.getBooleanExtra("clearData", false)
        } else {
            Log.d(TAG, "intent extras null")
            finish()
        }

        var url_load = "$GITHUB_URL?client_id=$CLIENT_ID"

        if (isScopeDefined) {
            scopeList = intent.getStringArrayListExtra("scope_list")
            scopeAppendToUrl = getCsvFromList(scopeList)
            url_load += "&scope=$scopeAppendToUrl"
        }

        if (debug) {
            Log.d(TAG, "intent received is " + "\n-client id: " + CLIENT_ID + "\n-secret:" + CLIENT_SECRET + "\n-activity: " + ACTIVITY_NAME + "\n-Package: " + PACKAGE)
            Log.d(TAG, "onCreate: Scope request are : $scopeAppendToUrl")
        }

        if (clearDataBeforeLaunch) {
            clearDataBeforeLaunch()
        }
        webview = findViewById<WebView>(org.jagsc.jagsc_official_app.R.id.webView2)
        if (webview == null) {
            return
        }

        webview!!.settings.javaScriptEnabled = true
        webview!!.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                super.shouldOverrideUrlLoading(view, url)
                // Try catch to allow in app browsing without crashing.
                try {
                    if (!url.contains("?code=")) return false

                    CODE = url.substring(url.lastIndexOf("?code=") + 1)
                    val token_code = CODE.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val tokenFetchedIs = token_code[1]
                    val cleanToken = tokenFetchedIs.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    fetchOauthTokenWithCode(cleanToken[0])

                    if (debug) {
                        Log.d(TAG, "code fetched is: $CODE")
                        Log.d(TAG, "code token: " + token_code[1])
                        Log.d(TAG, "token cleaned is: " + cleanToken[0])
                    }
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                } catch (e: ArrayIndexOutOfBoundsException) {
                    e.printStackTrace()
                }

                return false
            }
        }

        webview!!.loadUrl(url_load)
    }

    private fun clearDataBeforeLaunch() {
        val cookieManager = CookieManager.getInstance()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies { aBoolean ->
                // a callback which is executed when the cookies have been removed
                Log.d(TAG, "Cookie removed: " + aBoolean!!)
            }
        } else {

            cookieManager.removeAllCookie()
        }
    }

    private fun fetchOauthTokenWithCode(code: String) {
        val client = OkHttpClient()
        val url = HttpUrl.parse(GITHUB_OAUTH).newBuilder()
        url.addQueryParameter("client_id", CLIENT_ID)
        url.addQueryParameter("client_secret", CLIENT_SECRET)
        url.addQueryParameter("code", code)

        val url_oauth = url.build().toString()

        val request = Request.Builder().header("Accept", "application/json").url(url_oauth).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (debug) {
                    Log.d(TAG, "IOException: " + e.message)
                }

                finishThisActivity(ResultCode.ERROR)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {

                if (response.isSuccessful) {
                    val JsonData = response.body().string()

                    if (debug) {
                        Log.d(TAG, "response is: $JsonData")
                    }

                    try {
                        val jsonObject = JSONObject(JsonData)
                        val auth_token = jsonObject.getString("access_token")

                        storeToSharedPreference(auth_token)

                        if (debug) {
                            Log.d(TAG, "token is: $auth_token")
                        }

                    } catch (exp: JSONException) {
                        if (debug) {
                            Log.d(TAG, "json exception: " + exp.message)
                        }
                    }

                } else {
                    if (debug) {
                        Log.d(TAG, "onResponse: not success: " + response.message())
                    }
                }

                finishThisActivity(ResultCode.SUCCESS)
            }
        })
    }

    // Allow web view to go back a page.
    override fun onBackPressed() {
        if (webview!!.canGoBack()) {
            webview!!.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun storeToSharedPreference(auth_token: String) {
        val prefs = getSharedPreferences("github_prefs", Context.MODE_PRIVATE)
        val edit = prefs.edit()

        edit.putString("oauth_token", auth_token)
        edit.apply()
    }

    /**
     * Finish this activity and returns the result
     *
     * @param resultCode one of the constants from the class ResultCode
     */
    private fun finishThisActivity(resultCode: Int) {
        setResult(resultCode)
        finish()
    }

    /**
     * Generate a comma separated list of scopes out of the
     *
     * @param scopeList list of scopes as defined
     * @return comma separated list of scopes
     */
    fun getCsvFromList(scopeList: List<String>): String {
        var csvString = ""

        for (scope in scopeList) {
            if (csvString != "") {
                csvString += ","
            }

            csvString += scope
        }

        return csvString
    }

    companion object {

        val GITHUB_URL = "https://github.com/login/oauth/authorize"
        val GITHUB_OAUTH = "https://github.com/login/oauth/access_token"
        var CODE = ""
        var PACKAGE = "Jagsc-Official-App"
        var CLIENT_ID = "9e676ccb216c3c0f04eb"
        var CLIENT_SECRET = "675b5e47147ab84e0ca1442be95a47c8f15c8f4f"
        var ACTIVITY_NAME = "OauthActivity"

        private val TAG = "github-oauth"
    }
}
