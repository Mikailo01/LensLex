package com.bytecause.lenslex.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.bytecause.lenslex.R
import java.io.File

class ComposeFileProvider : FileProvider(
    R.xml.pictures_file_paths
) {
    companion object {
        fun getImageUri(context: Context): Uri {
            val directory = File(context.cacheDir, "images")
            directory.mkdirs()
            val file = File.createTempFile(
                "captured_image",
                ".jpg",
                directory,
            )
            val authority = context.packageName + ".fileprovider"
            return getUriForFile(
                context,
                authority,
                file,
            )
        }
    }
}