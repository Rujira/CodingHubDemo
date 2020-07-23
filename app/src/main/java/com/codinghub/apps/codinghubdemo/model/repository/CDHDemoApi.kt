package com.codinghub.apps.codinghubdemo.model.repository

import com.codinghub.apps.codinghubdemo.model.objects.preferences.AppPrefs
import com.codinghub.apps.codinghubdemo.model.objects.requests.CompareFaceRequest
import com.codinghub.apps.codinghubdemo.model.objects.requests.IdentifyFaceRequest
import com.codinghub.apps.codinghubdemo.model.objects.requests.TrainFaceRequest
import com.codinghub.apps.codinghubdemo.model.objects.responses.CompareResponse
import com.codinghub.apps.codinghubdemo.model.objects.responses.FaceResponse
import com.codinghub.apps.codinghubdemo.model.objects.responses.TrainResponse
import retrofit2.Call
import retrofit2.http.*

interface CDHDemoApi {


    @Headers("Accept: application/json")
    @POST("face/identify")
    fun identifyFace(@Body body: IdentifyFaceRequest): Call<FaceResponse>

    @Headers("Accept: application/json")
    @POST("face/train")
    fun trainFace(@Body body: TrainFaceRequest): Call<TrainResponse>

    @Headers("Accept: application/json")
    @POST("face/compare")
    fun compareFace(@Body body: CompareFaceRequest): Call<CompareResponse>
    
}