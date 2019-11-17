package org.cornelldti.density.density.network

import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.cornelldti.density.density.DensityApplication
import org.cornelldti.density.density.data.FacilityClass
import org.cornelldti.density.density.util.FluxUtil
import org.json.JSONArray
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
            onResponse: (facilities: MutableList<FacilityClass>?) -> Unit,
            onError: (error: VolleyError) -> Unit
    ) {
        val facilityListRequest = object : JsonArrayRequest(
                Method.GET,
                FACILITY_LIST_ENDPOINT,
                null,
                Response.Listener { response ->
                    Log.d("RESP1", response.toString())
                    val facilities = parseFacilitiesJson(jsonArray = response)
                    onResponse(facilities)
                }, Response.ErrorListener { error -> onError(error) }
        ) {
            override fun getHeaders(): Map<String, String> {
                return hashMapOf("Authorization" to "Bearer $idToken")
            }
        }
        queue.add(facilityListRequest)
    }

    private fun parseFacilitiesJson(jsonArray: JSONArray): MutableList<FacilityClass>? =
        try {
            val facilities = arrayListOf<FacilityClass>()
            for (i in 0 until jsonArray.length()) {
                val facilityJson = jsonArray.getJSONObject(i)
                val facility = FacilityClass(
                    name = facilityJson.getString("displayName"),
                    id = facilityJson.getString("id")
                )
                facilities.add(facility)
            }
            facilities
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }

    fun fetchFacilityInfo(
            list: MutableList<FacilityClass>,
            success: (Boolean) -> Unit,
            fetchFacilityOccupancyOnResponse: (
                    list: MutableList<FacilityClass>,
                    response: JSONArray
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
            list: MutableList<FacilityClass>,
            success: (Boolean) -> Unit,
            fetchFacilityOccupancyOnResponse: (
                    list: MutableList<FacilityClass>,
                    response: JSONArray
            ) -> Unit) {
        val facilityOccupancyRequest = object : JsonArrayRequest(
                Method.GET,
                HOW_DENSE_ENDPOINT,
                null,
                Response.Listener { response ->
                    Log.d("RESP3", response.toString())
                    fetchFacilityOccupancyOnResponse(list, response)
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
            fetchOperatingHoursOnResponse: (operatingHours: List<String>) -> Unit
    ) {
        val operatingHoursRequest = object : JsonArrayRequest(
                Method.GET,
                "$OPERATING_HOURS_ENDPOINT?id=$facilityId&startDate=${FluxUtil.getDate(day)}&endDate=${FluxUtil.getDate(day)}",
                null,
                Response.Listener { response ->
                    fetchOperatingHoursOnResponse(parseOperatingHoursJson(response))
                },
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
            fetchOperatingHoursOnResponse: (operatingHours: List<String>) -> Unit,
            fetchHistoricalJSONOnResponse: (densities: List<Double>) -> Unit
    ) {
        fetchOperatingHours(day, facilityId, fetchOperatingHoursOnResponse)
        val historicalDataRequest = object : JsonArrayRequest(
                Method.GET,
                "$HISTORICAL_DATA_ENDPOINT?id=$facilityId",
                null,
                Response.Listener { response ->
                    fetchHistoricalJSONOnResponse(parseHistoricalJson(response, day))
                },
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

    private fun parseOperatingHoursJson(jsonArray: JSONArray): List<String> {
        val operatingHours = arrayListOf<String>()
        try {
            val hours = jsonArray.getJSONObject(0).getJSONArray("hours")
            for (i in 0 until hours.length()) {
                val segment = hours.getJSONObject(i).getJSONObject("dailyHours")
                val start = segment.getLong("startTimestamp")
                val end = segment.getLong("endTimestamp")
                operatingHours.add(parseTime(start) + " – " + parseTime(end))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return operatingHours
    }

    private fun parseHistoricalJson(jsonArray: JSONArray, day: String): List<Double> {
        val densities = arrayListOf<Double>()
        try {
            val facilityHistory = jsonArray.getJSONObject(0).getJSONObject("hours")
            val facOnDay = facilityHistory.getJSONObject(day)
            for (hour in 7..23) {
                densities.add(facOnDay.getDouble(hour.toString()))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return densities
    }

    private fun parseTime(timestamp: Long): String {
        val timeZone = Calendar.getInstance().timeZone
        var format = SimpleDateFormat("h:mma", Locale.US)
        if (DateFormat.is24HourFormat(DensityApplication.getAppContext())) {
            format = SimpleDateFormat("HH:mm", Locale.US)
        }
        format.timeZone = timeZone

        return format.format(Date(timestamp * 1000)).toLowerCase(Locale.US)
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
