package ca.uwaterloo.cs346project.ui.whiteboard

import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asAndroidBitmap
import dev.shreyaspatil.capturable.Capturable
import dev.shreyaspatil.capturable.controller.CaptureController
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun ExportCapturable(captureController: CaptureController, content: @Composable () -> Unit) {
    Capturable(
        controller = captureController,
        onCaptured = { bitmap, error ->
            // This is captured bitmap
            if (bitmap != null) {
                // Bitmap is captured successfully.
                println("Bitmap successful")
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas : Canvas = page.canvas
                canvas.drawBitmap(bitmap.asAndroidBitmap(), 0f, 0f, null)
                pdfDocument.finishPage(page)

                try {
                    val timestamp = System.currentTimeMillis()
                    val sdf = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
                    val date = Date(timestamp)
                    val formattedDate = sdf.format(date)
                    val filename = "Canvas$formattedDate.pdf"
                    val pdfFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename)
                    pdfDocument.writeTo(FileOutputStream(pdfFile))
                    // Handle success - the PDF is saved
                    println("save successful")
                } catch (e: IOException) {
                    // Handle the error
                    println("exception")
                    println(e)
                } finally {
                    pdfDocument.close()
                }

            }

            if (error != null) {
                // Error occurred.
                println("abc")
            }
        }
    ) {
        content()
    }
}