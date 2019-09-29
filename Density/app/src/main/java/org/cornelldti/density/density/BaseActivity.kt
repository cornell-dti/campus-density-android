package org.cornelldti.density.density

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import androidx.arch.core.util.Function
import java.util.*

open class BaseActivity :
        AppCompatActivity(), FirebaseAuth.IdTokenListener, FirebaseAuth.AuthStateListener {

    @Transient
    var idToken: String? = null
        private set

    private var auth: FirebaseAuth? = null

    /**
     * GETTER FUNCTION FOR ALL_FACILITIES LIST
     */
    var all_facilities: ArrayList<Facility>? = null
        private set // KEEPS TRACK OF ALL FACILITIES

    /**
     * GETTER FUNCTION FOR SELECTED FACILITY'S OCCUPANCY RATING
     */
    val facility_occupancy_rating: Int = 0 // KEEPS TRACK OF SELECTED FACILITY'S OCCUPANCY

    private var queue: RequestQueue? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        checkUserSignedIn()
        queue = Volley.newRequestQueue(this)
    }

    override fun onRestart() {
        super.onRestart()
        checkUserSignedIn()
    }

    // Invoked whenever ID Token changed!
    override fun onIdTokenChanged(auth: FirebaseAuth) {
        if (auth.currentUser != null) {
            requestToken(auth.currentUser!!)
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

    fun refreshToken() {
        requestToken(auth!!.currentUser!!)
    }

    protected fun requestToken(user: FirebaseUser) {
        Log.d("checkpoint", "requestToken")
        user.getIdToken(true)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("checkpoint", "gotToken")
                        idToken = task.result!!.token
                        updateUI()
                    } else {
                        Log.d("AUTH ERROR", "Error obtaining Firebase Auth ID token")
                    }
                }
    }

    private fun checkUserSignedIn() {
        Log.d("checkpoint", "checkUserSignedIn")
        //        auth.addIdTokenListener(this);
        auth!!.addAuthStateListener(this)
        val user = auth!!.currentUser
        if (user == null) {
            signIn()
        } else {
            requestToken(user)
        }

    }

    private fun signIn() {
        Log.d("checkpoint", "signIn")
        auth!!.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("checkpoint", "signIn = success")
                        Log.d("Firebase", "signInAnonymously:success")
                        val user = auth!!.currentUser
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

    open fun fetchFacilitiesOnResponse(response: JSONArray, refresh: Boolean, success: (Boolean) -> Unit) {
        try {
            val f = ArrayList<Facility>()
            for (i in 0 until response.length()) {
                val facility = response.getJSONObject(i)
                f.add(Facility(facility.getString("displayName"), facility.getString("id")))
            }
            fetchFacilityInfo(f, refresh, success)
        } catch (e: JSONException) {
            success(false)
            e.printStackTrace()
        }

    }

    open fun fetchFacilitiesOnError(error: VolleyError, success: (Boolean) -> Unit) {
        Log.d("ERROR", error.toString())
        success(false)
    }

    open fun fetchFacilityOccupancyOnResponse(
            list: ArrayList<Facility>,
            response: JSONArray,
            refresh: Boolean,
            success: (Boolean) -> Unit
    ) {
        try {
            for (i in list.indices) {
                for (x in 0 until response.length()) {
                    val obj = response.getJSONObject(x)
                    if (obj.getString("id") == list[i].id) {
                        list[i] = list[i].setOccupancy_rating(obj.getInt("density"))
                    }
                }
            }

            all_facilities = list

        } catch (e: JSONException) {
            success(false)
            e.printStackTrace()
        }

    }

    // TODO Function returns a 403 Error Code!
    fun singleFacilityOccupancy(facId: String) {
        val facilityRequest = object : JsonObjectRequest(Method.GET, "$HOW_DENSE_ENDPOINT?=$facId", null, Response.Listener { response ->
            Log.d("GOTOCCRATING", response.toString())
            //                        try {
            //                            facility_occupancy_rating = response.getInt("density");
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
        queue!!.add(facilityRequest)
    }

    /**
     * @return
     */
    fun fetchFacilities(refresh: Boolean, success: (Boolean) -> Unit) {
        val facilityListRequest = object : JsonArrayRequest(Method.GET, FACILITY_LIST_ENDPOINT, null, Response.Listener { response ->
            Log.d("RESP1", response.toString())
            fetchFacilitiesOnResponse(response, refresh, success)
        }, Response.ErrorListener { error -> fetchFacilitiesOnError(error, success) }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer " + idToken!!
                return headers
            }
        }
        queue!!.add(facilityListRequest)
    }

    fun fetchFacilityInfo(list: ArrayList<Facility>, refresh: Boolean, success: (Boolean) -> Unit) {
        val facilityInfoRequest = object : JsonArrayRequest(Method.GET, FACILITY_INFO_ENDPOINT, null, Response.Listener { response ->
            Log.d("RESP2", response.toString())
            try {
                for (i in list.indices) {
                    for (x in 0 until response.length()) {
                        val obj = response.getJSONObject(x)
                        if (obj.getString("id") == list[i].id) {
                            val f = list[i]
                            if (obj.has("campusLocation")) {
                                f.setLocation(obj.getString("campusLocation"))
                            }

                            if (obj.has("description")) {
                                f.description = obj.getString("description")
                            }

                            if (obj.has("closingAt")) {
                                f.setClosingAt(obj.getLong("closingAt"))
                            }

                            list[i] = f
                        }
                    }
                }
                fetchFacilityOccupancy(list, refresh, success)
            } catch (e: JSONException) {
                success(false)
                e.printStackTrace()
            }
        }, Response.ErrorListener { error ->
            Toast.makeText(this@BaseActivity, "Please check internet connection", Toast.LENGTH_LONG).show()
            Log.d("ERROR MESSAGE", error.toString())
            success(false)
        }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer " + idToken!!
                return headers
            }
        }
        queue!!.add(facilityInfoRequest)
    }

    fun fetchFacilityOccupancy(list: ArrayList<Facility>, refresh: Boolean, success: (Boolean) -> Unit) {
        val facilityOccupancyRequest = object : JsonArrayRequest(Method.GET, HOW_DENSE_ENDPOINT, null, Response.Listener { response ->
            Log.d("RESP3", response.toString())
            fetchFacilityOccupancyOnResponse(list, response, refresh, success)
        }, Response.ErrorListener { error ->
            success(false)
            Toast.makeText(this@BaseActivity, "Please check internet connection", Toast.LENGTH_LONG).show()
            Log.d("ERROR MESSAGE", error.toString())
        }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer " + idToken!!
                return headers
            }
        }
        queue!!.add(facilityOccupancyRequest)
    }

    open fun fetchOperatingHoursOnResponse(response: JSONArray, success: (Boolean) -> Unit, day: String) {
        // OVERRIDE IN FACILITYPAGE
    }

    open fun fetchHistoricalJSONOnResponse(response: JSONArray, success: (Boolean) -> Unit, day: String) {
        // OVERRIDE IN FACILITYPAGE
    }


    fun fetchOperatingHours(success: (Boolean) -> Unit, day: String, facility: Facility) {
        val operatingHoursRequest = object : JsonArrayRequest(Method.GET, OPERATING_HOURS_ENDPOINT + "?id=" + facility.id + "&startDate=" + getDate(day) + "&endDate=" + getDate(day), null, Response.Listener { response -> fetchOperatingHoursOnResponse(response, success, day) }, Response.ErrorListener { error ->
            // Toast.makeText(FacilityPage.this, "Please check internet connection", Toast.LENGTH_LONG).show();
            Log.d("ERROR MESSAGE", error.toString())
            success(false)
        }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer " + idToken!!
                return headers
            }
        }
        queue!!.add(operatingHoursRequest)
    }

    fun fetchHistoricalJSON(success: (Boolean) -> Unit, day: String, facility: Facility) {
        fetchOperatingHours({ }, day, facility)
        val historicalDataRequest = object : JsonArrayRequest(Method.GET, HISTORICAL_DATA_ENDPOINT + "?id=" + facility.id, null, Response.Listener { response -> fetchHistoricalJSONOnResponse(response, success, day) }, Response.ErrorListener { error ->
            // Toast.makeText(FacilityPage.this, "Please check internet connection", Toast.LENGTH_LONG).show();
            Log.d("ERROR MESSAGE", error.toString())
            success(false)
        }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer " + idToken!!
                return headers
            }
        }
        queue!!.add(historicalDataRequest)
    }

    private fun getDate(day: String): String {
        val current = Calendar.getInstance()
        val format = SimpleDateFormat("MM-dd-yy", Locale.US)
        val checkFormat = SimpleDateFormat("E", Locale.US)

        var dayCheck = checkFormat.format(current.time).toUpperCase(Locale.US)
        while (dayCheck != day) {
            current.add(Calendar.DAY_OF_MONTH, 1)
            dayCheck = checkFormat.format(current.time).toUpperCase(Locale.US)
        }

        return format.format(current.time)
    }

    companion object {

        private val FACILITY_LIST_ENDPOINT = "https://flux.api.internal.cornelldti.org/v1/facilityList"
        private val FACILITY_INFO_ENDPOINT = "https://flux.api.internal.cornelldti.org/v1/facilityInfo"
        private val HOW_DENSE_ENDPOINT = "https://flux.api.internal.cornelldti.org/v1/howDense"

        val OPERATING_HOURS_ENDPOINT = "https://flux.api.internal.cornelldti.org/v1/facilityHours"

        val HISTORICAL_DATA_ENDPOINT = "https://flux.api.internal.cornelldti.org/v1/historicalData"
    }

}
