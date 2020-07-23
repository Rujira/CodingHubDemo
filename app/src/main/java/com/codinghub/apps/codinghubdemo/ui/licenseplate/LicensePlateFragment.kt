package com.codinghub.apps.codinghubdemo.ui.licenseplate

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
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
import com.codinghub.apps.codinghubdemo.model.objects.licenseplate.Plate
import com.codinghub.apps.codinghubdemo.model.objects.licenseplate.Province
import com.codinghub.apps.codinghubdemo.model.objects.licenseplate.ProvinceList
import com.codinghub.apps.codinghubdemo.model.objects.error.ApiError.*
import com.codinghub.apps.codinghubdemo.model.objects.error.Either
import com.codinghub.apps.codinghubdemo.model.objects.error.Status
import com.codinghub.apps.codinghubdemo.model.objects.responses.OpenALPRResponse
import com.codinghub.apps.codinghubdemo.viewmodel.LicensePlateViewModel
import com.google.gson.Gson
import com.google.gson.JsonParseException
import kotlinx.android.synthetic.main.bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_license_plate.view.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*


class LicensePlateFragment : Fragment() {

    private lateinit var licensePlateViewModel: LicensePlateViewModel

    internal lateinit var scanButton: Button
    internal lateinit var photoImageView: ImageView

    internal val TAG = Fragment::class.java.simpleName

    private val PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    private val IMAGE_GALLERY_CODE = 1002

    var image_uri: Uri? = null
    internal var isTakePhoto: Boolean = false
    internal var isTakingFromCamera: Boolean = false

    var exifData: Uri? = null
    internal lateinit var snapImage: Bitmap

    companion object {
        fun newInstance(): LicensePlateFragment {
            val fragment = LicensePlateFragment()
            return  fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        activity?.setTitle("License Plate Recognition")

        val view: View = inflater.inflate(R.layout.fragment_license_plate, container, false)

        licensePlateViewModel = ViewModelProviders.of(this).get(LicensePlateViewModel::class.java)

        view.photoImageView.setOnClickListener {
            showBottomSheetDialog()
        }

        view.scanButton.setOnClickListener { v ->
            scanLicensePlate()
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

    private fun openGallery() {
        val intent: Intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_PICK)
        startActivityForResult(intent, IMAGE_GALLERY_CODE)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        view?.let {
            scanButton = it.findViewById(R.id.scanButton)
            photoImageView = it.findViewById(R.id.photoImageView)

        }
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

            val ins : InputStream? = activity!!.contentResolver.openInputStream(image_uri)
            snapImage = BitmapFactory.decodeStream(ins)
            ins?.close()

            if (snapImage != null) {
                photoImageView.setImageBitmap(licensePlateViewModel.modifyOrientation(activity!! ,snapImage, image_uri!!))
            }
        }

        else if (requestCode == IMAGE_GALLERY_CODE && resultCode == Activity.RESULT_OK) {

            isTakePhoto = true
            isTakingFromCamera = false

            exifData = data?.data!!
            val ins: InputStream? = activity!!.contentResolver.openInputStream(exifData)
            snapImage = BitmapFactory.decodeStream(ins)

            if (snapImage != null) {
                photoImageView.setImageBitmap(licensePlateViewModel.modifyOrientation(activity!! ,snapImage, exifData!!))
            }

        }
    }

    private fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
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

    private fun identifyLicensePlate() {

    }

    private fun scanLicensePlate() {

        scanButton.text = "Scaning..."
        scanButton.isEnabled = false

        if (!isTakePhoto) {

            Toast.makeText(activity!!.applicationContext, "กรุณาถ่ายรูป", Toast.LENGTH_LONG).show()
            scanButton.text = "Scan"
            scanButton.isEnabled = true

        } else {

            val base64 = getImageBase64(photoImageView)

            scanButton.text = "Scaning..."
            scanButton.isEnabled = false

            licensePlateViewModel.identifyVehicle(base64).observe(this, androidx.lifecycle.Observer<Either<OpenALPRResponse>> { either ->

                if (either?.status == Status.SUCCESS && either.data != null) {
                    if (either.data.results.isNotEmpty()) {
                        showDialog(either.data.results.first())
                    } else {
                        Toast.makeText(context,"Could not find plate", Toast.LENGTH_SHORT).show()
                    }
                    scanButton.text = "Scan"
                    scanButton.isEnabled = true
                } else {
                    if (either?.error == LICENSE_PLATE) {
                        Toast.makeText(context,"Error retrieving data.", Toast.LENGTH_SHORT).show()

                    }
                    scanButton.text = "Scan"
                    scanButton.isEnabled = true
                }
            })

        }

    }

    private fun showDialog(plate: Plate) {
        val dialogBuilder = AlertDialog.Builder(activity)
        val dialogView = this.layoutInflater.inflate(R.layout.dialog_vehicle, null)
        dialogBuilder.setView(dialogView)

        val df = DecimalFormat("##.##")
        df.roundingMode = RoundingMode.CEILING

        dialogBuilder.setTitle("Plate Detail : Confidence ${df.format(plate.confidence)}%")

        val plateEditText = dialogView.findViewById<TextView>(R.id.plateEditText)
        val provinceEditText = dialogView.findViewById<TextView>(R.id.regionEditText)
        val makeEditText = dialogView.findViewById<TextView>(R.id.makeEditText)
        val modelEditText = dialogView.findViewById<TextView>(R.id.modelEditText)
        val colorEditText = dialogView.findViewById<TextView>(R.id.colorEditText)
        val detailImageView = dialogView.findViewById<ImageView>(R.id.detialImageView)

        plateEditText.text = plate.plate
        provinceEditText.text = checkProvince(plate.region)
        makeEditText.text = "${plate.vehicle.make.first().name} (${df.format(plate.vehicle.make.first().confidence)}%)"
        modelEditText.text = "${plate.vehicle.make_model.first().name} (${df.format(plate.vehicle.make_model.first().confidence)}%)"
        colorEditText.text = "${plate.vehicle.color.first().name} (${df.format(plate.vehicle.color.first().confidence)}%)"

        if(isTakingFromCamera == true) {
            detailImageView.setImageBitmap(licensePlateViewModel.modifyOrientation(activity!! ,snapImage, image_uri!!))
        } else {
            detailImageView.setImageBitmap(licensePlateViewModel.modifyOrientation(activity!! ,snapImage, exifData!!))
        }



        dialogBuilder.setNegativeButton("OK") { _, _->
            //pass
        }

        val dialog = dialogBuilder.create()

        dialog.show()

    }

    private fun checkProvince(provinceID: String): String {

        var targetProvince = ""
        val json: String?
        val inputStream: InputStream = context!!.assets.open("province.json")
        json = inputStream.bufferedReader().use { it.readText() }

        try {

            val jsonArray = Gson().fromJson(json, ProvinceList::class.java)
            val provinceList = mutableListOf<Province>()

            for (province in jsonArray.province) {
                provinceList.add(province)
            }

            val lastTwo = provinceID.takeLast(2)


            for (province in provinceList) {
                if (province.pid.toString() == lastTwo) {
                    targetProvince = province.name
                }
            }

        } catch (e : JsonParseException) {
           // Toast.makeText(activity?.applicationContext, "JSON จังหวัดผิดพลาด", Toast.LENGTH_LONG).show()
        }

        return targetProvince
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
