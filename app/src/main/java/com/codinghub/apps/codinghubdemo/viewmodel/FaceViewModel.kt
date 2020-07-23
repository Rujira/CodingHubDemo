package com.codinghub.apps.codinghubdemo.viewmodel

import android.app.Activity
import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.codinghub.apps.codinghubdemo.app.Injection
import com.codinghub.apps.codinghubdemo.model.objects.error.Either
import com.codinghub.apps.codinghubdemo.model.objects.requests.IdentifyFaceRequest
import com.codinghub.apps.codinghubdemo.model.objects.responses.FaceResponse

class FaceViewModel(application: Application): AndroidViewModel(application) {

    private val repository = Injection.provideRepository()

    fun identifyFace(picture: String): LiveData<Either<FaceResponse>> {
        val request = IdentifyFaceRequest(picture)

        return repository.identifyFace(request)
    }

    fun modifyOrientation(activity: Activity, bitmap: Bitmap, uri: Uri): Bitmap {
        return repository.modifyOrientation(activity, bitmap, uri)
    }
}