package com.codinghub.apps.codinghubdemo.ui.addnewface

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
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.codinghub.apps.codinghubdemo.R
import com.codinghub.apps.codinghubdemo.model.objects.error.Either
import com.codinghub.apps.codinghubdemo.model.objects.responses.TrainResponse
import com.codinghub.apps.codinghubdemo.viewmodel.AddNewFaceViewModel
import com.google.gson.Gson
import com.google.gson.JsonParseException
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.codinghub.apps.codinghubdemo.model.objects.error.ApiError
import com.codinghub.apps.codinghubdemo.model.objects.error.Status
import kotlinx.android.synthetic.main.activity_new_person.*
import kotlinx.android.synthetic.main.bottom_sheet.view.*
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class AddNewFaceActivity : AppCompatActivity() {

    private lateinit var addNewFaceViewModel: AddNewFaceViewModel

    internal lateinit var fullNameTextView: TextView
    internal lateinit var personIDTextView: TextView

    internal var isTakePhoto : Boolean = false

    var image_uri: Uri? = null
    var exifData: Uri? = null
    internal lateinit var snapImage: Bitmap

    internal val TAG = AddNewFaceActivity::class.java.simpleName

    private val PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    private val IMAGE_GALLERY_CODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_person)

        addNewFaceViewModel = ViewModelProviders.of(this).get(AddNewFaceViewModel::class.java)

        assert(supportActionBar != null)   //null check
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        this.setTitle("Add New Person")

        fullNameTextView = findViewById(R.id.newNameEditText)
        personIDTextView = findViewById(R.id.personIDTextView)

        addNewPersonImageView.setOnClickListener {
            showBottomSheetDialog()
        }

        addButton.setOnClickListener {
            addNewPerson()
        }

        personIDTextView.text = generatePersonID()

    }

    private fun showBottomSheetDialog() {

        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = this.layoutInflater.inflate(R.layout.bottom_sheet, null)
        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(true)
        val dialog = dialogBuilder.create()

        dialogView.textViewCamera.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || ActivityCompat.checkSelfPermission(this.applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun openCamera() {

        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    //       Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == Activity.RESULT_OK) {
            isTakePhoto = true

            val ins : InputStream? = contentResolver.openInputStream(image_uri)
            val snapImage : Bitmap? = BitmapFactory.decodeStream(ins)
            ins?.close()


            if (snapImage != null) {
                addNewPersonImageView.setImageBitmap(addNewFaceViewModel.modifyOrientation(this ,snapImage, image_uri!!))

                // (findViewById(R.id.imageView) as ImageView).setImageBitmap(pictureTurn(img, uri));
            }

        }

        else if (requestCode == IMAGE_GALLERY_CODE && resultCode == Activity.RESULT_OK) {

            isTakePhoto = true

            exifData = data?.data!!
            val ins: InputStream? = contentResolver.openInputStream(exifData)
            snapImage = BitmapFactory.decodeStream(ins)

            if (snapImage != null) {
                addNewPersonImageView.setImageBitmap(addNewFaceViewModel.modifyOrientation(this ,snapImage, exifData!!))
            }
        }
    }

    private fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    private fun getImageBase64(image: ImageView): String {

        val bitmap = (image.drawable as BitmapDrawable).bitmap

        val resizedBitmap = resizeBitmap(bitmap,bitmap.width / 2,bitmap.height / 2)

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

    private fun addNewPerson() {
        val fullName: String = fullNameTextView.text.toString()
        if (!isTakePhoto) {
            showAlert("Alert", "Please take photo of person face.")
        }
        else if (fullName.trim().length == 0) {
            showAlert("Alert", "Please enter full name.")
        } else {

            val base64 = getImageBase64(addNewPersonImageView)

            addButton.text = "Uploding..."
            addButton.isEnabled = false

            addNewFaceViewModel.trainFace(base64, fullName, personIDTextView.text.toString()).observe(this, Observer<Either<TrainResponse>> {either ->
                if (either?.status == Status.SUCCESS && either.data != null) {
                    if (either.data.ret == 0) {

                        Toast.makeText(this,"Train face complete", Toast.LENGTH_LONG).show()

                        finish()
                    } else {
                        // emptyStudentLayout.visibility = View.VISIBLE
                        Toast.makeText(this,"Error training face.", Toast.LENGTH_SHORT).show()
                    }
                    addButton.text = "Add New Person"
                    addButton.isEnabled = true

                } else {
                    if (either?.error == ApiError.IDENTIFY_FACE) {
                        Toast.makeText(this,"Error training face.", Toast.LENGTH_SHORT).show()
                    }
                    addButton.text = "Add New Person"
                    addButton.isEnabled = true
                }
            })
        }
    }

    private fun showAlert(title: String, subtitle: String) {
        val builder = AlertDialog.Builder(this@AddNewFaceActivity)
        builder.setTitle(title)
        builder.setMessage(subtitle)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun generatePersonID() : String {
        var pidString = "CDH" + getCurrentDate()
        Log.d(TAG, "Genrate PID : " + pidString)
        return pidString
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyyMMddHHmmss")
        val currentDate = sdf.format(Date())
        return currentDate.toString()
    }
}
