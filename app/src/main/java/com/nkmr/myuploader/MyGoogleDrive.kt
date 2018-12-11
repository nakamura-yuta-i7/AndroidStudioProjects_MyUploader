package com.nkmr.myuploader

import android.app.Activity
import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.Drive
import com.google.android.gms.drive.DriveFolder
import com.google.android.gms.drive.MetadataChangeSet
import android.content.IntentSender.SendIntentException
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.drive.DriveApi.DriveContentsResult
import com.google.android.gms.drive.DriveFile
import com.google.api.client.googleapis.media.MediaHttpUploader
import java.io.ByteArrayOutputStream
import java.io.IOException


class MyGoogleDrive(application: MyApplication): MyGoogleApiBase(application) {

    var googleApi: GoogleApiClient
    init {
        googleApi = application.googleApiClient!!
        if (googleApi == null) {
            throw Exception("GoogleApi Not Valid.")
        }
        if (!canAuthorized()) {
            throw Exception("GoogleApi Not Authorized.")
        }
    }

    fun canAuthorized(): Boolean {
        Log.d("canAuthorized", "koko1")
        val optionalPendingResult = Auth.GoogleSignInApi.silentSignIn(googleApi)
        Log.d("canAuthorized", "koko2")
        return optionalPendingResult.isDone
    }

    fun folderExists(id: String, onSuccess: (exists: Boolean) -> Unit) {
        Log.d("folderExists", "koko1")
        val folder = Drive.DriveApi.fetchDriveId(googleApi, id)
        folder.setResultCallback { onSuccess(it.driveId != null) }
    }

    fun testRequest(folderId: String) {
        Drive.DriveApi.fetchDriveId(googleApi, folderId).setResultCallback {
            driveIdResult ->
            Log.d("driveIdResult.status", driveIdResult.status.toString())
            Log.d("driveIdResult.driveId", driveIdResult.driveId.toString())
        }
    }

    fun uploadBitmap(folderId: String, bitmap: Bitmap, name: String, callback: (DriveFolder.DriveFileResult) -> Unit) {

        Log.d("uploadBitmap.folderId", folderId)
        val image = bitmap
        Drive.DriveApi.newDriveContents(googleApi)
            .setResultCallback { result ->
                if (!result.status.isSuccess) {
                    Log.i("uploadBitmap", "Failed to create new contents.")
                    return@setResultCallback
                }

                Log.i("uploadBitmap", "New contents created.")
                val outputStream = result.driveContents.outputStream
                val bitmapStream = ByteArrayOutputStream()
                image.compress(Bitmap.CompressFormat.JPEG, 100, bitmapStream)
                try {
                    outputStream.write(bitmapStream.toByteArray())
                } catch (e1: IOException) {
                    Log.i("uploadBitmap", "Unable to write file contents.")
                }
                val metadataChangeSet = MetadataChangeSet.Builder()
                    .setMimeType("image/jpg")
                    .setTitle(name)
                    .build()

                Drive.DriveApi.fetchDriveId(googleApi, folderId).setResultCallback {
                    driveIdResult ->

                    Log.d("driveIdResult2", driveIdResult.toString())
                    Log.d("driveIdResult2.status", driveIdResult.status.toString())

                    var folder = driveIdResult.driveId.asDriveFolder()


                    Log.d("fetchDriveId", folder.toString() )


                    var creation = folder.createFile(googleApi, metadataChangeSet, result.driveContents)

                    creation.setResultCallback {
                        driveFileResult ->
//                        var listner = MediaHttpUploader.UploadState.
                        Log.d("createFile.result.status", result.status.toString())
                        callback(driveFileResult)
                    }
                }
            }

    }

    fun createRootFolder(name: String, onSuccess: (DriveFolder.DriveFolderResult) -> Unit) {
        Log.d("createFolder", "koko1")

        val changeSet = MetadataChangeSet.Builder()
            .setTitle(name).build()

        Drive.DriveApi.getRootFolder(googleApi)
            .createFolder(googleApi, changeSet)
            .setResultCallback { onSuccess(it) }
    }

    fun createFolderInFolder(name: String, parentFolderId: String, onSuccess: (DriveFolder.DriveFolderResult) -> Unit) {
        folderExists(parentFolderId) {
            if (it) {
                Drive.DriveApi.fetchDriveId(googleApi, parentFolderId)
                    .setResultCallback {
                        val changeSet = MetadataChangeSet.Builder()
                            .setTitle(name).build()
                        val parentFolder = it.driveId.asDriveFolder()
                        parentFolder.createFolder(googleApi, changeSet)
                            .setResultCallback { onSuccess(it) }
                    }
            } else {
                throw Exception("Parent folder not found.  PARENT_ID: ${parentFolderId}")
            }
        }
    }
}