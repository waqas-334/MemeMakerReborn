package com.androidbull.meme.maker.ui.activities

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.androidbull.meme.maker.R
import com.androidbull.meme.maker.helper.ALL_MEMES_ZIP_FILE_NAME
import com.androidbull.meme.maker.helper.ALL_MEMES_ZIP_URL
import com.androidbull.meme.maker.helper.StorageHelper
import com.androidbull.meme.maker.helper.StorageHelper.isExternalStorageWriteable
import com.androidbull.meme.maker.helper.unzip
import com.google.android.material.appbar.MaterialToolbar
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import java.io.File
import java.io.IOException
import java.util.zip.ZipFile

private const val TAG = "DownloadAllMemeActivity"

enum class MemeDownloadState {
    NEUTRAL, STARTED
}

class DownloadAllMemeActivity : BaseActivity(), FetchListener {

    private lateinit var tbDownloadActivity: MaterialToolbar
    private lateinit var btnDownload: Button
    private lateinit var tvDownloadStatus: TextView
    private lateinit var pbDownload: ProgressBar
    private lateinit var fetch: Fetch
    private var downloadState = MemeDownloadState.NEUTRAL


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.download_all_meme_activity)

        initUi()
        initToolbar()
        initDownloader()

        btnDownload.setOnClickListener {

            when (downloadState) {
                MemeDownloadState.NEUTRAL -> {
                    btnDownload.text = getString(R.string.str_cancel_download)
                    tvDownloadStatus.text = getString(R.string.str_download_started)
                    downloadState = MemeDownloadState.STARTED
                    pbDownload.progress = 0
                    startDownload()
                }
                MemeDownloadState.STARTED -> {
                    btnDownload.text = getString(R.string.str_start_download)
                    tvDownloadStatus.text = getString(R.string.str_download_cancelled)
                    downloadState = MemeDownloadState.NEUTRAL
                    cancelDownload()
                }
            }
        }
    }


    private fun initUi() {
        tbDownloadActivity = findViewById(R.id.tbDownloadActivity)
        btnDownload = findViewById(R.id.btnDownload)
        tvDownloadStatus = findViewById(R.id.tvDownloadStatus)
        pbDownload = findViewById(R.id.pbDownload)
    }


    private fun initToolbar() {
        setSupportActionBar(tbDownloadActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun initDownloader() {
        val fetchConfiguration = FetchConfiguration.Builder(this)
            .enableLogging(true)
            .preAllocateFileOnCreation(false)
            .build()

        fetch = Fetch.Impl.getInstance(fetchConfiguration)
        fetch.addListener(this)
    }

    private fun cancelDownload() {
        fetch.cancelAll()
    }

    private fun startDownload() {

        try {
            if (isExternalStorageWriteable()) {
                val fileDir = StorageHelper.getExternalCacheDir()
                val filePath = fileDir.path + File.separator + ALL_MEMES_ZIP_FILE_NAME

                val file = File(filePath)
                if (!file.exists()) {
                    file.createNewFile()
                }

                val request = Request(ALL_MEMES_ZIP_URL, filePath)
                request.priority = Priority.HIGH
                request.enqueueAction = EnqueueAction.REPLACE_EXISTING
                request.networkType = NetworkType.ALL

                fetch.enqueue(request = request, func2 = { error ->
                    val errorStr = getString(R.string.sr_download_failed, error.name)
                    tvDownloadStatus.text = errorStr
                })
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.str_external_storage_busy),
                    Toast.LENGTH_SHORT
                ).show()
            }

        } catch (ioException: IOException) {
            ioException.printStackTrace()
        } catch (npException: NullPointerException) {
            npException.printStackTrace()
        }
    }


    override fun onWaitingNetwork(download: Download) {
        tvDownloadStatus.text = getString(R.string.str_waiting_for_network)
    }

    override fun onStarted(
        download: Download,
        downloadBlocks: List<DownloadBlock>,
        totalBlocks: Int
    ) {

    }

    override fun onError(download: Download, error: Error, throwable: Throwable?) {
        val errorStr = getString(R.string.sr_download_failed, error.name)
        tvDownloadStatus.text = errorStr
        btnDownload.text = getString(R.string.str_start_download)
        downloadState = MemeDownloadState.NEUTRAL
    }

    override fun onDownloadBlockUpdated(
        download: Download,
        downloadBlock: DownloadBlock,
        totalBlocks: Int
    ) {
    }

    override fun onAdded(download: Download) {}

    override fun onQueued(download: Download, waitingOnNetwork: Boolean) {}

    override fun onCompleted(download: Download) {
        if (isValidZipFile(File(download.file))) {
            tvDownloadStatus.text = getString(R.string.all_memes_downloaded)
            btnDownload.text = getString(R.string.str_start_download_again)
            downloadState = MemeDownloadState.NEUTRAL

            unZipMemesFile(download.file)

        } else {
            val errorStr =
                getString(R.string.sr_download_failed, getString(R.string.file_corrupted))
            tvDownloadStatus.text = errorStr
            btnDownload.text = getString(R.string.str_start_download)
            downloadState = MemeDownloadState.NEUTRAL
        }
    }

    private fun unZipMemesFile(memeZipFilePath: String) {
        try {
            if (isExternalStorageWriteable()) {
                val memesStorageDir = StorageHelper.getMemesPrivateDir()
                val memesDir = File(memesStorageDir)
                memesDir.mkdirs()

                val zipFile = File(memeZipFilePath)
                zipFile.unzip(memesDir)
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.str_external_storage_busy),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onProgress(
        download: Download,
        etaInMilliSeconds: Long,
        downloadedBytesPerSecond: Long
    ) {
        val progress = download.progress
        pbDownload.progress = progress
        tvDownloadStatus.text = getString(R.string.str_download_progress, progress)
    }

    override fun onPaused(download: Download) {

    }

    override fun onResumed(download: Download) {

    }

    override fun onCancelled(download: Download) {
        tvDownloadStatus.text = getString(R.string.str_download_cancelled)
        btnDownload.text = getString(R.string.str_start_download)
        downloadState = MemeDownloadState.NEUTRAL
    }

    override fun onRemoved(download: Download) {

    }

    override fun onDeleted(download: Download) {

    }


    override fun onDestroy() {
        super.onDestroy()
        fetch.cancelAll()
        fetch.close()
        StorageHelper.clearCacheDirs()
    }

    private fun isValidZipFile(file: File): Boolean {
        var zipfile: ZipFile? = null
        return try {
            zipfile = ZipFile(file)
            true
        } catch (e: IOException) {
            false
        } finally {
            try {
                if (zipfile != null) {
                    zipfile.close()
                }
            } catch (e: IOException) {
            }
        }
    }


    //PRDownloader
    /* val fileDir = if (StorageHelper.isExternalStorageWriteable()) {
         StorageHelper.getExternalCacheDir()
     } else {
         StorageHelper.getInternalCacheDir()
     }


     val filePath = fileDir.path + File.separator
     val fileName = "all_memes.zip"

     val file = File(filePath)
     if (!file.exists()) {
         file.mkdirs()
     }


     val downloadId = PRDownloader.download(zipUrl, filePath, fileName).build()
         .setOnProgressListener(object : OnProgressListener {
             override fun onProgress(progress: Progress?) {
                 Log.d(TAG, "onProgress: ${progress.toString()}")
             }
         }).start(object : OnDownloadListener {
             override fun onDownloadComplete() {
                 Log.d(TAG, "onDownloadComplete: Download Successfully completed")
             }

             override fun onError(error: Error?) {
                 Log.d(TAG, "onError: Error downloading file")
             }
         })*/

    /*    GlobalScope.launch(Dispatchers.IO) {
            try {

                val dir = if (StorageHelper.isExternalStorageWriteable()) {
                    StorageHelper.getExternalCacheStorage()
                } else {
                    StorageHelper.getInternalCacheStorage()
                }

                val fileDir = File(dir);
                fileDir.mkdirs()

                val filePath = dir + "all_memes" + ".zip"

                val file = File(filePath);
                if (!file.exists()) {
                    file.createNewFile()
                }


                downloadFile(
                    url = zipUrl,
                    downloadFile = file
                ) { bytesRead, contentLength, isDone ->
                    launch(Dispatchers.Main) {
                        try {
                            val percentage = (bytesRead * 100) / contentLength
                            Log.d(TAG, "downloadZip: $percentage%")
                            if (isDone) {
                                Log.d(TAG, "downloadZip: File Download Complete")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }*/

}