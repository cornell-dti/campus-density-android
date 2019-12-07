package org.cornelldti.density.density

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cornelldti.density.density.data.FacilityClass
import org.cornelldti.density.density.network.API

class FacilityRepository {

    private val api: API = API(context = DensityApplication.getAppContext())

    fun getFacilities(): LiveData<List<FacilityClass>> {
        val facilities = MutableLiveData<List<FacilityClass>>()
        api.fetchFacilities(
                onDone = { response -> facilities.value = response },
                onError = { _ -> TODO() },
                success = {}
        )

        return facilities
    }

    fun getHistoricalData(day: String, facilityId: String): LiveData<List<Double>> {
        val densities = MutableLiveData<List<Double>>()
        api.fetchHistoricalJSON(
                day = day,
                facilityId = facilityId,
                fetchOperatingHoursOnResponse = {},
                fetchHistoricalJSONOnResponse = { response -> densities.value = response }
        )

        return densities
    }

    fun getOperatingHours(day: String, facilityId: String): LiveData<List<String>> {
        val hours = MutableLiveData<List<String>>()
        api.fetchHistoricalJSON(
                day = day,
                facilityId = facilityId,
                fetchOperatingHoursOnResponse = { response -> hours.value = response },
                fetchHistoricalJSONOnResponse = {}
        )

        return hours
    }
}