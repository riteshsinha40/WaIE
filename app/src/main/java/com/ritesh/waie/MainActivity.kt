package com.ritesh.waie

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ritesh.waie.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    lateinit var binding : ActivityMainBinding
    lateinit var pref : SharedPreferences

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        pref = applicationContext.getSharedPreferences("MyPref", MODE_PRIVATE)
        val getStoredDate= pref?.getString("DATE", "")

        if(getStoredDate.isNullOrEmpty()){
            checkNetworkAndHitApi(false)
        }else{
            val storeDate = SimpleDateFormat("yyyy-MM-dd").parse(getStoredDate)
            checkUseCase(storeDate)
        }


        viewModel.data.observe(this, Observer {

            viewModel.savePlanetaryData(it.date, it.title, it.explanation)
            binding.title.setText(it.title)
            viewModel.setBitmap(it.url)
            binding.explanation.setText(it.explanation)

        })

        viewModel.getBitmapData.observe(this, Observer {

            viewModel.savePlanetaryImage(it)
            binding.image.setImageBitmap(it)
            binding.image.visibility = View.VISIBLE

        })
    }

    private fun checkUseCase(storeDate: Date) : Unit{

        if(DateUtils.isToday(storeDate.time)){
            checkNetworkAndHitApi(true)
        }else if(viewModel.checkForInternet(this)) {
            viewModel.getData()
        }else{
            Toast.makeText(
                this,
                "We are not connected to the internet, showing you the last image we have.",
                Toast.LENGTH_LONG
            ).show()

            val title = pref?.getString("TITLE", "")
            val explanation = pref?.getString("EXPLANATION", "")
            val image = pref?.getString("IMAGE", "")

            binding.title.setText(title)
            binding.image.setImageBitmap(viewModel.decodeBase64(image))
            binding.image.visibility = View.VISIBLE
            binding.explanation.setText(explanation)
        }
    }

    private fun checkNetworkAndHitApi(storedData : Boolean) {

        if(storedData) {
            val title = pref?.getString("TITLE", "")
            val explanation = pref?.getString("EXPLANATION", "")
            val image = pref?.getString("IMAGE", "")

            binding.title.setText(title)
            binding.image.setImageBitmap(viewModel.decodeBase64(image))
            binding.image.visibility = View.VISIBLE
            binding.explanation.setText(explanation)
        }else if (viewModel.checkForInternet(this)) {
            viewModel.getData()
        } else{
            Toast.makeText(
                this,
                "We are not connected to the internet, Please connect internet and try again",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}