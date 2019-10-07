package com.bwet.digifit.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtil {
    fun getOrCreateProfileImageFile(context: Context, extension: String): File {
        val folder = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(folder, "profile_image.${extension.split('/').last()}")
        if(!file.exists()) file.parentFile.mkdirs()
        return file
    }

    fun copyStreamToFile(inputStream: InputStream, outputFile: File) {
        inputStream.use { input ->
            val outputStream = FileOutputStream(outputFile)
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024) // buffer size
                while (true) {
                    val byteCount = input.read(buffer)
                    if (byteCount < 0) break
                    output.write(buffer, 0, byteCount)
                }
                output.flush()
            }
        }
    }
}
