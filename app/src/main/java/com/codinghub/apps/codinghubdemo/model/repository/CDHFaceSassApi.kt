package com.codinghub.apps.codinghubdemo.model.repository

import com.codinghub.apps.codinghubdemo.model.objects.requests.LivenessRequest
import com.codinghub.apps.codinghubdemo.model.objects.responses.LivenessResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface CDHFaceSassApi {

    @Headers("Accept: application/json")
    @POST("face/v1/algorithm/recognition/face_pair_verification")
    fun checkLivesess(@Body body: LivenessRequest): Call<LivenessResponse>
}