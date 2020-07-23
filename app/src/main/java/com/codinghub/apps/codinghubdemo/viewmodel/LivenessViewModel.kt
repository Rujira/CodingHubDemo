package com.codinghub.apps.codinghubdemo.viewmodel

import android.app.Activity
import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.codinghub.apps.codinghubdemo.app.Injection
import com.codinghub.apps.codinghubdemo.model.objects.error.Either
import com.codinghub.apps.codinghubdemo.model.objects.requests.LivenessRequest
import com.codinghub.apps.codinghubdemo.model.objects.responses.LivenessResponse

class LivenessViewModel(application: Application): AndroidViewModel(application) {

    private val repository = Injection.provideRepository()

    fun checkLiveness(database_image_content: String,
                      database_image_type: Int,
                      query_image_package: String,
                      query_image_package_return_image_list: Boolean,
                      query_image_package_check_same_person: Boolean,
                      auto_rotate_for_database: Boolean,
                      true_negative_rate: Double): LiveData<Either<LivenessResponse>> {

        val request = LivenessRequest(database_image_content,
            database_image_type,
            query_image_package,
            query_image_package_return_image_list,
            query_image_package_check_same_person,
            auto_rotate_for_database,
            true_negative_rate)

        return repository.checkLiveness(request)
    }

    fun modifyOrientation(activity: Activity, bitmap: Bitmap, uri: Uri): Bitmap {
        return repository.modifyOrientation(activity, bitmap, uri)
    }

}