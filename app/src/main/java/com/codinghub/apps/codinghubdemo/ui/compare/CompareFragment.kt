package com.codinghub.apps.codinghubdemo.ui.compare


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
import androidx.core.app.ActivityCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders

import com.codinghub.apps.codinghubdemo.R
import com.codinghub.apps.codinghubdemo.app.Injection
import com.codinghub.apps.codinghubdemo.model.objects.error.ApiError
import com.codinghub.apps.codinghubdemo.model.objects.error.Either
import com.codinghub.apps.codinghubdemo.model.objects.error.Status
import com.codinghub.apps.codinghubdemo.model.objects.responses.CompareResponse
import com.codinghub.apps.codinghubdemo.viewmodel.CompareViewModel
import com.google.gson.Gson
import com.google.gson.JsonParseException
import kotlinx.android.synthetic.main.bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_compare.view.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

class CompareFragment : Fragment() {

    private lateinit var compareViewModel: CompareViewModel

    internal lateinit var compareButton: Button
    internal lateinit var compareImageView1: ImageView
    internal lateinit var compareImageView2: ImageView
    internal lateinit var compareTextView: TextView

    internal val TAG = CompareFragment::class.java.simpleName

    private val PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_CODE1 = 1001
    private val IMAGE_CAPTURE_CODE2 = 1002

    private val IMAGE_GALLERY_CODE1 = 1021
    private val IMAGE_GALLERY_CODE2 = 1022

    var image_uri: Uri? = null
    var exifData: Uri? = null

    internal var isTakePhoto1: Boolean = false
    internal var isTakePhoto2: Boolean = false

    companion object {
        fun newInstance(): CompareFragment {
            val fragment = CompareFragment()
            return fragment
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        activity?.setTitle("Facial Comparision")

        val view: View = inflater.inflate(R.layout.fragment_compare, container, false)

        compareViewModel = ViewModelProviders.of(this).get(CompareViewModel::class.java)

        view.compareImageView1.setOnClickListener {
            showBottomSheetDialog(IMAGE_CAPTURE_CODE1, IMAGE_GALLERY_CODE1)
        }

        view.compareImageView2.setOnClickListener {
            showBottomSheetDialog(IMAGE_CAPTURE_CODE2, IMAGE_GALLERY_CODE2)
        }

        view.compareButton.setOnClickListener {
            compareFace()
        }

        return view
    }

    private fun showBottomSheetDialog(cameraCode: Int, galleryCode: Int) {

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
                    openCamera(cameraCode)
                    dialog.dismiss()
                }

            } else {
                openCamera(cameraCode)
                dialog.dismiss()
            }

        }
        dialogView.textViewGallery.setOnClickListener {
            openGallery(galleryCode)
            dialog.dismiss()
        }

        dialog.show()

    }

    private fun openGallery(galleryCode: Int) {
        val intent: Intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_PICK)
        startActivityForResult(intent, galleryCode)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        view?.let {

            compareButton = it.findViewById(R.id.compareButton)
            compareImageView1 = it.findViewById(R.id.compareImageView1)
            compareImageView2 = it.findViewById(R.id.compareImageView2)
            compareTextView = it.findViewById(R.id.compareTextView)


        }
    }


    private fun openCamera(imageCaptureCode: Int) {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")

        image_uri = activity!!.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, imageCaptureCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG, "requestCode" + requestCode)
        Log.d(TAG, "resultCode" + resultCode)



            if (requestCode == IMAGE_CAPTURE_CODE1 && resultCode == Activity.RESULT_OK) {

                isTakePhoto1 = true
                val ins : InputStream? = activity!!.contentResolver.openInputStream(image_uri)
                val img : Bitmap? = BitmapFactory.decodeStream(ins)
                ins?.close()
                if (img != null) {
                    compareImageView1.setImageBitmap(compareViewModel.modifyOrientation(activity!!, img!!, image_uri!!))
                }

            } else if (requestCode == IMAGE_CAPTURE_CODE2 && resultCode == Activity.RESULT_OK) {
                isTakePhoto2 = true
                val ins : InputStream? = activity!!.contentResolver.openInputStream(image_uri)
                val img : Bitmap? = BitmapFactory.decodeStream(ins)
                ins?.close()
                if (img != null) {
                    compareImageView2.setImageBitmap(compareViewModel.modifyOrientation(activity!!, img!!, image_uri!!))
                }

            } else if (requestCode == IMAGE_GALLERY_CODE1 && resultCode == Activity.RESULT_OK) {
                isTakePhoto1 = true
                exifData = data?.data!!
                val ins: InputStream? = activity!!.contentResolver.openInputStream(exifData)
                val img : Bitmap? = BitmapFactory.decodeStream(ins)

                if (img != null) {
                    compareImageView1.setImageBitmap(compareViewModel.modifyOrientation(activity!! ,img!!, exifData!!))
                }

            } else if (requestCode == IMAGE_GALLERY_CODE2 && resultCode == Activity.RESULT_OK) {
                isTakePhoto2 = true
                exifData = data?.data!!
                val ins: InputStream? = activity!!.contentResolver.openInputStream(exifData)
                val img : Bitmap? = BitmapFactory.decodeStream(ins)

                if (img != null) {
                    compareImageView2.setImageBitmap(compareViewModel.modifyOrientation(activity!! ,img!!, exifData!!))
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

    private fun compareFace() {
        if (!isTakePhoto1 || !isTakePhoto2) {
            Toast.makeText(activity!!.applicationContext, "Please take a photo.", Toast.LENGTH_LONG).show()
        } else {

            val base641 = getImageBase64(compareImageView1)
            val base642 = getImageBase64(compareImageView2)

            compareButton.text = "Comparing..."
            compareButton.isEnabled = false

            compareViewModel.compareFace(base641, base642)
                .observe(this, androidx.lifecycle.Observer<Either<CompareResponse>> { either ->
                    if (either?.status == Status.SUCCESS && either.data != null) {
                        if (either.data.ret == 0) {
                            compareTextView.text = either.data.similarity.toString()


                        } else {
                            // emptyStudentLayout.visibility = View.VISIBLE
                            Toast.makeText(context, "Error comparing face.", Toast.LENGTH_SHORT).show()
                        }
                        compareButton.text = "Compare"
                        compareButton.isEnabled = true
                    } else {
                        if (either?.error == ApiError.COMPARE_FACE) {
                            Toast.makeText(context, "Error comparing face.", Toast.LENGTH_SHORT).show()
                        }
                        compareButton.text = "Compare"
                        compareButton.isEnabled = true
                    }
                })
        }
    }

    private fun deletePhoto(filePath: String){
        val file = File(filePath)
        if (file.exists()) {
            if (file.delete()) {
                Log.d(TAG, "File Delete : " + filePath)
            } else {
                Log.d(TAG, "File Not Delete : " + filePath)
            }
        }

    }

}
