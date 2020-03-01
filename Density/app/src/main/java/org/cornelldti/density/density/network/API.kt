package org.cornelldti.density.density.network

import android.content.Context
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.cornelldti.density.density.data.FacilityClass
import org.cornelldti.density.density.data.MenuClass
import org.cornelldti.density.density.util.FluxUtil
import org.json.JSONArray
import org.json.JSONException

class API(context: Context) {
    @Transient
    private lateinit var idToken: String
    private val queue: RequestQueue = Volley.newRequestQueue(context)

    fun setIdToken(idToken: String) {
        this.idToken = idToken
    }

    fun fetchFacilities(
            success: () -> Unit,
            onDone: (facilities: List<FacilityClass>) -> Unit,
            onError: (error: VolleyError) -> Unit
    ) {
        val facilityListRequest = getRequest(
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

    private fun fetchFacilityInfo(
            list: MutableList<FacilityClass>,
            success: () -> Unit,
            onDone: (facilities: List<FacilityClass>) -> Unit,
            onError: (error: VolleyError) -> Unit
    ) {
        val facilityInfoRequest = getRequest(
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

    private fun fetchFacilityOccupancy(
            list: MutableList<FacilityClass>,
            success: () -> Unit,
            onDone: (facilities: MutableList<FacilityClass>) -> Unit,
            onError: (error: VolleyError) -> Unit
    ) {
        val facilityOccupancyRequest = getRequest(
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

    fun fetchHistoricalJSON(
            day: String,
            facilityId: String,
            fetchOperatingHoursOnResponse: (operatingHours: List<String>) -> Unit,
            fetchHistoricalJSONOnResponse: (densities: List<Double>) -> Unit
    ) {
        val operatingHoursRequest = getRequest(
                url = "$OPERATING_HOURS_ENDPOINT?id=$facilityId&startDate=${FluxUtil.getDate(day)}&endDate=${FluxUtil.getDate(day)}",
                onResponse = { response ->
                    fetchOperatingHoursOnResponse(JsonParser.parseOperatingHours(response))
                },
                onError = { error -> Log.d("ERROR MESSAGE", error.toString()) }
        )
        val historicalDataRequest = getRequest(
                url = "$HISTORICAL_DATA_ENDPOINT?id=$facilityId",
                onResponse = { response ->
                    fetchHistoricalJSONOnResponse(JsonParser.parseHistorical(response, day))
                },
                onError = { error -> Log.d("ERROR MESSAGE", error.toString()) }
        )
        queue.add(operatingHoursRequest)
        queue.add(historicalDataRequest)
    }

    // TODO Function returns a 403 Error Code!
    fun singleFacilityOccupancy(facId: String) {
        val facilityRequest = getRequest(
                url = "$HOW_DENSE_ENDPOINT?=$facId",
                onResponse = { response ->
                    Log.d("GOTOCCRATING", response.toString())
                    // try {
                    //   facilityOccupancyRating = response.getInt("density");
                    // } catch(JSONException e) {
                    //   e.printStackTrace();
                    // }
                },
                onError = { error -> Log.d("ERRORSON", error.toString()) }
        )
        queue.add(facilityRequest)
    }

    fun fetchMenuJSON(
            facilityId: String,
            day: String,
            fetchMenuJSONOnResponse: ((menu: MenuClass?) -> Unit)
    ) {
        val menuRequest = getRequest(
                url = "$MENU_DATA_ENDPOINT?facility=$facilityId&date=$day",
                onResponse = {response ->
                    fetchMenuJSONOnResponse(JsonParser.parseMenu(response, facilityId, day))
                    Log.d("RESPON", response.toString())
                },
                onError = {
                    error -> Log.d("Error Fetching Menu", error.networkResponse.toString())
                }
        )
        queue.add(menuRequest)
    }

    private fun getRequest(
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
