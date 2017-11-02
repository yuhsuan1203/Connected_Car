package com.example.connected_car

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val SELECT_FILE = 1
    private val REQUEST_CAMERA = 0
    internal lateinit var userChoosenTask: String
    internal lateinit var imageSelected: ImageView
    private var filePath: Uri? = null

    internal var storage: FirebaseStorage? = null
    internal var storageReference: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Init Firebase
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        val btnSelect = findViewById(R.id.bSelectImage) as Button
        btnSelect.setOnClickListener{
            selectImage()
        }
        imageSelected = findViewById(R.id.imageToUpload) as ImageView
        val btnUpload = findViewById(R.id.bUploadImage) as Button
        btnUpload.setOnClickListener{
            uploadImage()
        }
    }

    private fun uploadImage() {
        if(filePath != null)
        {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading...")
            progressDialog.show()

            val imageRef = storageReference!!.child("images/" + UUID.randomUUID().toString() + ".jpg")
            imageRef.putFile(filePath!!)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(applicationContext, "Image Uploaded", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        progressDialog.dismiss()
                        Toast.makeText(applicationContext, "Failed", Toast.LENGTH_SHORT).show()
                    }
                    .addOnProgressListener { taskSnapshot ->
                        val progress = 100.0 * taskSnapshot.bytesTransferred/taskSnapshot.totalByteCount
                        progressDialog.setMessage("Uploaded " + progress.toInt() + "%...")
                    }
        }
    }

    private fun selectImage() {
        val items = arrayOf<CharSequence>("Take Photo", "Choose from Library", "Cancel")

        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Add Photo!")
        builder.setItems(items) { dialog, item ->
            val result = Utility.checkPermission(this@MainActivity)

            if (items[item] == "Take Photo") {
                userChoosenTask = "Take Photo"
                if (result)
                    cameraIntent()
            } else if (items[item] == "Choose from Library") {
                userChoosenTask = "Choose from Library"
                if (result)
                    galleryIntent()
            } else if (items[item] == "Cancel") {
                dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun cameraIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    private fun galleryIntent() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data)
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data)
        } else {
            Toast.makeText(applicationContext, "You don't select a image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onCaptureImageResult(data: Intent?) {
        val thumbnail = data?.extras?.get("data") as Bitmap
        val bytes = ByteArrayOutputStream()
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes)

        /*val destination = File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis().toString() + ".jpg")

        val fo: FileOutputStream
        try {
            destination.createNewFile()
            fo = FileOutputStream(destination)
            fo.write(bytes.toByteArray())
            fo.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }*/

        imageSelected.setImageBitmap(thumbnail)
    }

    private fun onSelectFromGalleryResult(data: Intent?) {
        if (data != null) {
            var bm: Bitmap? = null
            try {
                bm = MediaStore.Images.Media.getBitmap(applicationContext.contentResolver, filePath)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            imageSelected.setImageBitmap(bm)
        }
    }
}
