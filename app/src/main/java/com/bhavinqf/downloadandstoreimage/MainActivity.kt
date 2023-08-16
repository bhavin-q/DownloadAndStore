package com.bhavinqf.downloadandstoreimage

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bhavinqf.downloadandstoreimage.databinding.ActivityMainBinding
import com.bhavinqf.downloadandstoreimage.dialogs.CustomVideoDialog
import com.bhavinqf.downloadandstoreimage.fordelete.DeleteUtilsR
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.permissionx.guolindev.PermissionX
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Arrays


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    val activity = this@MainActivity
    var imageAdapter: ImageAdapter? = null
    var mArrayList: ArrayList<File> = arrayListOf()
    val LOG_TAG="bhavin->"
    var selectedPhotoUri:Uri?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setData()
    }

    private fun setData() {
        val urlString = binding.enterUrl.text?.trim().toString()
        val deleteUtilsR = DeleteUtilsR(activity)

        imageAdapter = ImageAdapter(
            activity,
            mArrayList,
            deleteUtilsR,
        ) { position: Int, _, click: String ->
        }

        binding.apply {
            photoPickerButton.setOnClickListener {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
//                val mimeType = "image/*"
//                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.SingleMimeType(mimeType)))
            }
            videoPickerButton.setOnClickListener {
                pickMediaVideo.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
            }


            downloadButton.setOnClickListener {

                if (urlString.isNotEmpty()) {

                    /*     val url= URL(urlString)
                    val imageData = url.readBytes()*/


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        PermissionX.init(activity)
                            .permissions(Manifest.permission.READ_MEDIA_IMAGES)
                            .onExplainRequestReason { scope, deniedList ->
                                scope.showRequestReasonDialog(
                                    deniedList,
                                    "Core fundamental are based on these permissions",
                                    "OK",
                                    "Cancel"
                                )
                            }
                            .onForwardToSettings { scope, deniedList ->
                                scope.showForwardToSettingsDialog(
                                    deniedList,
                                    "You need to allow necessary permissions in Settings manually",
                                    "OK",
                                    "Cancel"
                                )
                            }
                            .request { allGranted, grantedList, deniedList ->
                                if (allGranted) {
                                    downloadAndSaveBitmap(urlString, progressBar)
                                } else {
                                    Toast.makeText(
                                        activity,
                                        "These permissions are denied: $deniedList",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }
                    else {
                        PermissionX.init(activity)
                            .permissions(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                            .onExplainRequestReason { scope, deniedList ->
                                scope.showRequestReasonDialog(
                                    deniedList,
                                    "Core fundamental are based on these permissions",
                                    "OK",
                                    "Cancel"
                                )
                            }
                            .onForwardToSettings { scope, deniedList ->
                                scope.showForwardToSettingsDialog(
                                    deniedList,
                                    "You need to allow necessary permissions in Settings manually",
                                    "OK",
                                    "Cancel"
                                )
                            }
                            .request { allGranted, grantedList, deniedList ->
                                if (allGranted) {
                                    downloadAndSaveBitmap(urlString, progressBar)
                                } else {
                                    Toast.makeText(
                                        activity,
                                        "These permissions are denied: $deniedList",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }


                } else {
                    Toast.makeText(this@MainActivity, "is empty", Toast.LENGTH_SHORT).show()
                }


            }

            downloadImageList.layoutManager = GridLayoutManager(
                activity,
                3,
                GridLayoutManager.VERTICAL,
                false
            )
            downloadImageList.adapter = imageAdapter


        }
        refreshData()
    }


    // Registers a photo picker activity launcher in single-select mode.
    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            selectedPhotoUri=uri
            Log.e("PhotoPicker", "Selected URI: $uri")
            binding.imageViewSample.setImageURI(selectedPhotoUri)
        } else {
            Log.e("PhotoPicker", "No media selected")
        }
    }
    val pickMediaVideo = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            val customDialog = CustomVideoDialog(this,uri)
            customDialog.show()
        } else {
            Log.e("PhotoPicker", "No media selected")
        }
    }



    fun downloadAndSaveBitmap(urlString: String, progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
        var bitmap: Bitmap? = null
        Glide.get(activity).clearMemory();
        Glide.with(activity)
            .asBitmap()
            .load(urlString)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    e?.stackTrace
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                   return false
                }

            })
            .into(object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    bitmap = resource
                    runOnUiThread {
                        binding.imageViewSample.setImageBitmap(bitmap)
                        saveBitmap(bitmap)
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                    }
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)

                    runOnUiThread {
                        progressBar.visibility = View.GONE
                    }
                }
            })

    }

    fun saveBitmap(bitmap: Bitmap?) {
        val fileName = "${System.currentTimeMillis()}.jpg"
        var fileOutputStream: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val contentValues = ContentValues().apply {

                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.RELATIVE_PATH, filePath())
                put(MediaStore.MediaColumns.MIME_TYPE, "image/*")
            }

            val uri: Uri? =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fileOutputStream = uri?.let { contentResolver.openOutputStream(it) }

            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream?.flush()
            fileOutputStream?.close()

//            sendBroadcast(Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", uri))
            scanFileRefreshGAllery(filePath(),activity)
            binding.progressBar.visibility = View.GONE
            refreshData()
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val mFile = File(imagesDir, getString(R.string.app_name))
            if (!mFile.exists()) {
                mFile.mkdirs()
            }
            val imageFile = File(mFile, fileName)
            Log.e(LOG_TAG,mFile.absolutePath+"---")

           val fileOutputStreamI=FileOutputStream(imageFile)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStreamI)
            fileOutputStreamI?.flush()
            fileOutputStreamI?.close()
            refreshData()
            binding.progressBar.visibility = View.GONE

        }

    }

    fun Context.getPathFromUri(uri: Uri?) {
        val cursor: Cursor? = uri?.let { contentResolver.query(it, null, null, null, null) }
        if (cursor != null) {
            if (cursor.moveToNext()) {
                cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
            }
        }
        cursor?.close()
    }

    fun filePath(): String {
        return Environment.DIRECTORY_PICTURES + File.separator + getString(R.string.app_name)
    }

    fun createDirIfNot() {
        val imagesDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val mFile = File(imagesDir, getString(R.string.app_name))
        if (!mFile.exists()) {
            mFile.mkdirs()
        }
    }
    fun scanFileRefreshGAllery(path: String, context: Context) {
        MediaScannerConnection.scanFile(context, arrayOf(path), null,
            object : MediaScannerConnection.OnScanCompletedListener {
                override fun onScanCompleted(path: String, uri: Uri?) {
                    Log.e(LOG_TAG,"Finished scanning $path")
                }
            })
    }

    fun refreshData() {
        mArrayList.clear()
        imageAdapter?.notifyDataSetChanged()
        val imagesDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val mFile = File(imagesDir, getString(R.string.app_name))
        Log.e(LOG_TAG, "refreshData->"+mFile.absolutePath)
        val listFiles = mFile.listFiles()
        listFiles?.let { list ->
            Arrays.sort(list) { f1, f2 ->
                f2.lastModified().compareTo(f1.lastModified())
            }
        }
        if (listFiles != null) {
            for (mItem in listFiles) {
                if (mItem.isFile && (mItem.extension in extensionArray)) {
                    mArrayList.add(mItem)
                }
            }
        }
    }

    val extensionArray = arrayOf("jpg", ".jpg", "jpeg", ".jpeg", "png", ".png")


}