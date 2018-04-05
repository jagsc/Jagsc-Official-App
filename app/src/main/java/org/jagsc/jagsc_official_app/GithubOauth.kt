package org.jagsc.jagsc_official_app

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.hardikgoswami.oauthLibGithub.OauthActivity

import java.util.ArrayList


class GithubOauth {
    var client_id: String = ""
    var client_secret: String = ""
    var nextActivity: String = ""
    private var appContext: Activity? = null
    var isDebug: Boolean = false
    var packageName: String? = null
    var scopeList: ArrayList<String>? = null
        set(scopeList) {
            field = ArrayList()
            field = scopeList
        }
    private var clearBeforeLaunch: Boolean = false


    fun withContext(activity: Activity): GithubOauth {
        setAppContext(activity)
        return this
    }


    @Deprecated("Use withContext(Activity) instead")
    fun withContext(context: Context): GithubOauth {
        setAppContext(context)
        return this
    }
    /*
    fun withClientId(client_id: String): GithubOauth {
        client_id = client_id
        return this
    }*/
    /*
    fun withClientSecret(client_secret: String): GithubOauth {
        client_secret = client_secret
        return this
    }*/

    fun nextActivity(activity: String): GithubOauth {
        nextActivity = activity
        return this
    }

    fun debug(active: Boolean): GithubOauth {
        isDebug = active
        return this
    }
    /*
    fun packageName(packageName: String): GithubOauth {
        packageName = packageName
        return this
    }*/
    /*
    fun withScopeList(scopeList: ArrayList<String>): GithubOauth {
        scopeList = scopeList
        return this
    }*/

    /**
     * Whether the app should clear all data (cookies and cache) before launching a new instance of
     * the webView
     *
     * @param clearBeforeLaunch true to clear data
     * @return An instance of this class
     */
    fun clearBeforeLaunch(clearBeforeLaunch: Boolean): GithubOauth {
        this.clearBeforeLaunch = clearBeforeLaunch
        return this
    }

    fun getAppContext(): Context? {
        return appContext
    }

    fun setAppContext(appContext: Activity) {
        this.appContext = appContext
    }


    @Deprecated("Use setAppContext(Activity) instead")
    fun setAppContext(appContext: Context) {
        this.appContext = appContext as Activity
    }

    /**
     * This method will execute the instance created. The activity of login will be launched and
     * it will return a result after finishing its execution. The result will be one of the constants
     * hold in the class [ResultCode]
     * client_id, client_secret, package name and activity fully qualified are required
     */
    fun execute() {
        val scopeList = scopeList
        val github_id = client_id
        val github_secret = client_secret
        val hasScope = scopeList != null && scopeList.size > 0

        val intent = Intent(getAppContext(), OauthActivity::class.java)
        intent.putExtra("id", github_id)
        intent.putExtra("debug", isDebug)
        intent.putExtra("secret", github_secret)
        intent.putExtra("package", packageName)
        intent.putExtra("activity", nextActivity)
        intent.putExtra("clearData", clearBeforeLaunch)
        intent.putExtra("isScopeDefined", hasScope)

        if (hasScope) {
            intent.putStringArrayListExtra("scope_list", scopeList)
        }

        appContext!!.startActivityForResult(intent, REQUEST_CODE)
    }

    companion object {
        val REQUEST_CODE = 1000

        fun Builder(): GithubOauth {
            return GithubOauth()
        }
    }
}
