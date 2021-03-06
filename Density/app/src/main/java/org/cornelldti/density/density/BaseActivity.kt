package org.cornelldti.density.density

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.cornelldti.density.density.network.API

/**
 * This class holds the main activity that checks for Firebase user authentication.
 * This class is extended by other activities.
 */
open class BaseActivity :
        AppCompatActivity(), FirebaseAuth.IdTokenListener, FirebaseAuth.AuthStateListener {

    protected lateinit var api: API

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        api = API(context = this)
        checkUserSignedIn()
    }

    override fun onRestart() {
        super.onRestart()
        checkUserSignedIn()
    }

    // Invoked whenever ID Token changed!
    override fun onIdTokenChanged(auth: FirebaseAuth) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            requestToken(currentUser)
        } else {
            signIn()
        }
    }

    // When user is signed out, or lost access.
    override fun onAuthStateChanged(auth: FirebaseAuth) {
        // TODO DISPLAY ERROR SCREEN AND ATTEMPT TO RE SIGN IN
    }

    protected open fun updateUI() {}

    protected fun refreshToken() {
        requestToken(FirebaseAuth.getInstance().currentUser!!)
    }

    private fun requestToken(user: FirebaseUser) {
        Log.d("checkpoint", "requestToken")
        user.getIdToken(true)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("checkpoint", "gotToken")
                        task.result?.token?.let { api.setIdToken(it) }
                        updateUI()
                    } else {
                        Log.d("AUTH ERROR", "Error obtaining Firebase Auth ID token")
                    }
                }
    }

    private fun checkUserSignedIn() {
        Log.d("checkpoint", "checkUserSignedIn")
        //        auth.addIdTokenListener(this);
        val auth = FirebaseAuth.getInstance()
        auth.addAuthStateListener(this)
        val user = auth.currentUser
        if (user == null) {
            signIn()
        } else {
            requestToken(user)
        }
    }

    private fun signIn() {
        Log.d("checkpoint", "signIn")
        val auth = FirebaseAuth.getInstance()
        auth.signInAnonymously()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("checkpoint", "signIn = success")
                        Log.d("Firebase", "signInAnonymously:success")
                        val user = auth.currentUser
                        requestToken(user!!)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.d("checkpoint", "signIn = failure")
                        Log.w("Firebase", "signInAnonymously:failure", task.exception)
                        Toast.makeText(this@BaseActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

}
