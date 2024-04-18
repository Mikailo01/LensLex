package com.bytecause.lenslex.ui.components

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.bytecause.lenslex.R

fun launchPermissionRationaleDialog(context: Context) {
    val builder = AlertDialog.Builder(context)
        .setTitle(context.resources.getString(R.string.camera_permission_needed))
        .setMessage(context.resources.getString(R.string.allow_camera_permission_message))
        .setPositiveButton(context.resources.getString(R.string.allow)) { dialog, _ ->
            dialog.dismiss()

            // Open app settings intent
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }
        .setNegativeButton(context.resources.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }

    builder.create().show()
}