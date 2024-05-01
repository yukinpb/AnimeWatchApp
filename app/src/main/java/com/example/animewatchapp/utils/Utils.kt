package com.example.animewatchapp.utils

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.concurrent.TimeUnit
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object Utils {
    private var httpClient = OkHttpClient.Builder()
        .cookieJar(AndroidCookieJar())
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(2, TimeUnit.MINUTES)
        .build()

    private fun get(url: String,
            mapOfHeaders: Map<String, String>? = null
    ): String {
        val requestBuilder = Request.Builder().url(url)
        if (!mapOfHeaders.isNullOrEmpty()) {
            mapOfHeaders.forEach{
                requestBuilder.addHeader(it.key, it.value)
            }
        }
        return httpClient.newCall(requestBuilder.build())
            .execute().body!!.string()
    }

    private fun post(url: String, mapOfHeaders: Map<String, String>? = null, payload: Map<String, String>? = null): String {
        val requestBuilder = Request.Builder().url(url)

        if (!mapOfHeaders.isNullOrEmpty()) {
            mapOfHeaders.forEach {
                requestBuilder.addHeader(it.key, it.value)
            }
        }

        val requestBody = payload?.let {
            FormBody.Builder().apply {
                it.forEach { (key, value) ->
                    add(key, value)
                }
            }.build()
        }

        if (requestBody != null) {
            requestBuilder.post(requestBody)
        }

        val response = httpClient.newCall(requestBuilder.build()).execute()
        return response.body?.string() ?: ""
    }

    fun getJsoup(
        url: String,
        mapOfHeaders: Map<String, String>? = null
    ): Document {
        return Jsoup.parse(get(url, mapOfHeaders))
    }

    fun getJson(
        url: String,
        mapOfHeaders: Map<String, String>? = null
    ): JsonElement? {
        return JsonParser.parseString(get(url, mapOfHeaders))
    }

    fun postJson(
        url: String,
        mapOfHeaders: Map<String, String>? = null,
        payload: Map<String, String>? = null
    ): JsonElement? {
        val res = post(url, mapOfHeaders, payload)
        return JsonParser.parseString(res)
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
}