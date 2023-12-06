package ca.uwaterloo.cs346project.ui.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

fun openPdfFromAssets(context: Context, fileName: String) {
    val file = File(context.cacheDir, fileName)
    if (!file.exists()) {
        context.assets.open(fileName).use { asset ->
            FileOutputStream(file).use { output ->
                asset.copyTo(output)
            }
        }
    }

    val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(intent)
}
