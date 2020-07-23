package com.codinghub.apps.codinghubdemo.viewmodel

import android.app.Activity
import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.codinghub.apps.codinghubdemo.app.Injection
import com.codinghub.apps.codinghubdemo.model.objects.error.Either
import com.codinghub.apps.codinghubdemo.model.objects.requests.TrainFaceRequest
import com.codinghub.apps.codinghubdemo.model.objects.responses.TrainResponse

class AddNewFaceViewModel(application: Application): AndroidViewModel(application) {

    private val repository = Injection.provideRepository()

    fun trainFace(picture: String, name: String, person_id: String): LiveData<Either<TrainResponse>> {

        val request = TrainFaceRequest(picture, name, person_id)

        return repository.trainFace(request)
    }

    fun modifyOrientation(activity: Activity, bitmap: Bitmap, uri: Uri): Bitmap {
        return repository.modifyOrientation(activity, bitmap, uri)
    }
}