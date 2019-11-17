package org.cornelldti.density.density.network

import android.content.Context
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.cornelldti.density.density.BaseActivity
import org.cornelldti.density.density.DensityApplication
import org.cornelldti.density.density.data.FacilityClass
import org.cornelldti.density.density.util.FluxUtil
import org.json.JSONArray
import org.json.JSONException

private const val FACILITY_LIST_ENDPOINT = "https://flux.api.internal.cornelldti.org/v1/facilityList"
private const val FACILITY_INFO_ENDPOINT = "https://flux.api.internal.cornelldti.org/v1/facilityInfo"
private const val HOW_DENSE_ENDPOINT = "https://flux.api.internal.cornelldti.org/v1/howDense"

private const val OPERATING_HOURS_ENDPOINT = "https://flux.api.internal.cornelldti.org/v1/facilityHours"

private const val HISTORICAL_DATA_ENDPOINT = "https://flux.api.internal.cornelldti.org/v1/historicalData"

class API(context: Context) {
    @Transient
    private lateinit var idToken: String
    private val queue: RequestQueue = Volley.newRequestQueue(context)

    fun setIdToken(idToken: String) {
        this.idToken = idToken
    }

    /**
     * @return
     */
    fun fetchFacilities(
            success: (Boolean) -> Unit,
            refresh: Boolean,
            fetchFacilitiesOnResponse: (response: JSONArray, refresh: Boolean, success: (Boolean) -> Unit) -> Unit,
            fetchFacilitiesOnError: (error: VolleyError, success: (Boolean) -> Unit) -> Unit
    ) {
        val facilityListRequest = object : JsonArrayRequest(
                Method.GET,
                FACILITY_LIST_ENDPOINT,
                null,
                Response.Listener { response ->
                    Log.d("RESP1", response.toString())
                    fetchFacilitiesOnResponse(response, refresh, success)
                }, Response.ErrorListener { error -> fetchFacilitiesOnError(error, success) }
        ) {
            override fun getHeaders(): Map<String, String> {
                return hashMapOf("Authorization" to "Bearer $idToken")
            }
        }
        queue.add(facilityListRequest)
    }

    fun fetchFacilityInfo(
            list: ArrayList<FacilityClass>,
            success: (Boolean) -> Unit,
            fetchFacilityOccupancyOnResponse: (
                    list: ArrayList<FacilityClass>,
                    response: JSONArray,
                    success: (Boolean) -> Unit
            ) -> Unit) {
        val facilityInfoRequest = object : JsonArrayRequest(
                Method.GET,
                FACILITY_INFO_ENDPOINT,
                null,
                Response.Listener { response ->
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
                        fetchFacilityOccupancy(list, success, fetchFacilityOccupancyOnResponse)
                    } catch (e: JSONException) {
                        success(false)
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { error ->
                    Log.d("ERROR MESSAGE", error.toString())
                    success(false)
                }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $idToken"
                return headers
            }
        }
        queue.add(facilityInfoRequest)
    }

    private fun fetchFacilityOccupancy(
            list: ArrayList<FacilityClass>,
            success: (Boolean) -> Unit,
            fetchFacilityOccupancyOnResponse: (
                    list: ArrayList<FacilityClass>,
                    response: JSONArray,
                    success: (Boolean) -> Unit
            ) -> Unit) {
        val facilityOccupancyRequest = object : JsonArrayRequest(
                Method.GET,
                HOW_DENSE_ENDPOINT,
                null,
                Response.Listener { response ->
                    Log.d("RESP3", response.toString())
                    fetchFacilityOccupancyOnResponse(list, response, success)
                },
                Response.ErrorListener { error ->
                    success(false)
                    Log.d("ERROR MESSAGE", error.toString())
                }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $idToken"
                return headers
            }
        }
        queue.add(facilityOccupancyRequest)
    }

    private fun fetchOperatingHours(
            day: String,
            facilityId: String,
            fetchOperatingHoursOnResponse: (response: JSONArray, day: String) -> Unit
    ) {
        val operatingHoursRequest = object : JsonArrayRequest(
                Method.GET,
                "$OPERATING_HOURS_ENDPOINT?id=$facilityId&startDate=${FluxUtil.getDate(day)}&endDate=${FluxUtil.getDate(day)}",
                null,
                Response.Listener { response -> fetchOperatingHoursOnResponse(response, day) },
                Response.ErrorListener { error -> Log.d("ERROR MESSAGE", error.toString()) }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $idToken"
                return headers
            }
        }
        queue.add(operatingHoursRequest)
    }

    fun fetchHistoricalJSON(
            day: String,
            facilityId: String,
            fetchOperatingHoursOnResponse: (response: JSONArray, day: String) -> Unit,
            fetchHistoricalJSONOnResponse: (response: JSONArray, day: String) -> Unit
    ) {
        fetchOperatingHours(day, facilityId, fetchOperatingHoursOnResponse)
        val historicalDataRequest = object : JsonArrayRequest(
                Method.GET,
                "$HISTORICAL_DATA_ENDPOINT?id=$facilityId",
                null,
                Response.Listener { response -> fetchHistoricalJSONOnResponse(response, day) },
                Response.ErrorListener { error -> Log.d("ERROR MESSAGE", error.toString()) }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $idToken"
                return headers
            }
        }
        queue.add(historicalDataRequest)
    }

    // TODO Function returns a 403 Error Code!
    fun singleFacilityOccupancy(facId: String) {
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
                headers["Authorization"] = "Bearer $idToken"
                return headers
            }
        }
        queue.add(facilityRequest)
    }
}
