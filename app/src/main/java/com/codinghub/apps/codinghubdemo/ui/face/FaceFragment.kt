package com.codinghub.apps.codinghubdemo.ui.face


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
import androidx.lifecycle.Observer
import com.codinghub.apps.codinghubdemo.R
import com.codinghub.apps.codinghubdemo.model.objects.responses.FaceResponse
import com.codinghub.apps.codinghubdemo.model.objects.face.Person
import com.codinghub.apps.codinghubdemo.model.objects.error.ApiError
import com.codinghub.apps.codinghubdemo.model.objects.error.Either
import com.codinghub.apps.codinghubdemo.model.objects.error.Status
import com.codinghub.apps.codinghubdemo.viewmodel.FaceViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_face.view.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

class FaceFragment : Fragment() {

    private lateinit var faceViewModel: FaceViewModel

    internal lateinit var identifyButton: Button
    internal lateinit var faceImageView: ImageView

    internal val TAG = FaceFragment::class.java.simpleName

    private val PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    private val IMAGE_GALLERY_CODE = 1002

    var image_uri: Uri? = null
    internal var isTakePhoto: Boolean = false
    internal var isTakingFromCamera: Boolean = false

    var exifData: Uri? = null
    internal lateinit var snapImage: Bitmap

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

        activity?.setTitle("Facial Recognition")
       // activity?.toolbar?.menu?.findItem(R.id.action_settings)?.isVisible = true
        val view: View = inflater.inflate(R.layout.fragment_face, container, false)

        faceViewModel = ViewModelProviders.of(this).get(FaceViewModel::class.java)

        view.faceImageView.setOnClickListener {

            showBottomSheetDialog()
        }

        view.identifyButton.setOnClickListener {
            identifyFace()
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
            identifyButton = it.findViewById(R.id.identifyButton)
            faceImageView = it.findViewById(R.id.faceImageView)

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
                faceImageView.setImageBitmap(faceViewModel.modifyOrientation(activity!! ,snapImage, image_uri!!))
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
                faceImageView.setImageBitmap(faceViewModel.modifyOrientation(activity!! ,snapImage, exifData!!))
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

    private fun identifyFace() {

        if (!isTakePhoto) {
            Toast.makeText(activity!!.applicationContext, "Please take a photo.", Toast.LENGTH_LONG).show()
        } else {

            val base64 = getImageBase64(faceImageView)

            identifyButton.text = "Identifying..."
            identifyButton.isEnabled = false

            faceViewModel.identifyFace(base64).observe(this, Observer<Either<FaceResponse>> { either ->
                if (either?.status == Status.SUCCESS && either.data != null) {
                    if (either.data.ret == 0) {
                       // Log.d(TAG, "Student : ${either.data.students}")
                        //configureRoomViewPager(either.data.students)
                        if (either.data.similars[0].similarity > 90) {
                            showDialog(either.data.similars[0])
                        }
                        else {
                            Toast.makeText(context,"Face not found or similarity less than 90%", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                       // emptyStudentLayout.visibility = View.VISIBLE
                        Toast.makeText(context,"Error retrieving face.", Toast.LENGTH_SHORT).show()
                    }

                    identifyButton.text = "Identify"
                    identifyButton.isEnabled = true
                } else {
                    if (either?.error == ApiError.IDENTIFY_FACE) {
                        Toast.makeText(context,"Error retrieving face.", Toast.LENGTH_SHORT).show()
                    }
                    identifyButton.text = "Identify"
                    identifyButton.isEnabled = true
                }
            })
        }
    }

    private fun showDialog(person: Person) {
        val dialogBuilder = AlertDialog.Builder(activity)
        val dialogView = this.layoutInflater.inflate(R.layout.dialog_person, null)
        dialogBuilder.setView(dialogView)

        dialogBuilder.setTitle("Person")

        val percentEditText = dialogView.findViewById<TextView>(R.id.percentEditText)
        val personNameEditText = dialogView.findViewById<TextView>(R.id.personNameEditText)
        val personIDEditText = dialogView.findViewById<TextView>(R.id.personIDEditText)
        val personImageView1 = dialogView.findViewById<ImageView>(R.id.personImageView1)
        val personImageView2 = dialogView.findViewById<ImageView>(R.id.personImageView2)

        val df = DecimalFormat("##.##")
        df.roundingMode = RoundingMode.CEILING

        percentEditText.text = "Similarity : " + df.format(person.similarity) + "%"
        personNameEditText.text = "Name : ${person.name}"
        personIDEditText.text = "ID : ${person.person_id}"

        if (isTakingFromCamera == true) {
            personImageView1.setImageBitmap(faceViewModel.modifyOrientation(activity!! ,snapImage, image_uri!!))
        } else {
            personImageView1.setImageBitmap(faceViewModel.modifyOrientation(activity!! ,snapImage, exifData!!))
        }

        Picasso.get().load("http://103.208.27.9:8041/image/train/${person.image}").into(personImageView2)

        dialogBuilder.setNegativeButton("OK") { _, _->
            //pass
        }

        val dialog = dialogBuilder.create()
        dialog.show()

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
