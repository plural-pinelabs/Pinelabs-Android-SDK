package com.plural_pinelabs.expresscheckoutsdk.common

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.plural_pinelabs.expresscheckoutsdk.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class PdfActivity : AppCompatActivity() {

    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var currentPage: PdfRenderer.Page
    private lateinit var parcelFileDescriptor: ParcelFileDescriptor
    private lateinit var imageView: ImageView
    private var pageIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pdf_activity)
        val extraUrl: String = intent.getStringExtra("pdf_url") ?: ""

        imageView = findViewById(R.id.pdf_page)
        //   showPage(pageIndex)
        findViewById<Button>(R.id.prev_page).setOnClickListener {
            if (pageIndex > 0) {
                showPage(--pageIndex)
            }
        }

        findViewById<Button>(R.id.next_page).setOnClickListener {
            if (pageIndex < pdfRenderer.pageCount - 1) {
                showPage(++pageIndex)
            }
        }
        downloadAndRenderPdf(
            extraUrl
        )
    }


    @SuppressLint("UseKtx")
    private fun showPage(index: Int) {
        // Close previous page if initialized
        if (::currentPage.isInitialized) {
            currentPage.close()
        }

        // Open and render new page
        currentPage = pdfRenderer.openPage(index)

        val bitmap = Bitmap.createBitmap(
            currentPage.width,
            currentPage.height,
            Bitmap.Config.ARGB_8888
        )
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        findViewById<ImageView>(R.id.pdf_page).setImageBitmap(bitmap)
    }


    override fun onDestroy() {
        currentPage.close()
        pdfRenderer.close()
        parcelFileDescriptor.close()
        super.onDestroy()
    }

    private fun downloadAndRenderPdf(pdfUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(pdfUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                val file = File(cacheDir, "downloaded.pdf")
                val inputStream = BufferedInputStream(connection.inputStream)
                val outputStream = FileOutputStream(file)

                val buffer = ByteArray(1024)
                var count: Int
                while (inputStream.read(buffer).also { count = it } != -1) {
                    outputStream.write(buffer, 0, count)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                withContext(Dispatchers.Main) {
                    openPdfRenderer(file)
                    showPage(0)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PdfActivity, "Failed to load PDF", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun openPdfRenderer(file: File) {
        try {
            parcelFileDescriptor =
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(parcelFileDescriptor)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error opening PDF", Toast.LENGTH_SHORT).show()
        }
    }


}
