package org.cornelldti.density.density.network

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class Firebase : FirebaseAuth.IdTokenListener, FirebaseAuth.AuthStateListener {
    var idToken: String? = null
        private set

    // Invoked whenever ID Token changed!
    override fun onIdTokenChanged(auth: FirebaseAuth) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            requestToken(currentUser)
        } else {
            signIn()
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
                }
            }
    }

    private fun requestToken(user: FirebaseUser) {
        Log.d("checkpoint", "requestToken")
        user.getIdToken(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("checkpoint", "gotToken")
                    idToken = task.result?.token
//                        updateUI()
                } else {
                    Log.d("AUTH ERROR", "Error obtaining Firebase Auth ID token")
                }
            }
    }

    // When user is signed out, or lost access.
    override fun onAuthStateChanged(auth: FirebaseAuth) {
        // TODO DISPLAY ERROR SCREEN AND ATTEMPT TO RE SIGN IN
    }
}