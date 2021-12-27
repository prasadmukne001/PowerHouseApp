package com.example.powerhouseapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.viewModels
import com.example.powerhouseapp.ui.main.MainViewModel
import com.example.powerhouseapp.databinding.MainActivityBinding
import dagger.hilt.android.AndroidEntryPoint

private const val REQUEST_IMAGE_CAPTURE = 1

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel>()
    private lateinit var mBinding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val emailID = "prasad@test.com"


        viewModel.apiManager.userConfigResponse.observe(this@MainActivity){
            when(it.isSuccess){
                true ->{
                    Log.d("MainActivity", "$it")
                }
                false ->{
                    Log.d("MainActivity", "$it")
                }
            }
        }

        viewModel.getUserConfig(emailID)

        val intent = Intent(this@MainActivity, CameraActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            //imageView.setImageBitmap(imageBitmap)
        }
    }
}