@file:Suppress("DEPRECATION")

package com.example.assignment_2
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.File


class MainActivity : AppCompatActivity() {
    lateinit var upload: Button
    lateinit var download: Button
    lateinit var pdfuri: Uri
    val reqCode:Int = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        upload = findViewById(R.id.uploadPdf)
        download = findViewById(R.id.downloadPdf)
        val storage = Firebase.storage
        val ref = storage.reference

        upload.setOnClickListener {
            val galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.type = "application/pdf"
            startActivityForResult(galleryIntent, reqCode)

        }

        download.setOnClickListener {
            val fileName = "Pdf"
            val fileExtension = ".pdf"
            val url = "https://firebasestorage.googleapis.com/v0/b/appkotlinmovies.appspot.com/o/Pdf?alt=media&token=187369b6-6cfa-4e66-95e5-e2ffe19825f4"
            val destinationDirectory = Environment.DIRECTORY_DOWNLOADS
            downloadFile(this, fileName, fileExtension, destinationDirectory, url)
            val localFile = File.createTempFile("file", "pdf")
            ref.child("Pdf/" + pdfuri.lastPathSegment)
                .getFile(localFile)
                .addOnSuccessListener {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(
                        FileProvider.getUriForFile(
                            applicationContext,
                            applicationContext.packageName + ".provider",
                            localFile
                        ),
                        "application/pdf"
                    )
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(
                        applicationContext,
                        " download file",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

    }

    var dialog: ProgressDialog? = null
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {

            dialog = ProgressDialog(this)
            dialog!!.setMessage("Uploading")

            dialog!!.show()
            pdfuri = data!!.data!!
            val timestamp = "" + System.currentTimeMillis()
            val storageReference = FirebaseStorage.getInstance().reference
            Toast.makeText(this, pdfuri.toString(), Toast.LENGTH_SHORT).show()

            val filepath = storageReference.child("$timestamp.pdf")
            Toast.makeText(this, filepath.name, Toast.LENGTH_SHORT).show()
            filepath.putFile(pdfuri)
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
                    } else {
                        dialog!!.dismiss()
                    }
                }

        }
    }

    private fun downloadFile(
        context: Context,
        fileName: String,
        fileExtension: String,
        destinationDirectory: String?,
        url: String?
    ): Long {
        val downloadmanager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalFilesDir(
            context,
            destinationDirectory,
            fileName + fileExtension
        )
        return downloadmanager.enqueue(request)
    }
}
