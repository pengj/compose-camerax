package me.pengj.arcompose

import android.graphics.*
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.palette.graphics.Palette
import java.io.ByteArrayOutputStream

class PaletteAnalyzer(private val onColorChange: (MeshColor) -> Unit) : ImageAnalysis.Analyzer {
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.convertImageProxyToBitmap()?.let {
            val palette = Palette.Builder(it).generate()
            onColorChange.invoke(
                MeshColor(
                    palette.darkVibrantSwatch?.rgb ?: -1,
                    palette.darkVibrantSwatch?.titleTextColor ?: -1,
                    imageProxy.imageInfo.timestamp
                )
            )
        }
        imageProxy.close()
    }
}

//based on the code from https://stackoverflow.com/a/56812799
fun ImageProxy.convertImageProxyToBitmap(): Bitmap? {
    val yBuffer = planes[0].buffer // Y
    val vuBuffer = planes[2].buffer // VU

    val ySize = yBuffer.remaining()
    val vuSize = vuBuffer.remaining()

    val nv21 = ByteArray(ySize + vuSize)

    yBuffer.get(nv21, 0, ySize)
    vuBuffer.get(nv21, ySize, vuSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}