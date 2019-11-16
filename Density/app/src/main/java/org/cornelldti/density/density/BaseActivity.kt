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

    @Transient
    var idToken: String? = null
        private set

    private lateinit var auth: FirebaseAuth
    protected lateinit var api: API

    /**
     * GETTER FUNCTION FOR ALL_FACILITIES LIST
     */
    var allFacilityClasses: ArrayList<FacilityClass>? = null
        private set // KEEPS TRACK OF ALL FACILITIES

    /**
     * GETTER FUNCTION FOR SELECTED FACILITY'S OCCUPANCY RATING
     */
    val facilityOccupancyRating: Int = 0 // KEEPS TRACK OF SELECTED FACILITY'S OCCUPANCY

    private lateinit var queue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        api = API(context = this)
        checkUserSignedIn()
        queue = Volley.newRequestQueue(this)
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
        requestToken(auth.currentUser!!)
    }

    private fun requestToken(user: FirebaseUser) {
        Log.d("checkpoint", "requestToken")
        user.getIdToken(true)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("checkpoint", "gotToken")
                        idToken = task.result?.token
                        idToken?.let { api.setIdToken(it) }
                        updateUI()
                    } else {
                        Log.d("AUTH ERROR", "Error obtaining Firebase Auth ID token")
                    }
                }
    }

    private fun checkUserSignedIn() {
        Log.d("checkpoint", "checkUserSignedIn")
        //        auth.addIdTokenListener(this);
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
        auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
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

    // API HANDLING FUNCTIONS HERE

    protected open fun fetchFacilityOccupancyOnResponse(
            list: ArrayList<FacilityClass>,
            response: JSONArray,
            refresh: Boolean,
            success: (Boolean) -> Unit
    ) {
        try {
            for (i in list.indices) {
                for (x in 0 until response.length()) {
                    val obj = response.getJSONObject(x)
                    if (obj.getString("id") == list[i].id) {
                        list[i] = list[i].setOccupancyRating(obj.getInt("density"))
                    }
                }
            }

            allFacilityClasses = list

        } catch (e: JSONException) {
            success(false)
            e.printStackTrace()
        }

    }

    // TODO Function returns a 403 Error Code!
    protected fun singleFacilityOccupancy(facId: String) {
        val facilityRequest = object : JsonObjectRequest(Method.GET, "$HOW_DENSE_ENDPOINT?=$facId", null, Response.Listener { response ->
            Log.d("GOTOCCRATING", response.toString())
            //                        try {
            //                            facilityOccupancyRating = response.getInt("density");
            //                        }
            //                        catch(JSONException e)
            //                        {
            //                            e.printStackTrace();
            //                        }
        }, Response.ErrorListener { error -> Log.d("ERRORSON", error.toString()) }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer " + idToken!!
                return headers
            }
        }
        queue.add(facilityRequest)
    }

    companion object {

        private const val HOW_DENSE_ENDPOINT = "https://flux.api.internal.cornelldti.org/v1/howDense"

    }

}
