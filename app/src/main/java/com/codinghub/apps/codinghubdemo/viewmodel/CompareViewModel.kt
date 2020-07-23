package com.codinghub.apps.codinghubdemo.viewmodel

import android.app.Activity
import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.codinghub.apps.codinghubdemo.app.Injection
import com.codinghub.apps.codinghubdemo.model.objects.error.Either
import com.codinghub.apps.codinghubdemo.model.objects.requests.CompareFaceRequest
import com.codinghub.apps.codinghubdemo.model.objects.responses.CompareResponse

class CompareViewModel (application: Application) : AndroidViewModel(application) {

    private val repository = Injection.provideRepository()

    fun compareFace(picture1: String, picture2: String): LiveData<Either<CompareResponse>> {
        val request = CompareFaceRequest(picture1, picture2)

        return repository.compareFaces(request)
    }

    fun modifyOrientation(activity: Activity, bitmap: Bitmap, uri: Uri): Bitmap {
        return repository.modifyOrientation(activity, bitmap, uri)
    }
}