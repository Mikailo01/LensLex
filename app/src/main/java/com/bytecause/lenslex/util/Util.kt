package com.bytecause.lenslex.util

import android.content.Context
import org.json.JSONObject
import java.io.IOException


object Util {

    fun readJsonFromAssets(context: Context, fileName: String): String? {
        return try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
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
}