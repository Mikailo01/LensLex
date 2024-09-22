package com.bytecause.lenslex.util

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.util.Log
import org.json.JSONObject
import java.io.IOException
import java.security.MessageDigest
import java.util.UUID


object Util {

    fun generateNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun readJsonAsMapFromAssets(context: Context, fileName: String): Map<String, String>? {
        return try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val jsonString = String(buffer, Charsets.UTF_8)
            val jsonObject = JSONObject(jsonString)
            val keyValueMap = mutableMapOf<String, String>()
            jsonObject.keys().forEach { key ->
                keyValueMap[key] = jsonObject.getString(key)
            }
            keyValueMap
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun forceOrientation(context: Context, activityInfo: Int) {
        (context as? Activity)?.requestedOrientation = activityInfo
    }

    // When Credential Manager is displayed, the device orientation is locked to prevent the coroutine
    // from being cancelled when the orientation is changed.
    // I couldn't think of a better way to solve this problem, because viewModel
    // should not hold an instance of the activity context.
    inline fun withOrientationLocked(context: Context, block: () -> Unit) {
        try {
            forceOrientation(context, ActivityInfo.SCREEN_ORIENTATION_LOCKED)
            block()
        } catch (e: Exception) {
            Log.e("LockedOrientationScope", "Error: ${e.message}")
        } finally {
            forceOrientation(context, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        }
    }
}