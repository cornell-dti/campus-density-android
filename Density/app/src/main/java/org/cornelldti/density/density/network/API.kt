package org.cornelldti.density.density.network

import android.content.Context
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.cornelldti.density.density.data.FacilityClass
import org.cornelldti.density.density.data.MenuClass
import org.cornelldti.density.density.data.OperatingHoursClass
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class API(context: Context) {
    @Transient
    private lateinit var idToken: String
    private val queue: RequestQueue = Volley.newRequestQueue(context)

    fun setIdToken(idToken: String) {
        this.idToken = idToken
    }

    /**
     * This function fetches the whole list of dining facilities. (1/3)
     */
    fun fetchFacilities(
            success: () -> Unit,
            onDone: (facilities: List<FacilityClass>) -> Unit,
            onError: (error: VolleyError) -> Unit
    ) {
        val facilityListRequest = getJsonArrayRequest(
                url = FACILITY_LIST_ENDPOINT,
                onResponse = { response ->
                    Log.d("RESP1", response.toString())
                    val facilities = JsonParser.parseFacilities(jsonArray = response)
                    if (facilities == null) {
                        success()
                    } else {
                        fetchFacilityInfo(list = facilities, success = success, onDone = onDone, onError = onError)
                    }
                },
                onError = onError
        )
        queue.add(facilityListRequest)
    }

    /**
     * This function fetches fields in FacilityClass for all facilities. (2/3)
     */
    private fun fetchFacilityInfo(
            list: MutableList<FacilityClass>,
            success: () -> Unit,
            onDone: (facilities: List<FacilityClass>) -> Unit,
            onError: (error: VolleyError) -> Unit
    ) {
        val facilityInfoRequest = getJsonArrayRequest(
                url = FACILITY_INFO_ENDPOINT,
                onResponse = { response ->
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
                        fetchFacilityOccupancy(list, success, onDone, onError)
                    } catch (e: JSONException) {
                        success()
                        e.printStackTrace()
                    }
                },
                onError = onError
        )
        queue.add(facilityInfoRequest)
    }

    /**
     * This function fetches the real-time density of each dining facility. (3/3)
     */
    private fun fetchFacilityOccupancy(
            list: MutableList<FacilityClass>,
            success: () -> Unit,
            onDone: (facilities: MutableList<FacilityClass>) -> Unit,
            onError: (error: VolleyError) -> Unit
    ) {
        val facilityOccupancyRequest = getJsonArrayRequest(
                url = HOW_DENSE_ENDPOINT,
                onResponse = { response ->
                    Log.d("RESP3", response.toString())
                    try {
                        for (i in list.indices) {
                            for (x in 0 until response.length()) {
                                val obj = response.getJSONObject(x)
                                if (obj.getString("id") == list[i].id) {
                                    list[i] = list[i].setOccupancyRating(obj.getInt("density"))
                                }
                            }
                        }
                    } catch (e: JSONException) {
                        success()
                        e.printStackTrace()
                    }
                    onDone(list)
                },
                onError = onError
        )
        queue.add(facilityOccupancyRequest)
    }

    /**
     * This function fetches dining menu for each day in a specific facility.
     */
    fun fetchMenuJSON(
            facilityId: String,
            day: String,
            fetchMenuJSONOnResponse: ((menu: MenuClass?) -> Unit)
    ) {
        val menuRequest = getJsonArrayRequest(
                url = "$MENU_DATA_ENDPOINT?facility=$facilityId",
                onResponse = { response ->
                    fetchMenuJSONOnResponse(JsonParser.parseMenu(response, day))
                },
                onError = { error ->
                    Log.d("Error Fetching Menu", error.networkResponse.toString())
                }
        )
        queue.add(menuRequest)
    }

    /**
     * This function applies the onResponse functions passed in as params on the response of the api request.
     */
    fun facilityHours(facilityId: String, startDate: String, endDate: String, facilityHoursTimeStampsOnResponse: (OperatingHoursClass) -> Unit) {
        val facilityHoursRequest = getJsonArrayRequest(
                url = "$OPERATING_HOURS_ENDPOINT?id=$facilityId&startDate=$startDate&endDate=$endDate",
                onResponse = { response ->
                    facilityHoursTimeStampsOnResponse(JsonParser.parseOperatingHoursToTimestampList(response))
                },
                onError = { error ->
                    Log.d("Error fetching hours", error.networkResponse.toString())
                }
        )
        queue.add(facilityHoursRequest)
    }

    /**
     * This function submits the FacilityInfoPage feedback.
     */
    fun addFacilityInfoFeedback(campusLocation: String,
                                predicted: Int,
                                observed: Int,
                                comment: String
    ) {
        val feedback = JSONObject()
        try {
            feedback.put("eatery", campusLocation)
            feedback.put("predictedWait", predicted)
            feedback.put("observedWait", observed)
            feedback.put("comment", comment)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val feedbackRequest = postJsonObjectRequest(
                url = FEEDBACK_ENDPOINT,
                body = feedback,
                onResponse = { response ->
                    Log.d("Success Response", response.toString())
                },
                onError = { error ->
                    Log.d("Error Response", error.toString())
                }
        )
        queue.add(feedbackRequest)
    }

    /**
     * This function submits the FacilitiesActivity feedback.
     */
    fun addFacilitiesFeedback(likelyToRecommend: Int,
                              usefulFeatures: List<Int>,
                              likeFluxOverall: Int,
                              comment: String
    ) {
        val feedback = JSONObject()
        try {
            feedback.put("likelyToRecommend", likelyToRecommend)
            feedback.put("usefulFeatures", JSONArray(usefulFeatures))
            feedback.put("likeFluxOverall", likeFluxOverall)
            feedback.put("comment", comment)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val feedbackRequest = postJsonObjectRequest(
                url = MAIN_FEEDBACK_ENDPOINT,
                body = feedback,
                onResponse = { response ->
                    Log.d("Success Response", response.toString())
                },
                onError = { error ->
                    Log.d("Error Response", error.toString())
                }
        )
        queue.add(feedbackRequest)
    }

    /**
     * This function fetches the waitTimes for all dining locations.
     */
    fun fetchWaitTimes(
            onDone: (waitTimes: Map<String, Double>) -> Unit,
            onError: (error: VolleyError) -> Unit
    ) {
        val waitTimesRequest = getJsonObjectRequest(
                url = WAIT_TIME_ENDPOINT,
                onResponse = { response ->
                    val waitTimes = JsonParser.parseWaitTimes(jsonObject = response).toMap()
                    onDone(waitTimes)
                },
                onError = { error ->
                    Log.d("TAG", error.toString())
                    onError(error)
                }
        )
        queue.add(waitTimesRequest)
    }

    private fun postJsonObjectRequest(
            url: String,
            body: JSONObject,
            onResponse: (jsonObject: JSONObject) -> Unit,
            onError: (error: VolleyError) -> Unit
    ): JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            body,
            Response.Listener(onResponse),
            Response.ErrorListener(onError)
    ) {
        override fun getHeaders(): Map<String, String> =
                hashMapOf("Authorization" to "Bearer $idToken")
    }

    private fun getJsonObjectRequest(
            url: String,
            onResponse: (jsonObject: JSONObject) -> Unit,
            onError: (error: VolleyError) -> Unit
    ): JsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener(onResponse),
            Response.ErrorListener(onError)
    ) {
        override fun getHeaders(): Map<String, String> =
                hashMapOf("Authorization" to "Bearer $idToken")
    }

    private fun getJsonArrayRequest(
            url: String,
            onResponse: (jsonArray: JSONArray) -> Unit,
            onError: (error: VolleyError) -> Unit
    ): JsonArrayRequest = object : JsonArrayRequest(
            Method.GET,
            url,
            null,
            Response.Listener(onResponse),
            Response.ErrorListener(onError)
    ) {
        override fun getHeaders(): Map<String, String> =
                hashMapOf("Authorization" to "Bearer $idToken")
    }
}
