package com.codinghub.apps.codinghubdemo.model.repository

import com.codinghub.apps.codinghubdemo.model.objects.preferences.AppPrefs
import com.codinghub.apps.codinghubdemo.model.objects.responses.OpenALPRResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface CDHALPRApi {

    @Headers("Accept: application/json")
    @POST("recognize_bytes?recognize_vehicle=1&country=th&secret_key=sk_7fa083e0f33a9254006f98c4")
    fun identifyLicensePlate(@Body body: String) : Call<OpenALPRResponse>

}