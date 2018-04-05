package org.jagsc.jagsc_official_app

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.ArrayList


class MainActivity : AppCompatActivity() {

    val GITHUB_URL = "https://github.com/login/oauth/authorize"
    val GITHUB_OAUTH = "https://github.com/login/oauth/access_token"
    var CODE = ""
    var PACKAGE = "Jagsc-Official-App"
    var CLIENT_ID = "9e676ccb216c3c0f04eb"
    var CLIENT_SECRET = "675b5e47147ab84e0ca1442be95a47c8f15c8f4f"
    var ACTIVITY_NAME = "Activity"

    private val TAG = "github-oauth"

    var scopeAppendToUrl = ""
    var scopeList = arrayListOf<String>()

    private var webiew = null

    private var clearDataBeforeLaunch = false
    private var isScopeDefined = true
    private var debug = true

    @SuppressLint("SetJavaScriptEnabled")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ここで1秒間スリープし、スプラッシュを表示させたままにする。
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
        }
        // スプラッシュthemeを通常themeに変更する
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)

        val webView = findViewById<WebView>(R.id.webView)
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true

        webView.loadUrl("http://student.android-group.jp/")

        val home = findViewById<Button>(R.id.home)
        home.setOnClickListener {
            webView.loadUrl("http://student.android-group.jp/")
        }

        val students = findViewById<Button>(R.id.students)
        students.setOnClickListener {
            webView.loadUrl("http://student.android-group.jp/about/")
        }

        val history = findViewById<Button>(R.id.history)
        history.setOnClickListener {
            webView.loadUrl("http://student.android-group.jp/")
        }

        val entry = findViewById<Button>(R.id.entry)
        entry.setOnClickListener {
            webView.loadUrl("http://student.android-group.jp/join/")
        }
        val loginbutton = findViewById<Button>(R.id.loginButton)
        loginbutton.setOnClickListener(View.OnClickListener {

            scopeList = ArrayList()
            scopeList.add("read:user")
            scopeList.add("read:org")
            scopeAppendToUrl = ""

            //val intent = intent
            /*
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
            }*/

            var url_load = "$GITHUB_URL?client_id=$CLIENT_ID"

            if (isScopeDefined) {
                //scopeList = intent.getStringArrayListExtra("scope_list")
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

            if (webiew == null) {
            }

            webView.getSettings().javaScriptEnabled = true
            webView.webViewClient = object : WebViewClient() {
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
            webView.loadUrl(url_load)
        })

            /*
            GithubOauth.Builder()
                    .withClientId(GITHUB_ID)
                    .withClientSecret(GITHUB_SECRET)
                    .withContext(this)
                    .packageName("org.jagsc.jagsc_official_app")
                    .nextActivity("org.jagsc.jagsc_official_app.MainActivity")
                    .debug(true).execute()

            // Sample to read logged in user oauth token
            val PREFERENCE = "github_prefs"
            val sharedPreferences = getSharedPreferences(PREFERENCE, 0)
            val oauthToken = sharedPreferences.getString("oauth_token", null)
            Log.d("debug", "oauth token for github loged in user is :$oauthToken")
        })
        */
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
                //finishThisActivity(ResultCode.ERROR)
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

                //finishThisActivity(ResultCode.SUCCESS)
            }
        })
    }

    // Allow web view to go back a page.
    override fun onBackPressed() {
            super.onBackPressed()
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
        Log.d(TAG, "finishThisActivity起動しちゃった・・・")
        Log.d(TAG, "resultCodeは" + resultCode + "です！")
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


    override fun onStart() {
        super.onStart()
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
