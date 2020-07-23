package com.codinghub.apps.codinghubdemo.model.repository

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.codinghub.apps.codinghubdemo.app.Injection
import com.codinghub.apps.codinghubdemo.model.objects.error.ApiError
import com.codinghub.apps.codinghubdemo.model.objects.error.Either
import com.codinghub.apps.codinghubdemo.model.objects.requests.CompareFaceRequest
import com.codinghub.apps.codinghubdemo.model.objects.requests.IdentifyFaceRequest
import com.codinghub.apps.codinghubdemo.model.objects.requests.LivenessRequest
import com.codinghub.apps.codinghubdemo.model.objects.requests.TrainFaceRequest
import com.codinghub.apps.codinghubdemo.model.objects.responses.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object RemoteRepository : Repository {

    private val api = Injection.provideCDHDemoApi()
    private val alprApi = Injection.provideALPRApi()
    private val faceSassApi = Injection.provideFaceSassApi()

    private val TAG = RemoteRepository::class.qualifiedName

    override fun identifyFace(request: IdentifyFaceRequest): LiveData<Either<FaceResponse>> {

        val liveData = MutableLiveData<Either<FaceResponse>>()
        api.identifyFace(request).enqueue(object : Callback<FaceResponse> {

            override fun onResponse(call: Call<FaceResponse>, response: Response<FaceResponse>) {
                if(response != null && response.isSuccessful) {
                    liveData.value = Either.success(response.body())
                } else {
                    liveData.value = Either.error(ApiError.IDENTIFY_FACE, null)
                }
            }
            override fun onFailure(call: Call<FaceResponse>, t: Throwable) {
                liveData.value = Either.error(ApiError.IDENTIFY_FACE, null)
            }
        })
        return liveData
    }

    override fun trainFace(request: TrainFaceRequest): LiveData<Either<TrainResponse>> {
        val liveData = MutableLiveData<Either<TrainResponse>>()
        api.trainFace(request).enqueue(object : Callback<TrainResponse>{

            override fun onResponse(call: Call<TrainResponse>, response: Response<TrainResponse>) {
                if(response != null && response.isSuccessful) {
                    liveData.value = Either.success(response.body())
                } else {
                    liveData.value = Either.error(ApiError.TRAIN_FACE, null)
                }
            }

            override fun onFailure(call: Call<TrainResponse>, t: Throwable) {
                liveData.value = Either.error(ApiError.TRAIN_FACE, null)
            }
        })
        return liveData
    }

    override fun compareFaces(request: CompareFaceRequest): LiveData<Either<CompareResponse>> {
        val liveData = MutableLiveData<Either<CompareResponse>>()
        api.compareFace(request).enqueue(object : Callback<CompareResponse>{

            override fun onResponse(call: Call<CompareResponse>, response: Response<CompareResponse>) {
                if(response != null && response.isSuccessful) {
                    liveData.value = Either.success(response.body())
                } else {
                    liveData.value = Either.error(ApiError.COMPARE_FACE, null)
                }
            }

            override fun onFailure(call: Call<CompareResponse>, t: Throwable) {
                liveData.value = Either.error(ApiError.COMPARE_FACE, null)
            }
        })
        return liveData
    }

    override fun identifyLicensePlate(base64String: String): LiveData<Either<OpenALPRResponse>> {

        val liveData = MutableLiveData<Either<OpenALPRResponse>>()

        alprApi.identifyLicensePlate(base64String).enqueue(object : Callback<OpenALPRResponse>{

            override fun onResponse(call: Call<OpenALPRResponse>, response: Response<OpenALPRResponse>) {
                if(response != null && response.isSuccessful) {
                    liveData.value = Either.success(response.body())
                } else {
                    liveData.value = Either.error(ApiError.LICENSE_PLATE, null)
                }
            }

            override fun onFailure(call: Call<OpenALPRResponse>, t: Throwable) {
                liveData.value = Either.error(ApiError.LICENSE_PLATE, null)
            }
        })
        return liveData
    }

    override fun checkLiveness(request: LivenessRequest): LiveData<Either<LivenessResponse>> {

        val liveData = MutableLiveData<Either<LivenessResponse>>()

        faceSassApi.checkLivesess(request).enqueue(object : Callback<LivenessResponse>{

            override fun onResponse(call: Call<LivenessResponse>, response: Response<LivenessResponse>) {
                if(response != null && response.isSuccessful) {
                    liveData.value = Either.success(response.body())
                } else {
                    liveData.value = Either.error(ApiError.LIVENESS, null)
                }
            }
            override fun onFailure(call: Call<LivenessResponse>, t: Throwable) {
                liveData.value = Either.error(ApiError.LIVENESS, null)

            }
        })
        return liveData

    }

    @SuppressLint("Recycle")
    override fun modifyOrientation(activity: Activity, bitmap: Bitmap, uri: Uri): Bitmap {

        val columns = arrayOf(MediaStore.MediaColumns.DATA)
        val c = activity.contentResolver.query(uri, columns, null, null, null)
        if (c == null) {
            Log.d("modifyOrientation", "Could not get cursor")
            return bitmap
        }

        c.moveToFirst()
        Log.d("modifyOrientation", c.getColumnName(0))
        val str = c.getString(0)
        if (str == null) {
            Log.d("modifyOrientation", "Could not get exif")
            return bitmap
        }
        Log.d("modifyOrientation", "get cursor");
        val exifInterface = ExifInterface(c.getString(0)!!)
        val exifR : Int = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        val orientation : Float =
            when (exifR) {
                ExifInterface.ORIENTATION_ROTATE_90 ->  90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

        val mat : Matrix? = Matrix()
        mat?.postRotate(orientation)
        return Bitmap.createBitmap(bitmap as Bitmap, 0, 0, bitmap?.width as Int,
            bitmap.height as Int, mat, true)
    }
}