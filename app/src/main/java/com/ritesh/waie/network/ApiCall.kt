package com.ritesh.waie.network

import com.ritesh.waie.utils.Const
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resumeWithException

class ApiCall {

    companion object {

        @OptIn(ExperimentalCoroutinesApi::class)
        suspend fun request(): String{
            return suspendCancellableCoroutine { continuation ->
                try {
                    val reader: BufferedReader
                    val url = URL(Const.base_url + Const.end_point
                            + "?api_key=" + Const.api_key)

                    with(url.openConnection() as HttpURLConnection) {
                        requestMethod = "GET"
                        reader = BufferedReader(InputStreamReader(inputStream) as Reader?)

                        val response = StringBuffer()
                        var inputLine = reader.readLine()
                        while (inputLine != null) {
                            response.append(inputLine)
                            inputLine = reader.readLine()
                        }
                        reader.close()

                        if (continuation.isActive) {
                            continuation.resume(response.toString())
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    if (continuation.isActive) {
                        continuation.resumeWithException(e)
                    }
                }
            }
        }
    }
}

private fun Any.resume(value: String) {

}
