@file:Suppress("DEPRECATION")

package com.example.assignment_2
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage


class MainActivity : AppCompatActivity() {
    lateinit var upload: ImageView
    var imageuri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        upload = findViewById(R.id.uploadPdf)
        upload.setOnClickListener {
            val galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.type = "application/pdf"
            startActivityForResult(galleryIntent, 1)
        }

    }

    var dialog: ProgressDialog? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {

            dialog = ProgressDialog(this)
            dialog!!.setMessage("Uploading")

            dialog!!.show()
            imageuri = data!!.data
            val timestamp = "" + System.currentTimeMillis()
            val storageReference = FirebaseStorage.getInstance().reference
            Toast.makeText(this@MainActivity, imageuri.toString(), Toast.LENGTH_SHORT).show()

            val filepath = storageReference.child("$timestamp.pdf")
            Toast.makeText(this@MainActivity, filepath.name, Toast.LENGTH_SHORT).show()
            filepath.putFile(imageuri!!)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    return@continueWithTask filepath.downloadUrl
                }
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        dialog!!.dismiss()
                        val uri = task.result
                        val myurl: String
                        myurl = uri.toString()
                        Toast.makeText(this@MainActivity, "Uploaded Successfully", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        dialog!!.dismiss()
                        Toast.makeText(this@MainActivity, "UploadedFailed", Toast.LENGTH_SHORT).show()
                    }
                }

        }
    }
}
