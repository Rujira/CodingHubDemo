package com.codinghub.apps.codinghubdemo.ui.liveness


import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.Observer

import com.codinghub.apps.codinghubdemo.R
import com.codinghub.apps.codinghubdemo.model.objects.error.ApiError
import com.codinghub.apps.codinghubdemo.model.objects.error.Either
import com.codinghub.apps.codinghubdemo.model.objects.error.Status
import com.codinghub.apps.codinghubdemo.model.objects.responses.LivenessResponse
import com.codinghub.apps.codinghubdemo.ui.face.FaceFragment
import com.codinghub.apps.codinghubdemo.viewmodel.LivenessViewModel
import kotlinx.android.synthetic.main.bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_liveness.view.*
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*

class LivenessFragment : Fragment() {

    private lateinit var livenessViewModel: LivenessViewModel

    internal lateinit var livenessButton: Button
    internal lateinit var livenessImageView: ImageView

    private val PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    private val IMAGE_GALLERY_CODE = 1002

    var image_uri: Uri? = null
    internal var isTakePhoto: Boolean = false
    internal var isTakingFromCamera: Boolean = false

    var exifData: Uri? = null
    internal lateinit var snapImage: Bitmap

    internal val TAG = LivenessFragment::class.java.simpleName

    companion object {
        fun newInstance(): FaceFragment {
            val fragment = FaceFragment()
            return  fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        activity?.setTitle("Liveness Detection")
        // activity?.toolbar?.menu?.findItem(R.id.action_settings)?.isVisible = true
        val view: View = inflater.inflate(R.layout.fragment_liveness, container, false)

        livenessViewModel = ViewModelProviders.of(this).get(LivenessViewModel::class.java)

        view.livenessImageView.setOnClickListener {

            showBottomSheetDialog()
        }

        view.livenessButton.setOnClickListener {
            checkliveness()
        }

        return view
    }

    private fun showBottomSheetDialog() {

        val dialogBuilder = AlertDialog.Builder(activity)
        val dialogView = this.layoutInflater.inflate(R.layout.bottom_sheet, null)
        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(true)
        val dialog = dialogBuilder.create()

        dialogView.textViewCamera.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(activity!!.applicationContext, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || ActivityCompat.checkSelfPermission(activity!!.applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    val permission = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestPermissions(permission, PERMISSION_CODE)
                } else {
                    openCamera()
                    dialog.dismiss()
                }

            } else {
                openCamera()
                dialog.dismiss()
            }

        }
        dialogView.textViewGallery.setOnClickListener {
            openGallery()
            dialog.dismiss()
        }

        dialog.show()

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        view?.let {
            livenessButton = it.findViewById(R.id.livenessButton)
            livenessImageView = it.findViewById(R.id.livenessImageView)

        }
    }

    private fun openGallery() {
        val intent: Intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_PICK)
        startActivityForResult(intent, IMAGE_GALLERY_CODE)
    }

    private fun openCamera() {

        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")

        image_uri = activity!!.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == Activity.RESULT_OK) {

            isTakePhoto = true
            isTakingFromCamera = true

            Log.d(TAG, image_uri.toString())
            val ins : InputStream? = activity!!.contentResolver.openInputStream(image_uri)
            snapImage = BitmapFactory.decodeStream(ins)
            ins?.close()

            if (snapImage != null) {
                livenessImageView.setImageBitmap(livenessViewModel.modifyOrientation(activity!! ,snapImage, image_uri!!))
            }
        }

        else if (requestCode == IMAGE_GALLERY_CODE && resultCode == Activity.RESULT_OK) {

            isTakePhoto = true
            isTakingFromCamera = false
            exifData = data?.data!!
            Log.d(TAG, exifData.toString())

            val ins: InputStream? = activity!!.contentResolver.openInputStream(exifData)
            snapImage = BitmapFactory.decodeStream(ins)
            ins?.close()

            if (snapImage != null) {
                livenessImageView.setImageBitmap(livenessViewModel.modifyOrientation(activity!! ,snapImage, exifData!!))
            }
        }


    }

    private fun getImageBase64(image: ImageView): String {

        val bitmap = (image.drawable as BitmapDrawable).bitmap

        val resizedBitmap = resizeBitmap(bitmap,800,1066)

        val stream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val byteArray = stream.toByteArray()

        var base64String: String

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            Log.d(TAG, "Android O")
            base64String = Base64.getEncoder().encodeToString(byteArray)

        } else {
            Log.d(TAG, "Android Other")
            base64String = android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
        }
        return base64String
    }

    private fun resizeBitmap(bitmap: Bitmap, width:Int, height:Int): Bitmap {

        return Bitmap.createScaledBitmap(
            bitmap,
            width,
            height,
            false
        )
    }

    private fun checkliveness() {

        if (!isTakePhoto) {
            Toast.makeText(activity!!.applicationContext, "Please take a photo.", Toast.LENGTH_LONG).show()
        } else {

            val base64 = getImageBase64(livenessImageView)

            val database_image_content: String = base64
            val database_image_type: Int = 101
            val query_image_package: String = base64
            val query_image_package_return_image_list: Boolean = true
            val query_image_package_check_same_person: Boolean = true
            val auto_rotate_for_database: Boolean = true
            val true_negative_rate: Double = 99.99

            livenessButton.text = "Checking..."
            livenessButton.isEnabled = false

            livenessViewModel.checkLiveness(
                database_image_content,
                database_image_type,
                query_image_package,
                query_image_package_return_image_list,
                query_image_package_check_same_person,
                auto_rotate_for_database,
                true_negative_rate).observe(this, Observer<Either<LivenessResponse>> { either ->
                if (either?.status == Status.SUCCESS && either.data != null) {

                    Log.d(TAG, "${either.data}")
//                    if (either.data.ret == 0) {
//                        // Log.d(TAG, "Student : ${either.data.students}")
//                        //configureRoomViewPager(either.data.students)
//
//
//                    } else {
//                        // emptyStudentLayout.visibility = View.VISIBLE
//                        Toast.makeText(context,"Error retrieving face.", Toast.LENGTH_SHORT).show()
//                    }

                    livenessButton.text = "Check Liveness"
                    livenessButton.isEnabled = true
                } else {
                    if (either?.error == ApiError.IDENTIFY_FACE) {
                        Toast.makeText(context,"Error retrieving face.", Toast.LENGTH_SHORT).show()
                    }
                    livenessButton.text = "Check Liveness"
                    livenessButton.isEnabled = true
                }
            })
        }
    }

}
