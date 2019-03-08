package com.nxsyed.google_oauth

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.SignInButton
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.Volley.*
import com.google.android.gms.tasks.Task
import java.io.IOException


class MainActivity : AppCompatActivity() {


    private val RC_SIGN_IN = 9001

    private var mGoogleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    public override fun onStart() {
        super.onStart()

        val mGmailSignIn = findViewById<SignInButton>(R.id.sign_in_button)

        val account = GoogleSignIn.getLastSignedInAccount(this)
        Log.w("Sign In: ", account.toString())

        mGmailSignIn.setOnClickListener {
                signIn()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        Log.w("Sign In: ", completedTask.toString())
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account!!.idToken
            Log.w("Sign In: ", idToken.toString())
            authCheck(idToken.toString())
        } catch (e: ApiException) {
            Log.w("Sign In: ", "signInResult:failed code=" + e.statusCode)
        }

    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun authCheck(token: String) {
        // Instantiate the RequestQueue.
        val queue = newRequestQueue(this)
        val url = "https://pubsub.pubnub.com/v1/blocks/sub-key/sub-c-87dbd99c-e470-11e8-8d80-3ee0fe19ec50/google?token=$token"
        val responseText = findViewById<TextView>(R.id.responseText)


        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                // Display the first 500 characters of the response string.
                runOnUiThread {
                    responseText.text = response
                }
                Log.d("Response: ", response)
            },
            Response.ErrorListener {Log.d("Response: ", "No Workie")})

        // Add the request to the RequestQueue.
        queue.add(stringRequest)

    }

}
