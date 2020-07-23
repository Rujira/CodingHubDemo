package com.codinghub.apps.codinghubdemo.model.repository

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import com.codinghub.apps.codinghubdemo.model.objects.error.Either
import com.codinghub.apps.codinghubdemo.model.objects.requests.CompareFaceRequest
import com.codinghub.apps.codinghubdemo.model.objects.requests.IdentifyFaceRequest
import com.codinghub.apps.codinghubdemo.model.objects.requests.LivenessRequest
import com.codinghub.apps.codinghubdemo.model.objects.requests.TrainFaceRequest
import com.codinghub.apps.codinghubdemo.model.objects.responses.*

interface Repository {

    fun identifyFace(request: IdentifyFaceRequest): LiveData<Either<FaceResponse>>
    fun trainFace(request: TrainFaceRequest): LiveData<Either<TrainResponse>>
    fun compareFaces(request: CompareFaceRequest): LiveData<Either<CompareResponse>>
    fun checkLiveness(request: LivenessRequest) : LiveData<Either<LivenessResponse>>

    fun identifyLicensePlate(base64String: String): LiveData<Either<OpenALPRResponse>>

    fun modifyOrientation(activity: Activity, bitmap: Bitmap, uri: Uri): Bitmap

}