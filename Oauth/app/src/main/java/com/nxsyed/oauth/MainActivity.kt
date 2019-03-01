package com.nxsyed.oauth

import com.nxsyed.oauth.R
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.auth0.android.Auth0
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.WebAuthProvider
import android.app.Dialog
import android.util.Log
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.result.Credentials


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val account = Auth0(getString(R.string.com_auth0_client_id), getString(R.string.com_auth0_domain))
        //Configure the account in OIDC conformant mode
        account.isOIDCConformant = true
        //Use the account in the API applications
        login()

    }

    private fun login() {
        WebAuthProvider.init(this)
            .withScheme("demo")
            .withAudience(String.format("https://%s/userinfo", getString(R.string.com_auth0_domain)))
            .start(this@MainActivity, object : AuthCallback {
                override fun onFailure(dialog: Dialog) {
                    // Show error Dialog to user
                }

                override fun onFailure(exception: AuthenticationException) {
                    // Show error to user
                }

                override fun onSuccess(credentials: Credentials) {
                    Log.d("credentials", credentials.idToken)
                    Log.d("credentials", credentials.accessToken)
                }
            })
    }

}
