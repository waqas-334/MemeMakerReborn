package com.androidbull.meme.maker.ui.activities

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.androidbull.meme.maker.R
import com.androidbull.meme.maker.helper.*
import com.androidbull.meme.maker.helper.StorageHelper.isExternalStorageWriteable
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

        if (SettingsManager.getIsAllMemesDownloaded()) {
            tvDownloadStatus.text = getString(R.string.str_all_meme_already_downloaded)
            btnDownload.text = getString(R.string.str_start_download_again)
        }

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

            if (!unZipMemesFile(download.file)) {
                val errorStr =
                    getString(R.string.something_went_wrong)
                tvDownloadStatus.text = errorStr
                btnDownload.text = getString(R.string.str_start_download_again)
                downloadState = MemeDownloadState.NEUTRAL
                SettingsManager.saveIsAllMemesDownloaded(false)
            } else {
                SettingsManager.saveIsAllMemesDownloaded(true)
            }
        } else {
            val errorStr =
                getString(R.string.sr_download_failed, getString(R.string.file_corrupted))
            tvDownloadStatus.text = errorStr
            btnDownload.text = getString(R.string.str_start_download)
            downloadState = MemeDownloadState.NEUTRAL
            SettingsManager.saveIsAllMemesDownloaded(false)

        }
    }

    private fun unZipMemesFile(memeZipFilePath: String): Boolean {
        try {
            if (isExternalStorageWriteable()) {
                val memesStorageDir = StorageHelper.getMemesPrivateDir()
                val memesDir = File(memesStorageDir)
                memesDir.mkdirs()

                val zipFile = File(memeZipFilePath)
                zipFile.unzip(memesDir)
            } else {
                return false
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return false
        }
        return true
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
                zipfile?.close()
            } catch (e: IOException) {
            }
        }
    }

    override fun onBackPressed() {
        if (downloadState == MemeDownloadState.STARTED)
            showCancelDownloadDialog()
        else {
            super.onBackPressed()
        }
    }

    private fun showCancelDownloadDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(resources.getString(R.string.download_memes_exit_dilaog_message))
            .setNegativeButton(resources.getString(R.string.no)) { _, i -> }
            .setPositiveButton(resources.getString(R.string.yes)) { _, i -> super.onBackPressed() }
            .show()
    }
}