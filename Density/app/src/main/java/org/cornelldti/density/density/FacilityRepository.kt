package org.cornelldti.density.density

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cornelldti.density.density.data.FacilityClass
import org.cornelldti.density.density.network.API

class FacilityRepository {
    fun getFacilities(): LiveData<FacilityClass> {
        val data = MutableLiveData<MutableList<FacilityClass>>()
//        api.getFacilities().


        TODO()
    }
}