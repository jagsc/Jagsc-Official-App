package org.jagsc.jagsc_official_app

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.HashMap


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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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

        //gitのステータスを上に表示する用
        val mainLayout = findViewById<FrameLayout>(R.id.mainLayout)
        val statusLayout = findViewById<FrameLayout>(R.id.statusLayout)
        val webLayout = findViewById<android.support.constraint.ConstraintLayout>(R.id.webLayout)
        //webLayout.bringToFront()
       // statusLayout.bringToFront()
//        statusLayout.translationZ = 1F
//        statusLayout.elevation = 1.0f
//        webLayout.translationZ = 20F
//        webLayout.elevation = 20.0f
//        statusLayout.invalidate()
//        webLayout.invalidate()
//        mainLayout.invalidate()

        val webView = findViewById<WebView>(R.id.webView)
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true

        webView.loadUrl("http://student.android-group.jp/")

        val home = findViewById<Button>(R.id.home)
        home.setOnClickListener {
            webView.loadUrl("http://student.android-group.jp/")
            webView.bringToFront()
            webLayout.bringToFront()

        }

        val students = findViewById<Button>(R.id.students)
        students.setOnClickListener {
            webView.loadUrl("http://student.android-group.jp/about/")
            webView.bringToFront()
            webLayout.bringToFront()

        }

        val history = findViewById<Button>(R.id.history)
        history.setOnClickListener {
            webView.loadUrl("http://student.android-group.jp/")
            webView.bringToFront()
            webLayout.bringToFront()

        }

        val entry = findViewById<Button>(R.id.entry)
        entry.setOnClickListener {
            webView.loadUrl("http://student.android-group.jp/join/")
            webView.bringToFront()
            webLayout.bringToFront()
        }
        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener(View.OnClickListener {

            scopeList = ArrayList()
            scopeList.add("read:user")
            scopeList.add("read:org")
            scopeAppendToUrl = ""

            var urlLoad = "$GITHUB_URL?client_id=$CLIENT_ID"

            if (isScopeDefined) {
                //scopeList = intent.getStringArrayListExtra("scope_list")
                scopeAppendToUrl = getCsvFromList(scopeList)
                urlLoad += "&scope=$scopeAppendToUrl"
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
                        val tokenCode = CODE.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        val tokenFetchedIs = tokenCode[1]
                        val cleanToken = tokenFetchedIs.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                        fetchOauthTokenWithCode(cleanToken[0])

                        if (debug) {
                            Log.d(TAG, "code fetched is: $CODE")
                            Log.d(TAG, "code token: " + tokenCode[1])
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

            webView.loadUrl(urlLoad)
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

        val urlOauth = url.build().toString()

        val request = Request.Builder().header("Accept", "application/json").url(urlOauth).build()

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
                    val jsonData = response.body().string()
                    if (debug) {
                        Log.d(TAG, "response is: $jsonData")
                    }

                    try {
                        val jsonObject = JSONObject(jsonData)
                        val oauthToken = jsonObject.getString("access_token")

                        //storeToSharedPreference(oauthToken)

                        if (debug) {
                            Log.d(TAG, "token is: $oauthToken")
                            var userName = GetUserName(oauthToken)
                            Log.d(TAG, "user name: $userName")
                            var isjagsc = IsJagscMember(oauthToken,userName)
                            Log.d(TAG, "Jagscのメンバーかどうか: $isjagsc")
                            var iconImage = GetImage(URL("https://avatars.githubusercontent.com/$userName"))
                            var contributionsImage = GetImage(URL("https://grass-graph.moshimo.works/images/$userName.png"))
                            var followers = GetFollowers(URL("https://api.github.com/users/$userName"),oauthToken)
                            var reposPair = GetUserRepos(URL("https://api.github.com/users/$userName/repos"),oauthToken)
                            var forksCount = reposPair.first.first
                            var stargazersCount = reposPair.first.second
                            var trueForkList = reposPair.second
                            var falseForkList = reposPair.third
                            var sumPairs = GetContributors(trueForkList,falseForkList,oauthToken,userName)
                            var sumCommit = sumPairs.first
                            var sumCode = sumPairs.second
                            Log.d(TAG ,"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                            var githubLatingSum = sumCode.toInt()+sumCommit.toInt()*100+forksCount.toInt()*1000+stargazersCount.toInt()*10000+followers.toInt()*10000
                            var githubLating= githubLatingSum / 10000
                            Log.d(TAG ,"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")



                            Handler(mainLooper).postDelayed({
                                val statusLayouts = findViewById<FrameLayout>(R.id.statusLayout)
                                statusLayouts.bringToFront()
                                val webLayout = findViewById<android.support.constraint.ConstraintLayout>(R.id.webLayout)
                                webLayout.bringToFront()
                                val gitHubUserName = findViewById<TextView>(R.id.gitHubUserName)
                                gitHubUserName.text = "ユーザーネーム："+userName
                                val gitHubFollowers = findViewById<TextView>(R.id.gitHubFollowers)
                                gitHubFollowers.text = "フォロワー数："+followers
                                val isJagscText = findViewById<TextView>(R.id.isJagscText)
                                if(isjagsc) {
                                    isJagscText.text = "日本Androidの会学生部メンバー"
                                }else{
                                    isJagscText.text = "非所属"
                                }
                                val iconImageView = findViewById<ImageView>(R.id.iconImageView)
                                iconImageView.setImageBitmap(iconImage)
                                val contributionsImageView = findViewById<ImageView>(R.id.contributionsImageView)
                                contributionsImageView.setImageBitmap(contributionsImage)
                                val gitHubForksCount = findViewById<TextView>(R.id.gitHubForksCount)
                                gitHubForksCount.text = "フォークされた数合計：" + forksCount
                                val gitHubStargazersCount = findViewById<TextView>(R.id.gitHubStargazersCount)
                                gitHubStargazersCount.text = "星の数合計：" + stargazersCount
                                val gitHubSumCommit = findViewById<TextView>(R.id.gitHubSumCommit)
                                gitHubSumCommit.text = "コミット数合計：" + sumCommit
                                val gitHubSumCode = findViewById<TextView>(R.id.gitHubSumCode)
                                gitHubSumCode.text = "コード記述数合計：" + sumCode
                                val gitHubSumLating = findViewById<TextView>(R.id.gitHubLate)
                                gitHubSumLating.text = "GitHubLating：" + githubLating

                                //処理
                            }, 3000) //1000ms後
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
    //トークンでユーザーネームの取得
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
                Log.d(TAG, "GetUserNameレスポンスコードは"+ codeStr + "だよ？")
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
                Log.d(TAG, "IsJagscのレスポンスコードは"+ codeStr + "だよ？")
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
    //githubのアイコンイメージ
    private fun GetImage(url: URL): Bitmap {
        var bmp = BitmapFactory.decodeResource(resources, R.drawable.identicon)
        try {
            //val url = URL("https://avatars.githubusercontent.com/$userName")
            //接続用HttpURLConnectionオブジェクト作成
            var connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            try {
                //接続タイムアウトを設定する。
                connection.connectTimeout = 300000
                //レスポンスデータ読み取りタイムアウトを設定する。
                connection.readTimeout = 300000
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
                Log.d(TAG, "GetImageのレスポンスコードは"+codeStr + "だよ？")
                var isg = connection.inputStream
                bmp = BitmapFactory.decodeStream(isg)
                isg.close()
            } finally {
                if (connection != null) {
                    connection.disconnect()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bmp
    }
    private fun GetFollowers(url: URL,token: String):String{
        var Followers = ""
        try {
            var connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = 300000
                connection.readTimeout = 300000
                connection.setRequestProperty("Authorization", "token $token")
                connection.addRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.requestMethod = "GET"
                connection.instanceFollowRedirects = false
                connection.doInput = true
                connection.doOutput = false
                connection.connect()
                val code = connection.responseCode
                val codeStr = Integer.toString(code)
                Log.d(TAG, "GetFollowersのレスポンスコードは"+codeStr + "だよ？")
                // サーバーからのレスポンスを標準出力へ出す
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                var line: String? = null
                val sb = StringBuilder()
                for (line in reader.readLines()) {
                    line?.let { sb.append(line) }
                }
                reader.close()
                val obj = JSONObject(sb.toString())
                Followers = obj.get("followers").toString()
            } finally {
                if (connection != null) {
                    connection.disconnect()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Followers
    }
    private fun GetContributors(trueForkList:MutableList<String>,falseForkList:MutableList<String>,token:String,userName:String):Pair<String,String> {
        var sumCommit = 0
        var sumCode = 0
        for(repoName in falseForkList) {
            val url = URL("https://api.github.com/repos/$userName/$repoName/stats/contributors")
            try {
                var connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                try {
                    connection.connectTimeout = 300000
                    connection.readTimeout = 300000
                    connection.setRequestProperty("Authorization", "token $token")
                    connection.addRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    connection.requestMethod = "GET"
                    connection.instanceFollowRedirects = false
                    connection.doInput = true
                    connection.doOutput = false
                    connection.connect()
                    val code = connection.responseCode
                    val codeStr = Integer.toString(code)
                    Log.d(TAG, "GetContributorsのレスポンスコードは"+codeStr + "でリポは"+repoName)
                    // サーバーからのレスポンスを標準出力へ出す
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    var line: String? = null
                    val sb = StringBuilder()
                    for (line in reader.readLines()) {
                        line?.let { sb.append(line) }
                    }
                    reader.close()
                    var objArray = JSONArray(sb.toString())
                    Log.d(TAG,"objArray成功ううううううううう")
                    var obj = objArray.getJSONObject(0)
                    Log.d(TAG,"obj成功ううううううううう")
                    //var obj = JSONObject(sb.toString())
                    sumCommit += obj.get("total") as Int
                    var weekArray=obj.getJSONArray("weeks")
                    Log.d(TAG,"weekArray成功ううううううううう")
                    for (i in 0..(weekArray.length() - 1)) {
                        var weekObj = weekArray.getJSONObject(i)
                        sumCode += weekObj.get("a") as Int
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return Pair(sumCommit.toString(),sumCode.toString())
    }
    private fun GetUserRepos(url: URL,token: String):Triple<Pair<String, String>,MutableList<String>,MutableList<String>> {
        //var isForkMap:MutableMap<String,Boolean> = mutableMapOf()
        var trueForkList:MutableList<String> = mutableListOf()
        var falseForkList:MutableList<String> = mutableListOf()
        var forksCount = 0
        var stargazersCount = 0
        var language = ""
        var languageList:MutableList<String> = mutableListOf()

        try {
            var connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = 300000
                connection.readTimeout = 300000
                connection.addRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.setRequestProperty("Authorization", "token $token")
                connection.requestMethod = "GET"
                connection.instanceFollowRedirects = false
                connection.doInput = true
                connection.doOutput = false
                connection.connect()
                val code = connection.responseCode
                val codeStr = Integer.toString(code)
                Log.d(TAG, "GetFollowersのレスポンスコードは"+codeStr + "だよ？")
                // サーバーからのレスポンスを標準出力へ出す
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                var line: String? = null
                val sb = StringBuilder()
                for (line in reader.readLines()) {
                    line?.let { sb.append(line) }
                }
                reader.close()
                var objArray = JSONArray(sb.toString())
                for( i in 0..(objArray.length() -1)){
                    var obj = objArray.getJSONObject(i)
                    if(obj.get("fork") as Boolean){
                        trueForkList.add(obj.get("name").toString())
                    }else{
                        falseForkList.add(obj.get("name").toString())
                    }
                    forksCount += obj.get("forks_count") as Int
                    stargazersCount += obj.get("stargazers_count") as Int
                    languageList.add(obj.get("language").toString())
                }
            } finally {
                if (connection != null) {
                    connection.disconnect()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var pairs = Pair(forksCount.toString(),stargazersCount.toString())
        return Triple(pairs,trueForkList,falseForkList)
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
