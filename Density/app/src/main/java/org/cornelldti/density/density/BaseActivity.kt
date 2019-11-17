package org.cornelldti.density.density

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import org.json.JSONArray
import org.json.JSONException

import java.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import org.cornelldti.density.density.data.FacilityClass
import org.cornelldti.density.density.network.API
import org.cornelldti.density.density.util.FluxUtil
import java.util.Calendar
import java.util.Locale


open class BaseActivity :
        AppCompatActivity(), FirebaseAuth.IdTokenListener, FirebaseAuth.AuthStateListener {

    protected lateinit var api: API

    /**
     * GETTER FUNCTION FOR SELECTED FACILITY'S OCCUPANCY RATING
     */
    val facilityOccupancyRating: Int = 0 // KEEPS TRACK OF SELECTED FACILITY'S OCCUPANCY

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

    protected open fun updateUI() {
        // add implementation
    }

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
