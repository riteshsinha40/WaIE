package com.ritesh.waie

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ritesh.waie.model.PlanetaryData
import com.ritesh.waie.network.ApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    var data = MutableLiveData<PlanetaryData>()
    var getBitmapData = MutableLiveData<Bitmap>()
    var bitmap : Bitmap? = null


    fun getData() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = ApiCall.request()

            withContext(Dispatchers.Main) {
                // call to UI thread

                data.value = getResponse(JSONObject(response))

            }
        }
    }
    fun setBitmap(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            bitmap = getBitmapFromURL(url)
            withContext(Dispatchers.Main){
                getBitmapData.value = bitmap
            }

        }
    }

    fun getBitmapFromURL(src: String?): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val url = URL(src)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            bitmap = BitmapFactory.decodeStream(input)
            return bitmap
        } catch (e: IOException) {
            Log.e("", e.message.toString())
        }
        return bitmap
    }


    private fun getResponse(jsonObject: JSONObject) : PlanetaryData{
        val pojoObject = PlanetaryData()
        try {
            pojoObject.date =  jsonObject.getString("date")
            pojoObject.url = jsonObject.getString("url")
            pojoObject.title =    jsonObject.getString("title")
            pojoObject.explanation = jsonObject.getString("explanation")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return pojoObject
    }

    // method for bitmap to base64
    fun encodeTobase64(image: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        val imageEncoded: String = Base64.encodeToString(b, Base64.DEFAULT)
        Log.d("Image Log:", imageEncoded)
        return imageEncoded
    }

    // method for base64 to bitmap
    fun decodeBase64(input: String?): Bitmap? {
        val decodedByte = Base64.decode(input, 0)
        return BitmapFactory
            .decodeByteArray(decodedByte, 0, decodedByte.size)
    }

    fun savePlanetaryData(date: String, title: String, explanation: String){
        val editor: SharedPreferences.Editor? = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE)?.edit()?.apply {
            putString("DATE", date)
            putString("TITLE", title)
            putString("EXPLANATION", explanation)
            commit()
        }
    }

    fun savePlanetaryImage(bitmap: Bitmap){
        val editor: SharedPreferences.Editor? = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE)?.edit()?.apply {
            putString("IMAGE", encodeTobase64(bitmap))
            commit()
        }
    }

    @SuppressLint("ServiceCast")
    fun checkForInternet(context: Context): Boolean {

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }}