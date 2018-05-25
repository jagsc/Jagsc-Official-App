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
//import jdk.nashorn.internal.runtime.ScriptingFunctions.readLine
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_OK
import java.net.URL
import java.nio.charset.StandardCharsets
import android.widget.Toast
import java.io.PrintStream
import java.util.*


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
    }

    private fun getCsvFromList(scopeList: ArrayList<String>): String {

        var csvString = ""

        for (scope in scopeList) {
            if (csvString != "") {
                csvString += ","
            }

            csvString += scope
        }

        return csvString
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

                        //storeToSharedPreference(auth_token)

                        if (debug) {
                            Log.d(TAG, "token is: $auth_token")
                            var userName = GetUserName(auth_token)
                            Log.d(TAG, "user name: $userName")
                            var isjagsc = IsJagscMember(auth_token,userName)
                            Log.d(TAG, "Jagscのメンバーかどうか: $isjagsc")
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
    private fun GetUserName(token: String): String {
        var userName = "UNKNOWN"
        try {
            val url = URL("https://api.github.com/user")
            //接続用HttpURLConnectionオブジェクト作成
            var connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            try {
                //接続タイムアウトを設定する。
                connection.connectTimeout = 300000
                //レスポンスデータ読み取りタイムアウトを設定する。
                connection.readTimeout = 300000
                //ヘッダーにAccept-Languageを設定する。
                connection.setRequestProperty("Authorization", "token $token")
                //ヘッダーにContent-Typeを設定する
                connection.addRequestProperty("Content-Type", "application/json; charset=UTF-8")
                // リクエストメソッドの設定
                connection.requestMethod = "GET"
                // リダイレクトを自動で許可しない設定
                connection.instanceFollowRedirects = false
                // URL接続からデータを読み取る場合はtrue
                connection.doInput = true
                // URL接続にデータを書き込む場合はtrue
                connection.doOutput = false
                // 接続
                connection.connect()
                // レスポンスコードの取得
                val code = connection.responseCode
                val codeStr = Integer.toString(code)
                Log.d("GetUserNameレスポンスコードは", codeStr + "だよ？")
                if (code == 200) {
                    Log.d(TAG, "受信成功")
                }
                // サーバーからのレスポンスを標準出力へ出す
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                var line: String? = null
                val sb = StringBuilder()
                for (line in reader.readLines()) {
                    line?.let { sb.append(line) }
                    //Log.d(TAG, "Lineの中身は$line")
                }
                reader.close()
                val obj = JSONObject(sb.toString())
                userName = obj.get("login").toString()
            } finally {
                if (connection != null) {
                    connection.disconnect()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return userName
    }
    //jagscのメンバーかどうかの確認
    private fun IsJagscMember(token: String,userName: String): Boolean {
        var isJagscMember = false
        try {
            val url = URL("https://api.github.com/orgs/jagsc/members/$userName")
            //接続用HttpURLConnectionオブジェクト作成
            var connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            try {
                //接続タイムアウトを設定する。
                connection.connectTimeout = 300000
                //レスポンスデータ読み取りタイムアウトを設定する。
                connection.readTimeout = 300000
                //ヘッダーにAccept-Languageを設定する。
                connection.setRequestProperty("Authorization", "token $token")
                //ヘッダーにContent-Typeを設定する
                connection.addRequestProperty("Content-Type", "application/json; charset=UTF-8")
                // リクエストメソッドの設定
                connection.requestMethod = "GET"
                // リダイレクトを自動で許可しない設定
                connection.instanceFollowRedirects = false
                // URL接続からデータを読み取る場合はtrue
                connection.doInput = true
                // URL接続にデータを書き込む場合はtrue
                connection.doOutput = false
                // 接続
                connection.connect()
                // レスポンスコードの取得
                val code = connection.responseCode
                val codeStr = Integer.toString(code)
                Log.d("IsJagscのレスポンスコードは", codeStr + "だよ？")
                if(code==204){
                    isJagscMember = true
                }
            } finally {
                if (connection != null) {
                    connection.disconnect()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return isJagscMember
    }

    override fun onBackPressed() {
        super.onBackPressed()
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
