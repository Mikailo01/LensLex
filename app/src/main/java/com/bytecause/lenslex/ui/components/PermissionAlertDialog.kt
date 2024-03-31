package com.bytecause.lenslex.ui.components

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

fun launchPermissionRationaleDialog(context: Context) {
    val builder = AlertDialog.Builder(context)
        .setTitle("Camera Permission Needed")
        .setMessage("This app needs camera permission to take pictures. Please allow permission to proceed.")
        .setPositiveButton("Allow") { dialog, _ ->
            dialog.dismiss()
            // Open app settings intent
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

    builder.create().show()
}