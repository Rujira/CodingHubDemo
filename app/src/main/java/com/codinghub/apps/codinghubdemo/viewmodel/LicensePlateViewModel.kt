package com.codinghub.apps.codinghubdemo.viewmodel

import android.app.Activity
import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.codinghub.apps.codinghubdemo.app.Injection
import com.codinghub.apps.codinghubdemo.model.objects.error.Either
import com.codinghub.apps.codinghubdemo.model.objects.requests.LicensePlateRequest
import com.codinghub.apps.codinghubdemo.model.objects.responses.OpenALPRResponse

class LicensePlateViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Injection.provideRepository()

    fun identifyVehicle(image: String): LiveData<Either<OpenALPRResponse>> {

        return repository.identifyLicensePlate(image)
    }

    fun modifyOrientation(activity: Activity, bitmap: Bitmap, uri: Uri): Bitmap {
        return repository.modifyOrientation(activity, bitmap, uri)
    }
}