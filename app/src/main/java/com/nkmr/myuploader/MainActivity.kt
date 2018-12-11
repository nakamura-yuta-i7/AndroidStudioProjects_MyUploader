package com.nkmr.myuploader

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.provider.MediaStore
import android.content.Intent
import android.content.IntentSender
import android.graphics.Bitmap
import android.graphics.DrawFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.drive.DriveFile


class MainActivity : GoogleSignInActivityBase() {

    private val RESULT_CAMERA: Int = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tappedCameraButton.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, RESULT_CAMERA)
        }

        sign_in_button.setOnClickListener {
            signIn()
        }


    }

    override fun handleSignInResult(result: GoogleSignInResult) {
        super.handleSignInResult(result)
        if (result.isSuccess) {
            sign_in_button.visibility = View.GONE
            loginEmailText.text = result.signInAccount!!.email
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == RESULT_CAMERA) {
            val bitmap = data.extras.get("data") as Bitmap
            pictureView.setImageBitmap(bitmap)
        }
    }

    fun createRootFolder(v: View) {
        val driveApi = MyGoogleDrive(MyApplication.mContext)
//        driveApi.createRootFolder("test1", onSuccess = {
//            driveFolderResult ->
//            Log.d("driveFolderResult.status", driveFolderResult.status.toString())
//            driveFolderResult.driveFolder.getMetadata(mGoogleApiClient).setResultCallback {
//                metadataResult ->
//                Log.d("metadataResult.metadata", metadataResult.metadata.toString())
//            }
////            Log.d("driveFolderResult.driveFolder.resourceId", driveFolderResult.driveFolder.driveId)
//        })
        // DriveId:CAESABiWkwEgkPfFv6hXKAE=
    }

    fun tappedUploadButton(v: View) {
        Log.d("tappedUploadButton", "koko1")
        val driveApi = MyGoogleDrive(MyApplication.mContext)
        val folderId = "0B0vk9XZ3993_S0hKX1cxelhJQ28"
        var name = "Android Photo.jpg"

//        driveApi.testRequest(folderId)

        driveApi.folderExists(folderId, onSuccess = {
            exists ->
            Log.d("folderExists", exists.toString())
            Log.d("folderExists", "アップロードを開始します")
            var bitmap = (pictureView.drawable as BitmapDrawable).bitmap
            driveApi.uploadBitmap(folderId = folderId ,bitmap = bitmap, name = name) {
                driveFileResult ->

                Log.d("driveFileResult.status.toString()", driveFileResult.status.toString())
                driveFileResult.driveFile.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY) {
                    i: Long, l: Long ->
                    Log.d("driveFile.open", "i:{$i}, l:${l}")
                }
            }
        })

    }
}
