package com.androidbull.meme.generator.helper

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import com.androidbull.meme.generator.R
import com.androidbull.meme.generator.TAG
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipFile


fun shareApp(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, context.packageName)
        }

        var sAux: String = context.getString(R.string.share_app_message) + "\n"
        sAux = """
                ${sAux}https://play.google.com/store/apps/details?id=${context.packageName}
                """.trimIndent()
        intent.putExtra(Intent.EXTRA_TEXT, sAux)
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.choose_one)))
    } catch (ex: Exception) {
        ex.printStackTrace()
    }

}

fun openDeveloperPageInPlayStore(context: Context, developerId: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW)
            .setFlags(FLAG_ACTIVITY_NEW_TASK)
            .setData(Uri.parse("https://play.google.com/store/apps/developer?id=$developerId"))
        try {
            context.startActivity(
                Intent(intent)
                    .setPackage("com.android.vending")
            )
        } catch (exception: ActivityNotFoundException) {
            context.startActivity(intent)
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }

}

fun openAppInPlayStore(packageName: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW)
            .setFlags(FLAG_ACTIVITY_NEW_TASK)
            .setData(Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
        try {
            AppContext.getInstance().context.startActivity(
                Intent(intent)
                    .setPackage("com.android.vending")
            )
        } catch (exception: ActivityNotFoundException) {
            AppContext.getInstance().context.startActivity(intent)
        }
    } catch (exception: Exception) {
    }
}

fun openLinkInBrowser(context: Context, link: String) {
    try {
        val intent =Intent(Intent.ACTION_VIEW, Uri.parse(link))
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    } catch (exception: Exception) {
    }
}

fun sendEmail(context: Context, subject: String, body: String) {
    val send = Intent(Intent.ACTION_SENDTO)
    val uriText = "mailto:" + Uri.encode(SUPPORT_EMAIL) +
            "?subject=" + Uri.encode(subject) +
            "&body=" + Uri.encode(body)
    val uri = Uri.parse(uriText)
    send.data = uri
    try {
        context.startActivity(Intent.createChooser(send, "Send email..."))
    } catch (exception: Exception) {
    }
}

fun downGradeFromPremium() {
    PreferenceManager.getInstance().setBoolean(PREF_IS_PREMIUM_USER, false) // for disk cache
    PremiumMemberObservable.isPremiumUser.value = false // for memory cache
    Log.d(TAG, "downGradeFromPremium")
}

fun upgradeToPremium() {
    PreferenceManager.getInstance().setBoolean(PREF_IS_PREMIUM_USER, true)
    PremiumMemberObservable.isPremiumUser.value = true
    Log.d(TAG, "upgradedToPremium")
}

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun spToPx(sp: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        sp,
        AppContext.getInstance().context.resources.displayMetrics
    )
}

fun setFullScreen(isFullScreen: Boolean, activity: Activity) {
    if (isFullScreen) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
    } else {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }
}


fun showSoftKeyboard(view: View) {
    val inputMethodManager =
        AppContext.getInstance().context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    inputMethodManager?.showSoftInput(view, 0)

//    inputMethodManager?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun hideSoftKeyboard(view: View) {
    val inputMethodManager =
        AppContext.getInstance().context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun getNumberOfColumns(context: Context, viewId: Int): Int {
    val view = View.inflate(context, viewId, null)
    view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    val width = view.measuredWidth
    var count = context.resources.displayMetrics.widthPixels / width
    val remaining = context.resources.displayMetrics.widthPixels - width * count
    count++
    return count
}

fun getJsonDataFromAsset(context: Context, fileName: String): String? {
    val jsonString: String
    try {
        jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (ioException: IOException) {
        ioException.printStackTrace()
        return null
    }
    return jsonString
}

fun hasPermissions(vararg permissions: String, context: Context): Boolean = permissions.all {
    ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}


fun Context.resIdByName(resIdName: String?, resType: String): Int {
    resIdName?.let {
        return resources.getIdentifier(it, resType, packageName)
    }
    throw Resources.NotFoundException()
}

data class ZipIO(val entry: ZipEntry, val output: File)

fun File.unzip(unzipLocationRoot: File) {

    if (!unzipLocationRoot.exists()) {
        unzipLocationRoot.mkdirs()
    }

    ZipFile(this).use { zip ->
        zip.entries()
            .asSequence()
            .map {
                val outputFile = File(unzipLocationRoot.absolutePath + File.separator + it.name)
                ZipIO(it, outputFile)
            }
            .map {
                it.output.parentFile?.run {
                    if (!exists()) mkdirs()
                }
                it
            }
            .filter { !it.entry.isDirectory }
            .forEach { (entry, output) ->
                zip.getInputStream(entry).use { input ->
                    output.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
    }

}

fun base64Key2() = "SI(EWiPQ&GY-lYzNNvLIGCfVO.eDayFUhxfv6HCv*6N,KpsqaowZZ-PU"

fun base64Key4() = ")ky-.W/BYTD/SoHELyEyx(y/Q(x,jskca/Az*VyMTdZcttuY/o(k-bHR"

fun base64Key1() = "miibiJanbGKQHKIg.W%baqefaaocaq-amiibcGkcaqeaL+HlleLuhc'M"

fun base64Key7() = "T(hfwKysxyL&DUYlQqWRtaDvD'EmSGc/nftFdRUUB'AnzuJMnqidaqab"
fun base64Key5() = "uVwkGO&&sNq-rrXKJzXGQxLGmWEr,nsgu/,P/dtwK-pDu)canJvhM'iQ"

fun base64Key3() = "&tKCuFi&%)CKezbWsD,ynjbqTEUfrNoZ%FDWGcjCrUerKuDsH+Qp6yVN"

fun base64Key6() = "JJ(VP&bZBo'UllMrnuKfM%(U%P'L'nWZSU)/pPAiGF-,BrFtthgYziC+"

fun String.swapBase64Chars() = map {
    when {
        it.isUpperCase() -> it.toLowerCase()
        it.isLowerCase() -> it.toUpperCase()
        it.isDigit() -> (37 + (it.toInt() - 48)).toChar()
        it.isDefined() -> if (it.toInt() in 37..46) (48 + (it.toInt() - 37)).toChar() else it
        else -> it
    }
}.joinToString("")


fun getDecodedBase64PublicKey() =
    (base64Key1() + base64Key2() + base64Key3() + base64Key4() + base64Key5() + base64Key6() + base64Key7()).swapBase64Chars()
