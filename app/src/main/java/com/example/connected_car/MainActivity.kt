package com.example.connected_car

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import java.io.*

class MainActivity : AppCompatActivity() {
    internal val SELECT_FILE = 1
    internal val REQUEST_CAMERA = 0
    internal lateinit var userChoosenTask: String
    internal lateinit var imageSelected: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnSelect = findViewById(R.id.bSelectImage) as Button
        btnSelect.setOnClickListener{
            selectImage()
        }
        imageSelected = findViewById(R.id.imageToUpload) as ImageView
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

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data)
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data)
        } else {
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle("You don't select any images!")
            builder.show()
        }
    }

    private fun onCaptureImageResult(data: Intent?) {
        val thumbnail = data?.extras?.get("data") as Bitmap
        val bytes = ByteArrayOutputStream()
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes)

        val destination = File(Environment.getExternalStorageDirectory(),
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
        }

        imageSelected.setImageBitmap(thumbnail)
    }

    private fun onSelectFromGalleryResult(data: Intent?) {
        if (data != null) {
            var bm: Bitmap? = null
            try {
                bm = MediaStore.Images.Media.getBitmap(applicationContext.contentResolver, data.data)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            imageSelected.setImageBitmap(bm)
        }
    }
}
